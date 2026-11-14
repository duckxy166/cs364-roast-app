package com.cs364.roastapp.onboarding;

import android.content.Context;
import android.content.SharedPreferences;

/** Remembers that the first-run onboarding has been completed. */
public final class OnboardingPrefs {

    private static final String PREFS = "onboarding";
    private static final String KEY_DONE = "done";
    private static final String KEY_NOTIFICATION_ASKED = "notification_asked";

    private OnboardingPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isDone(Context context) {
        return prefs(context).getBoolean(KEY_DONE, false);
    }

    public static void markDone(Context context) {
        prefs(context).edit().putBoolean(KEY_DONE, true).apply();
    }

    static boolean notificationAsked(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFICATION_ASKED, false);
    }

    static void markNotificationAsked(Context context) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATION_ASKED, true).apply();
    }
}
