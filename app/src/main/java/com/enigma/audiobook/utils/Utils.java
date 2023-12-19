package com.enigma.audiobook.utils;

public class Utils {

    public static void addTryCatch(Runnable runnable, String logTag) {
        try {
            runnable.run();
        } catch (Exception e) {
            ALog.e(logTag, "got exception while invoking runnable", e);
        }
    }
}
