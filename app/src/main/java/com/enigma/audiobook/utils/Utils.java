package com.enigma.audiobook.utils;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.enigma.audiobook.R;

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

    public static RequestManager initGlide(Context context) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.background_card_view)
                .error(R.drawable.background_card_view);

        return Glide.with(context)
                .setDefaultRequestOptions(options);
    }
}
