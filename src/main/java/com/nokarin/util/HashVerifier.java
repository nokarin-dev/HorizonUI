package com.nokarin.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class HashVerifier {
    public static boolean verifySHA512(File file, String expected) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = fis.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString().equalsIgnoreCase(expected);
    }
}
