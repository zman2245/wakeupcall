package com.bigzindustries.wakeupcall.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsWrapper {

    private static String PREFS_NAME = "FriendAlertDefaultPrefs";

    public static boolean isSmsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("sms_enabled", false) && isGlobalEnabled(context);
    }

    public static void enableSms(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("sms_enabled", enabled)
                .commit();
    }

    /**
     * Returns true if no setting yet exists for global state or its value if it does exist.
     * I.e. the app is "ON" by default. Does not account for any rules around permissioning.
     *
     * @param context
     * @return
     */
    public static boolean isGlobalEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.contains("global_enabled")) {
            return true;
        }

        return prefs.getBoolean("global_enabled", true);
    }

    public static void enableGlobal(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("global_enabled", enabled)
                .commit();
    }
}
