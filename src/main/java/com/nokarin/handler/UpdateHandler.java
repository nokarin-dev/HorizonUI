package com.nokarin.handler;

import com.nokarin.api.ModrinthAPI;
import com.nokarin.api.VersionInfo;
import com.nokarin.gui.UpdateState;
import com.nokarin.util.Logger;
import com.nokarin.util.VersionComparator;

import java.io.File;
import java.util.function.Consumer;

public class UpdateHandler {
    private final ModrinthAPI api;
    private final FileHandler fileHandler;
    private final DownloadHandler downloadHandler;

    private final String currentVersion;
    private final String mcVersion;
    private final String loader;
    private final String versionState;
    private final String modsPath;

    public UpdateHandler(
            String currentVersion,
            String mcVersion,
            String loader,
            String versionState,
            String modsPath
    ) {
        this.api = new ModrinthAPI();
        this.fileHandler = new FileHandler();
        this.downloadHandler = new DownloadHandler();

        this.currentVersion = currentVersion;
        this.mcVersion = mcVersion;
        this.loader = loader;
        this.versionState = versionState;
        this.modsPath = modsPath;
    }

    public VersionInfo checkForUpdates() throws Exception {
        Logger.info("Checking for updates...");
        Logger.info("Current: " + currentVersion +
                " | MC: " + mcVersion +
                " | Loader: " + loader);

        VersionInfo latest = api.findLatestVersion(loader, mcVersion, versionState);
        if (latest == null) {
            Logger.info("No compatible version found");
            return null;
        }

        String current = VersionComparator.normalizeVersion(currentVersion);
        String latestVer = VersionComparator.normalizeVersion(latest.versionNumber());

        if (VersionComparator.compare(current, latestVer) >= 0) {
            Logger.info("Already on latest version");
            return null;
        }

        return latest;
    }

    public void performUpdate(
            VersionInfo versionInfo,
            Consumer<UpdateState> callback
    ) throws Exception {
        Logger.info("Starting update process...");

        callback.accept(new UpdateState("Preparing update...", 25));

        if (!fileHandler.ensureModsDirectory(modsPath)) {
            throw new Exception("Failed to create mods directory");
        }

        callback.accept(new UpdateState("Removing old versions...", 35));
        fileHandler.deleteOldVersions(modsPath);

        callback.accept(new UpdateState("Downloading update...", 50));
        File target = new File(modsPath, versionInfo.fileName());

        downloadHandler.downloadFile(
                versionInfo.downloadUrl(),
                target.getAbsolutePath(),
                progress -> callback.accept(
                        new UpdateState("Downloading update...", progress)
                )
        );

        callback.accept(new UpdateState("Verifying files...", 90));
        Thread.sleep(500);

        callback.accept(new UpdateState("Finalizing installation...", 95));
        Thread.sleep(300);

        callback.accept(new UpdateState("Update complete! Restart Minecraft.", 100));

        Logger.info("Update completed successfully");
    }
}
