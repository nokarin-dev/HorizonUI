package xyz.nokarin.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String PREFIX = "[HorizonUI Updater]";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void info(String message) {
        System.out.println(timestamp() + " " + PREFIX + " " + message);
    }

    public static void warn(String message) {
        System.err.println(timestamp() + " " + PREFIX + " [WARN] " + message);
    }

    public static void error(String message) {
        System.err.println(timestamp() + " " + PREFIX + " [ERROR] " + message);
    }

    public static void error(String message, Throwable t) {
        System.err.println(timestamp() + " " + PREFIX + " [ERROR] " + message);
        t.printStackTrace();
    }

    private static String timestamp() {
        return LocalTime.now().format(FMT);
    }
}
