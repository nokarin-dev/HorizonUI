package xyz.nokarin.api;

import org.json.JSONArray;
import org.json.JSONObject;
import xyz.nokarin.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class GitHubReleaseAPI {
    private static final String USER_AGENT = "HorizonUI-Updater/1.0";

    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT = 20_000;

    public VersionInfo findLatestVersion(String loader, String mcVersion, String versionState) throws Exception {
        JSONArray releases = new JSONArray(fetchURL());

        for (int i = 0; i < releases.length(); i++) {
            JSONObject release = releases.getJSONObject(i);

            if (release.optBoolean("draft", false)) continue;

            boolean isPrerelease = release.optBoolean("prerelease", false);
            if (versionState.equalsIgnoreCase("release") && isPrerelease) continue;

            String tagName = release.getString("tag_name");
            String versionNumber = tagName.startsWith("v") ? tagName.substring(1) : tagName;

            JSONArray assets = release.optJSONArray("assets");
            if (assets == null || assets.isEmpty()) continue;

            JSONObject asset = findMatchingAsset(assets, loader, mcVersion);
            if (asset == null) continue;

            Logger.info("GitHub release match: " + asset.getString("name") + " in " + tagName);

            return new VersionInfo(
                    asset.getString("browser_download_url"),
                    asset.getString("name"),
                    versionNumber,
                    null,
                    null,
                    "GitHub"
            );
        }

        return null;
    }

    private JSONObject findMatchingAsset(JSONArray assets, String loader, String mcVersion) {
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            String name = asset.getString("name").toLowerCase();

            if (!name.endsWith(".jar")) continue;
            if (!name.startsWith("horizonui-")) continue;
            if (!name.contains(mcVersion.toLowerCase())) continue;
            if (!name.contains(loader.toLowerCase())) continue;

            return asset;
        }
        return null;
    }

    private String fetchURL() throws Exception {
        HttpURLConnection conn = openConnection("https://api.github.com/repos/nokarin-dev/HorizonUI/releases?per_page=10");
        try {
            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == 307 || status == 308) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                conn = openConnection(location);
            }
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("GitHub API returned " + conn.getResponseCode());
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    private HttpURLConnection openConnection(String urlString) throws Exception {
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setInstanceFollowRedirects(true);
        return conn;
    }
}