package com.enigma.audiobook.utils;

import android.util.Log;

public class ALog {

    public static void i(String tag, String msg) {
        Log.i(tag, String.format("[Thread-%s] %s", Thread.currentThread().getName(), msg));
    }

    public static void e(String tag, String msg, Throwable e) {
        Log.e(tag, String.format("[Thread-%s] %s", Thread.currentThread().getName(), msg), e);
    }
}
