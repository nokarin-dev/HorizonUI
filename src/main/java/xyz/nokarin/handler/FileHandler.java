package xyz.nokarin.handler;

import xyz.nokarin.util.Logger;

import java.io.File;
import java.util.regex.Pattern;

public class FileHandler {
    private static final Pattern HORIZONUI_JAR = Pattern.compile(
            "(?i)^horizonui-[\\w.\\-]+-(?:fabric|forge|neoforge|quilt)\\.jar$"
    );

    public boolean ensureModsDirectory(String modsPath) {
        File dir = new File(modsPath);
        if (!dir.exists()) {
            Logger.info("Creating mods directory: " + modsPath);
            return dir.mkdirs();
        }
        return true;
    }

    public void deleteOldVersions(String modsPath) {
        File dir = new File(modsPath);
        File[] files = dir.listFiles((d, name) -> HORIZONUI_JAR.matcher(name).matches());
        if (files == null) return;

        for (File file : files) {
            Logger.info("Removing old version: " + file.getName());
            if (!file.delete()) {
                Logger.error("Could not delete: " + file.getName());
            }
        }
    }
}
