package com.nokarin.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ModrinthAPI {
    private static final String API_BASE = "https://api.modrinth.com/v2";
    private static final String PROJECT_ID = "90mpuiZs";
    private static final String USER_AGENT = "HorizonUI-Updater/1.0";

    public JSONArray fetchVersions() throws Exception {
        String url = API_BASE + "/project/" + PROJECT_ID + "/version";
        String response = fetchURL(url);
        return new JSONArray(response);
    }

    public VersionInfo findLatestVersion(String loader, String mcVersion, String versionState) throws Exception {
        JSONArray versions = fetchVersions();
        
        for (int i = 0; i < versions.length(); i++) {
            JSONObject version = versions.getJSONObject(i);
            
            // Check version type (release/beta)
            String versionType = version.getString("version_type");
            if (versionState.equalsIgnoreCase("release") && !versionType.equals("release")) {
                continue;
            }
            
            // Check loaders
            if (!isLoaderCompatible(version, loader)) {
                continue;
            }
            
            // Check game versions
            if (!isGameVersionCompatible(version, mcVersion)) {
                continue;
            }
            
            // Get download information
            JSONArray files = version.getJSONArray("files");
            if (!files.isEmpty()) {
                JSONObject file = files.getJSONObject(0);
                String downloadUrl = file.getString("url");
                String fileName = file.getString("filename");
                String versionNumber = version.getString("version_number");
                return new VersionInfo(downloadUrl, fileName, versionNumber);
            }
        }
        
        return null;
    }

    private boolean isLoaderCompatible(JSONObject version, String loader) {
        JSONArray loaders = version.getJSONArray("loaders");
        for (int i = 0; i < loaders.length(); i++) {
            if (loaders.getString(i).equalsIgnoreCase(loader)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGameVersionCompatible(JSONObject version, String mcVersion) {
        JSONArray gameVersions = version.getJSONArray("game_versions");
        for (int i = 0; i < gameVersions.length(); i++) {
            if (gameVersions.getString(i).equals(mcVersion)) {
                return true;
            }
        }
        return false;
    }

    private String fetchURL(String urlString) throws Exception {
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return response.toString();
    }
}
