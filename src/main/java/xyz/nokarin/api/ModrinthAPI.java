package xyz.nokarin.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ModrinthAPI {
    private static final String API_BASE = "https://api.modrinth.com/v2";
    private static final String PROJECT_ID = "90mpuiZs";
    private static final String USER_AGENT = "HorizonUI-Updater/1.0";

    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT = 20_000;

    public VersionInfo findLatestVersion(String loader, String mcVersion, String versionState) throws Exception {
        String url = API_BASE + "/project/" + PROJECT_ID + "/version";
        JSONArray versions = new JSONArray(fetchURL(url));

        for (int i = 0; i < versions.length(); i++) {
            JSONObject version = versions.getJSONObject(i);

            String versionType = version.getString("version_type");
            if (versionState.equalsIgnoreCase("release") && !versionType.equals("release")) continue;

            if (!isLoaderCompatible(version, loader)) continue;
            if (!isGameVersionCompatible(version, mcVersion)) continue;

            JSONArray files = version.getJSONArray("files");
            if (files.isEmpty()) continue;

            JSONObject file = primaryFile(files);
            return new VersionInfo(
                    file.getString("url"),
                    file.getString("filename"),
                    version.getString("version_number"),
                    file.getJSONObject("hashes").getString("sha512")
            );
        }

        return null;
    }

    private JSONObject primaryFile(JSONArray files) {
        for (int i = 0; i < files.length(); i++) {
            JSONObject f = files.getJSONObject(i);
            if (f.optBoolean("primary", false)) return f;
        }
        return files.getJSONObject(0);
    }

    private boolean isLoaderCompatible(JSONObject version, String loader) {
        JSONArray loaders = version.getJSONArray("loaders");
        for (int i = 0; i < loaders.length(); i++) {
            if (loaders.getString(i).equalsIgnoreCase(loader)) return true;
        }
        return false;
    }

    private boolean isGameVersionCompatible(JSONObject version, String mcVersion) {
        JSONArray gameVersions = version.getJSONArray("game_versions");
        for (int i = 0; i < gameVersions.length(); i++) {
            if (gameVersions.getString(i).equals(mcVersion)) return true;
        }
        return false;
    }

    private String fetchURL(String urlString) throws Exception {
        HttpURLConnection conn = openConnection(urlString);
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
                throw new IOException("Modrinth API returned " + conn.getResponseCode());
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

    private HttpURLConnection openConnection(String urlString) throws Exception {
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setInstanceFollowRedirects(true);
        return conn;
    }
}