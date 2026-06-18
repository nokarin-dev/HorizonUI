package xyz.nokarin.handler;

import xyz.nokarin.util.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SelfUpdate {
    private static final String GITHUB_RELEASE_API = "https://api.github.com/repos/nokarin-dev/HorizonUI/releases/tags/updater";
    private static final String UPDATER_PREFIX = "HorizonUI.Updater-";
    private static final String USER_AGENT = "HorizonUI-Updater/1.0";

    public SelfUpdate() {}

    public void checkAndUpdateAsync() {
        Thread thread = new Thread(() -> {
            try {
                checkAndUpdate();
            } catch (Exception e) {
                Logger.error("Failed to self-update updater", e);
            }
        }, "updater-self-update");

        thread.setDaemon(true);
        thread.start();
    }

    private void checkAndUpdate() throws Exception {
        Logger.info("Checking updater version...");

        File currentJar = resolveCurrentJar();
        if (currentJar == null) {
            Logger.info("Could not resolve current jar path, skipping self-update");
            return;
        }

        GithubAsset latestAsset = fetchLatestAsset();
        if (latestAsset == null) {
            Logger.info("No updater asset found on GitHub");
            return;
        }

        String latestVersion  = parseVersion(latestAsset.name());
        String currentVersion = parseVersion(currentJar.getName());

        Logger.info("Updater current: " + currentVersion + " | latest: " + latestVersion);

        if (compareVersions(currentVersion, latestVersion) >= 0) {
            Logger.info("Updater is already up to date");
            return;
        }

        Logger.info("Updater outdated, downloading in background...");
        downloadAndReplace(latestAsset, currentJar);
        Logger.info("Updater updated to " + latestVersion);
    }

    private File resolveCurrentJar() {
        try {
            File f = new File(
                    SelfUpdate.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );
            if (f.isFile() && f.getName().endsWith(".jar")) {
                return f;
            }
        } catch (Exception e) {
            Logger.error("resolveCurrentJar failed", e);
        }
        return null;
    }

    private GithubAsset fetchLatestAsset() throws Exception {
        String json = getJson();

        int assetsStart = json.indexOf("\"assets\"");
        if (assetsStart == -1) return null;

        int searchFrom = assetsStart;
        while (true) {
            int nameIdx = json.indexOf("\"name\"", searchFrom);
            if (nameIdx == -1) break;

            int nameStart = json.indexOf("\"", nameIdx + 7) + 1;
            int nameEnd = json.indexOf("\"", nameStart);
            String name = json.substring(nameStart, nameEnd);

            if (name.startsWith(UPDATER_PREFIX) && name.endsWith(".jar")) {
                int urlIdx = json.indexOf("\"browser_download_url\"", nameIdx);
                int urlStart = json.indexOf("\"", urlIdx + 23) + 1;
                int urlEnd = json.indexOf("\"", urlStart);
                String downloadUrl = json.substring(urlStart, urlEnd);

                return new GithubAsset(name, downloadUrl);
            }

            searchFrom = nameIdx + 1;
        }

        return null;
    }

    private static String getJson() throws URISyntaxException, IOException {
        URL url = new URI(GITHUB_RELEASE_API).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "application/vnd.github+json");

        if (conn.getResponseCode() != 200) {
            throw new IOException("GitHub API returned: " + conn.getResponseCode());
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        return sb.toString();
    }

    private void downloadAndReplace(GithubAsset asset, File currentJar) throws Exception {
        File parentDir = currentJar.getParentFile();

        File tempFile = downloadToTemp(asset, parentDir);

        File[] oldFiles = parentDir.listFiles((d, name) ->
                name.startsWith(UPDATER_PREFIX) && name.endsWith(".jar"));

        if (oldFiles != null) {
            for (File old : oldFiles) {
                if (!old.delete()) {
                    Logger.error("Failed to delete old updater: " + old.getName());
                }
            }
        }

        File finalFile = new File(parentDir, asset.name());
        Files.move(tempFile.toPath(), finalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static File downloadToTemp(GithubAsset asset, File dir) throws URISyntaxException, IOException {
        File tempFile = new File(dir, asset.name() + ".tmp");

        URL url = new URI(asset.downloadUrl()).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);

        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return tempFile;
    }

    private int compareVersions(String a, String b) {
        String[] partsA = a.split("[.-]");
        String[] partsB = b.split("[.-]");

        int len = Math.max(partsA.length, partsB.length);
        for (int i = 0; i < len; i++) {
            String pa = i < partsA.length ? partsA[i] : "0";
            String pb = i < partsB.length ? partsB[i] : "0";

            boolean aIsNum = pa.matches("\\d+");
            boolean bIsNum = pb.matches("\\d+");

            int cmp;
            if (aIsNum && bIsNum) {
                cmp = Integer.compare(Integer.parseInt(pa), Integer.parseInt(pb));
            } else if (aIsNum) {
                cmp = 1;  // numeric > alpha (release > pre-release)
            } else if (bIsNum) {
                cmp = -1;
            } else {
                cmp = pa.compareToIgnoreCase(pb);
            }

            if (cmp != 0) return cmp;
        }
        return 0;
    }

    private String parseVersion(String fileName) {
        return fileName
                .replace(UPDATER_PREFIX, "")
                .replace(".jar", "")
                .trim();
    }

    private record GithubAsset(String name, String downloadUrl) {}
}
