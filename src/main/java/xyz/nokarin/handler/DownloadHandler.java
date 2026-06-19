package xyz.nokarin.handler;

import xyz.nokarin.gui.UpdateState;
import xyz.nokarin.util.Logger;
import xyz.nokarin.util.ProgressCalculator;
import xyz.nokarin.util.UpdateStage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

public class DownloadHandler {
    private static final int BUFFER_SIZE = 16_384;
    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT = 30_000;
    private static final int MAX_RETRIES = 3;

    public void downloadFile(
            String fileUrl,
            String targetPath,
            ProgressCalculator calculator,
            Consumer<UpdateState> callback
    ) throws Exception {
        Exception lastError = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                tryDownload(fileUrl, targetPath, calculator, callback);
                return;
            } catch (Exception e) {
                lastError = e;
                Logger.warn("Download attempt " + attempt + "/" + MAX_RETRIES + " failed: " + e.getMessage());

                if (attempt < MAX_RETRIES) {
                    long backoff = 1500L * attempt;
                    callback.accept(new UpdateState(
                            "Retrying download (" + attempt + "/" + MAX_RETRIES + ")...",
                            calculator.getTotalProgress()
                    ));
                    Thread.sleep(backoff);
                }
            }
        }

        throw new IOException("Download failed after " + MAX_RETRIES + " attempts", lastError);
    }

    private void tryDownload(
            String fileUrl,
            String targetPath,
            ProgressCalculator calculator,
            Consumer<UpdateState> callback
    ) throws Exception {
        HttpURLConnection conn = openConnection(fileUrl);

        int status = conn.getResponseCode();
        int redirects = 0;
        while ((status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_MOVED_PERM
                || status == 307 || status == 308) && redirects < 5) {
            String location = conn.getHeaderField("Location");
            conn.disconnect();
            conn = openConnection(location);
            status = conn.getResponseCode();
            redirects++;
        }

        if (status != HttpURLConnection.HTTP_OK) {
            conn.disconnect();
            throw new IOException("Server returned HTTP " + status);
        }

        long totalSize = conn.getContentLengthLong();
        boolean knownSize = totalSize > 0;

        try (InputStream input = new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
             FileOutputStream output = new FileOutputStream(targetPath)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            long downloaded = 0;
            long startTime = System.currentTimeMillis();
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                downloaded += bytesRead;

                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > 0) {
                    double speed = (downloaded / 1024.0) / (elapsed / 1000.0);

                    if (knownSize) {
                        int pct = (int) ((downloaded * 100) / totalSize);
                        calculator.update(UpdateStage.DOWNLOAD, pct);
                        long eta = (long) ((totalSize - downloaded) / (speed * 1024));
                        callback.accept(new UpdateState(
                                "Downloading " + formatSize(downloaded) + " / " + formatSize(totalSize),
                                calculator.getTotalProgress(),
                                speed,
                                eta
                        ));
                    } else {
                        // Size unknown
                        int pulse = 15 + (int) ((Math.sin(downloaded / 200_000.0) + 1) / 2 * 60);
                        calculator.update(UpdateStage.DOWNLOAD, pulse);
                        callback.accept(new UpdateState(
                                "Downloading " + formatSize(downloaded) + "...",
                                calculator.getTotalProgress(),
                                speed,
                                0
                        ));
                    }
                }
            }
        } finally {
            conn.disconnect();
        }

        calculator.update(UpdateStage.DOWNLOAD, 100);
    }

    private HttpURLConnection openConnection(String urlString) throws Exception {
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "HorizonUI-Updater/1.0");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setInstanceFollowRedirects(false);
        return conn;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }
}
