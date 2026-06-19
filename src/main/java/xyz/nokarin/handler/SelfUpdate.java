package xyz.nokarin.handler;

import org.json.JSONArray;
import org.json.JSONObject;
import xyz.nokarin.util.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SelfUpdate {
    private static final String GITHUB_RELEASE_API = "https://api.github.com/repos/nokarin-dev/HorizonUI/releases/tags/updater";
    private static final String UPDATER_PREFIX = "HorizonUI.Updater-";
    private static final String USER_AGENT = "HorizonUI-Updater/1.0";

    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT = 20_000;

    public SelfUpdate() {
    }

    public void checkAndUpdateAsync() {
        Thread t = new Thread(() -> {
            try {
                checkAndUpdate();
            } catch (Exception e) {
                Logger.error("Self-update check failed", e);
            }
        }, "updater-self-update");
        t.setDaemon(true);
        t.start();
    }

    private void checkAndUpdate() throws Exception {
        Logger.info("Checking updater version...");

        File currentJar = resolveCurrentJar();
        if (currentJar == null) {
            Logger.info("Not running from a jar - skipping self-update");
            return;
        }

        GithubAsset latestAsset = fetchLatestAsset();
        if (latestAsset == null) {
            Logger.info("No updater asset found on GitHub");
            return;
        }

        String currentVersion = parseVersion(currentJar.getName());
        String latestVersion = parseVersion(latestAsset.name());

        Logger.info("Updater current: " + currentVersion + " | latest: " + latestVersion);

        if (compareVersions(currentVersion, latestVersion) >= 0) {
            Logger.info("Updater is up to date");
            return;
        }

        Logger.info("Updater outdated, downloading in background...");
        downloadAndReplace(latestAsset, currentJar);
        Logger.info("Updater updated to " + latestVersion);
    }

    private File resolveCurrentJar() {
        try {
            File f = new File(
                    SelfUpdate.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );
            if (f.isFile() && f.getName().endsWith(".jar")) return f;
        } catch (Exception e) {
            Logger.error("resolveCurrentJar failed", e);
        }
        return null;
    }

    private GithubAsset fetchLatestAsset() throws Exception {
        String json = fetchString();
        JSONObject release = new JSONObject(json);
        JSONArray assets = release.optJSONArray("assets");
        if (assets == null) return null;

        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            String name = asset.getString("name");
            if (name.startsWith(UPDATER_PREFIX) && name.endsWith(".jar")) {
                return new GithubAsset(name, asset.getString("browser_download_url"));
            }
        }
        return null;
    }

    private String fetchString() throws Exception {
        HttpURLConnection conn = openConnection(SelfUpdate.GITHUB_RELEASE_API);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("GitHub API returned: " + conn.getResponseCode());
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    private void downloadAndReplace(GithubAsset asset, File currentJar) throws Exception {
        File dir = currentJar.getParentFile();
        File tmp = new File(dir, asset.name() + ".tmp");

        HttpURLConnection conn = openConnection(asset.downloadUrl());
        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(tmp)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        } finally {
            conn.disconnect();
        }

        // Remove old updater jars
        File[] old = dir.listFiles((d, name) -> name.startsWith(UPDATER_PREFIX) && name.endsWith(".jar"));
        if (old != null) {
            for (File f : old) {
                if (!f.delete()) Logger.warn("Could not delete old updater: " + f.getName());
            }
        }

        Files.move(tmp.toPath(), new File(dir, asset.name()).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private HttpURLConnection openConnection(String urlString) throws Exception {
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setInstanceFollowRedirects(true);
        return conn;
    }

    private int compareVersions(String a, String b) {
        String[] pa = a.split("[.\\-]");
        String[] pb = b.split("[.\\-]");
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            String sa = i < pa.length ? pa[i] : "0";
            String sb = i < pb.length ? pb[i] : "0";
            boolean an = sa.matches("\\d+"), bn = sb.matches("\\d+");
            int cmp;
            if (an && bn) cmp = Integer.compare(Integer.parseInt(sa), Integer.parseInt(sb));
            else if (an) cmp = 1;
            else if (bn) cmp = -1;
            else cmp = sa.compareToIgnoreCase(sb);
            if (cmp != 0) return cmp;
        }
        return 0;
    }

    private String parseVersion(String fileName) {
        return fileName.replace(UPDATER_PREFIX, "").replace(".jar", "").trim();
    }

    private record GithubAsset(String name, String downloadUrl) {
    }
}
