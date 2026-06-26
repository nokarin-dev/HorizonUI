package xyz.nokarin.api;

public record VersionInfo(String downloadUrl, String fileName, String versionNumber, String sha512, String sha1, String source) {
    public VersionInfo(String downloadUrl, String fileName, String versionNumber, String sha512) {
        this(downloadUrl, fileName, versionNumber, sha512, null, "unknown");
    }
}