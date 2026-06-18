package xyz.nokarin.handler;

import xyz.nokarin.api.ModrinthAPI;
import xyz.nokarin.api.VersionInfo;
import xyz.nokarin.gui.UpdateState;
import xyz.nokarin.util.*;
import xyz.nokarin.util.HashVerifier;
import xyz.nokarin.util.Logger;
import xyz.nokarin.util.ProgressCalculator;
import xyz.nokarin.util.UpdateStage;

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

        File existingFile = new File(modsPath, latest.fileName());
        if (existingFile.exists()) {
            Logger.info("Already up to date: " + latest.fileName());
            return null;
        }

        Logger.info("Update found: " + latest.versionNumber());
        return latest;
    }

    public void performUpdate(
            VersionInfo versionInfo,
            Consumer<UpdateState> callback
    ) throws Exception {
        ProgressCalculator calculator = new ProgressCalculator();

        fileHandler.ensureModsDirectory(modsPath);
        calculator.update(UpdateStage.PREPARE, 100);
        callback.accept(new UpdateState(
                "Preparing update...",
                calculator.getTotalProgress(),
                0,
                0
        ));

        fileHandler.deleteOldVersions(modsPath);
        calculator.update(UpdateStage.DELETE_OLD, 100);
        callback.accept(new UpdateState(
                "Removing old versions...",
                calculator.getTotalProgress(),
                0,
                0
        ));

        File target = new File(modsPath, versionInfo.fileName());
        downloadHandler.downloadFile(
                versionInfo.downloadUrl(),
                target.getAbsolutePath(),
                calculator,
                callback
        );

        calculator.update(UpdateStage.VERIFY, 50);
        callback.accept(new UpdateState(
                "Verifying file integrity...",
                calculator.getTotalProgress(),
                0,
                0
        ));

        if (!HashVerifier.verifySHA512(target, versionInfo.sha512())) {
            target.delete();
            throw new Exception("Integrity check failed");
        }

        calculator.update(UpdateStage.VERIFY, 100);

        calculator.update(UpdateStage.FINALIZE, 100);
        callback.accept(new UpdateState(
                "Finalizing...",
                calculator.getTotalProgress(),
                0,
                0
        ));

        Logger.info("Update completed successfully.");
    }
}
