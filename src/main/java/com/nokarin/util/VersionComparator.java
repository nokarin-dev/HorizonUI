package com.nokarin.util;

public class VersionComparator {
    public static int compare(String version1, String version2) {
        String v1 = normalizeVersion(version1);
        String v2 = normalizeVersion(version2);
        
        String[] v1Parts = v1.split("\\.");
        String[] v2Parts = v2.split("\\.");
        
        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length ? parseVersionPart(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? parseVersionPart(v2Parts[i]) : 0;
            
            if (v1Part != v2Part) {
                return v1Part - v2Part;
            }
        }
        return 0;
    }

    public static String normalizeVersion(String version) {
        return version.replaceFirst("^v", "");
    }

    private static int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean isNewer(String version1, String version2) {
        return compare(version1, version2) > 0;
    }
}
