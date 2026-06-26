package xyz.nokarin.api;

import xyz.nokarin.util.Logger;

public class SourceResolver {
    private final ModrinthAPI modrinth;
    private final GitHubReleaseAPI gitHub;

    public SourceResolver() {
        this.modrinth = new ModrinthAPI();
        this.gitHub = new GitHubReleaseAPI();
    }

    public VersionInfo findLatestVersion(String loader, String mcVersion, String versionState) {
        VersionInfo result;

        result = trySource("Modrinth", () -> modrinth.findLatestVersion(loader, mcVersion, versionState));
        if (result != null) return result;

        result = trySource("GitHub", () -> gitHub.findLatestVersion(loader, mcVersion, versionState));
        return result;
    }

    private VersionInfo trySource(String name, SourceCall call) {
        try {
            Logger.info("Trying " + name + "...");
            VersionInfo info = call.get();
            if (info != null) {
                Logger.info("Found version on " + name + ": " + info.versionNumber());
                return info;
            }
            Logger.info(name + ": no compatible version found");
        } catch (Exception e) {
            Logger.warn(name + " failed: " + e.getMessage());
        }
        return null;
    }

    @FunctionalInterface
    private interface SourceCall {
        VersionInfo get() throws Exception;
    }
}