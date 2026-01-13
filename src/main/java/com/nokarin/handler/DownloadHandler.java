package com.nokarin.handler;

import com.nokarin.util.Logger;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.IntConsumer;

public class DownloadHandler {
    private static final int BUFFER_SIZE = 8192;

    public void downloadFile(
            String fileUrl,
            String outputPath,
            IntConsumer progressCallback
    ) throws Exception {
        Logger.info("Downloading file from: " + fileUrl);

        URL url = new URL(fileUrl);
        URLConnection connection = url.openConnection();
        int contentLength = connection.getContentLength();

        try (
                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                FileOutputStream out = new FileOutputStream(outputPath)
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long downloaded = 0;
            int read;

            while ((read = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, read);
                downloaded += read;

                if (contentLength > 0) {
                    int progress = (int) ((downloaded * 100) / contentLength);
                    progressCallback.accept(progress);
                }
            }
        }

        progressCallback.accept(100);
        Logger.info("Download completed");
    }
}
