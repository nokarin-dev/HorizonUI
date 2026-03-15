package com.nokarin.handler;

import com.nokarin.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileHandler {
    public boolean ensureModsDirectory(String modsPath) {
        File dir = new File(modsPath);
        if (!dir.exists()) {
            Logger.info("Creating mods directory");
            return dir.mkdirs();
        }
        return true;
    }

    public void deleteOldVersions(String modsPath) {
        File dir = new File(modsPath);

        File[] files = dir.listFiles((d, name) ->
                name.toLowerCase().startsWith("horizonui")
                        && name.toLowerCase().endsWith(".jar")
        );

        if (files == null) return;

        for (File file : files) {
            Logger.info("Deleting old HorizonUI file: " + file.getName());
            if (!file.delete()) {
                Logger.error("Failed to delete: " + file.getName());
            }
        }
    }
}
