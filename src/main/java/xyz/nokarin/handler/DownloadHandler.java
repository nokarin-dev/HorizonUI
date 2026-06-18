package xyz.nokarin.handler;

import xyz.nokarin.gui.UpdateState;
import xyz.nokarin.util.ProgressCalculator;
import xyz.nokarin.util.UpdateStage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class DownloadHandler {
    private static final int BUFFER_SIZE = 8192;

    public void downloadFile(
            String fileUrl,
            String targetPath,
            ProgressCalculator calculator,
            Consumer<UpdateState> callback
    ) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        long totalSize = connection.getContentLengthLong();
        if (totalSize <= 0) {
            throw new IOException("Invalid content length");
        }

        try (InputStream input = new BufferedInputStream(connection.getInputStream());
             FileOutputStream output = new FileOutputStream(targetPath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long downloaded = 0;

            long startTime = System.currentTimeMillis();
            long lastTime = startTime;

            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                downloaded += bytesRead;

                long now = System.currentTimeMillis();
                long elapsed = now - startTime;

                if (elapsed > 0) {

                    int percent = (int) ((downloaded * 100) / totalSize);
                    calculator.update(UpdateStage.DOWNLOAD, percent);

                    double speed = (downloaded / 1024.0) / (elapsed / 1000.0); // KB/s

                    long remainingBytes = totalSize - downloaded;
                    long eta = (long) (remainingBytes / (speed * 1024));

                    callback.accept(new UpdateState(
                            "Downloading update...",
                            calculator.getTotalProgress(),
                            speed,
                            eta
                    ));
                }
            }
        }

        calculator.update(UpdateStage.DOWNLOAD, 100);
    }
}
