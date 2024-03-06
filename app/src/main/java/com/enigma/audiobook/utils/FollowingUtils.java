package com.enigma.audiobook.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;

import com.enigma.audiobook.R;

public class FollowingUtils {
    private static final String TAG = "FollowingUtils";

    public static boolean setToFollowing(Button followBtn, LinearLayout followBtnLL) {
        followBtn.setBackgroundColor(Color.parseColor("#B9DAAE"));
        if (followBtnLL != null) {
            followBtnLL.setBackgroundColor(Color.parseColor("#B9DAAE"));
        }
        ALog.i("Following utils", "color of followBtn:" + followBtn.getBackground());

        followBtn.setText("FOLLOWING");
        followBtn.setTextColor(Color.parseColor("#FF000000"));
        return true;
    }

    public static boolean setToNotFollowing(Button followBtn, LinearLayout followBtnLL) {
        followBtn.setBackgroundColor(Color.parseColor("#C2AAF1"));
        if (followBtnLL != null) {
//            followBtnLL.setBackgroundColor(Color.parseColor("#C2AAF1"));
        }

        followBtn.setTextColor(Color.parseColor("#FF000000"));
        followBtn.setText("FOLLOW");
        return false;
    }
}
