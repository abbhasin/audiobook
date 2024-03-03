package com.enigma.audiobook.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;

import com.enigma.audiobook.R;

public class FollowingUtils {
    private static final String TAG = "FollowingUtils";

    public static boolean setToFollowing(Button followBtn, LinearLayout followBtnLL) {
        followBtn.setBackgroundColor(0xFFB0ECE6);
        if (followBtnLL != null) {
            followBtnLL.setBackgroundColor(0xFFB0ECE6);
        }

        followBtn.setText("FOLLOWING");
        followBtn.setTextColor(Color.parseColor("#CE9033"));
        return true;
    }

    public static boolean setToNotFollowing(Button followBtn, LinearLayout followBtnLL) {
        followBtn.setBackgroundColor(0xFFDFD1FA);
        if (followBtnLL != null) {
            followBtnLL.setBackgroundColor(0xFFDFD1FA);
        }

        followBtn.setTextColor(Color.parseColor("#3BC0B2"));
        followBtn.setText("FOLLOW");
        return false;
    }
}
