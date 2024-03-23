package com.enigma.audiobook.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Optional;

public class SharedPreferencesHandler {
    private static final String SHARED_PREFERENCE_FILE = "OneGodSharedPreferences";
    private static final String USER_ID_KEY = "userId";

    public static void addUserId(Context context, String userId) {

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(USER_ID_KEY, userId);
        editor.apply();
    }

    public static Optional<String> getUserId(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        return Optional.ofNullable(sharedPreferences.getString(USER_ID_KEY, null));
    }
}
