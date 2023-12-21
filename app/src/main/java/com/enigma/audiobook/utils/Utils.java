package com.enigma.audiobook.utils;

public class Utils {

    public static void addTryCatch(Runnable runnable, String logTag) {
        try {
            runnable.run();
        } catch (Exception e) {
            ALog.e(logTag, "got exception while invoking runnable", e);
        }
    }

    public static String convertMSToTime(int ms) {
        int sec = ms / 1000;
        int mins = sec / 60;
        int secToShow = sec % 60;
        return String.format("%02d:%02d", mins, secToShow);
    }
}
