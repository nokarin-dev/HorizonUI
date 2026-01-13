package com.nokarin.util;

public class Logger {
    private static final String PREFIX = "[HorizonUI Updater]";
    
    public static void info(String message) {
        System.out.println(PREFIX + " " + message);
    }
    
    public static void error(String message) {
        System.err.println(PREFIX + " " + message);
    }
    
    public static void error(String message, Throwable throwable) {
        System.err.println(PREFIX + " " + message);
        throwable.printStackTrace();
    }
}
