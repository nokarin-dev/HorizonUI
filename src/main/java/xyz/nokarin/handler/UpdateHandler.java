package xyz.nokarin.handler;

import xyz.nokarin.api.SourceResolver;
import xyz.nokarin.api.VersionInfo;
import xyz.nokarin.gui.UpdateState;
import xyz.nokarin.util.*;

import java.io.File;
import java.util.function.Consumer;

public class UpdateHandler {
    private final SourceResolver resolver;
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
        this.resolver = new SourceResolver();
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
        Logger.info("Current: " + currentVersion + " | MC: " + mcVersion + " | Loader: " + loader);

        VersionInfo latest = resolver.findLatestVersion(loader, mcVersion, versionState);
        if (latest == null) {
            Logger.info("No compatible version found on any source");
            return null;
        }

        File existingFile = new File(modsPath, latest.fileName());
        if (existingFile.exists()) {
            Logger.info("Already up to date: " + latest.fileName());
            return null;
        }

        Logger.info("Update found via " + latest.source() + ": " + latest.versionNumber()
                + " (" + latest.fileName() + ")");
        return latest;
    }

    public void performUpdate(VersionInfo versionInfo, Consumer<UpdateState> callback) throws Exception {
        ProgressCalculator calc = new ProgressCalculator();

        fileHandler.ensureModsDirectory(modsPath);
        calc.update(UpdateStage.PREPARE, 100);
        callback.accept(new UpdateState("Preparing update...", calc.getTotalProgress()));

        fileHandler.deleteOldVersions(modsPath);
        calc.update(UpdateStage.DELETE_OLD, 100);
        callback.accept(new UpdateState("Removed old version files", calc.getTotalProgress()));

        File target = new File(modsPath, versionInfo.fileName());
        String sourceLabel = versionInfo.source() != null ? " [" + versionInfo.source() + "]" : "";
        callback.accept(new UpdateState("Downloading" + sourceLabel + "...", calc.getTotalProgress()));
        downloadHandler.downloadFile(versionInfo.downloadUrl(), target.getAbsolutePath(), calc, callback);

        calc.update(UpdateStage.VERIFY, 50);
        callback.accept(new UpdateState("Verifying file integrity...", calc.getTotalProgress(), true));

        if (!verifyIntegrity(target, versionInfo)) {
            target.delete();
            throw new Exception("Integrity check failed — file may be corrupt");
        }

        calc.update(UpdateStage.VERIFY, 100);
        calc.update(UpdateStage.FINALIZE, 100);
        callback.accept(new UpdateState("Finalizing...", calc.getTotalProgress()));

        Logger.info("Update completed successfully via " + versionInfo.source());
    }

    private boolean verifyIntegrity(File file, VersionInfo info) throws Exception {
        if (info.sha512() != null && !info.sha512().isBlank()) {
            Logger.info("Verifying SHA-512...");
            return HashVerifier.verifySHA512(file, info.sha512());
        }
        if (info.sha1() != null && !info.sha1().isBlank()) {
            Logger.info("Verifying SHA-1...");
            return HashVerifier.verifySHA1(file, info.sha1());
        }
        Logger.warn("No hash available for " + info.source() + " - skipping integrity check");
        return true;
    }
}