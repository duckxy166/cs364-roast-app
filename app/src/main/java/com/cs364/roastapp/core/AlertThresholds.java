package com.cs364.roastapp.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Timing for over-limit reminders. There is a single kind of reminder: it fires the moment an
 * app goes over its daily limit, then repeats on an interval while that app stays in front.
 */
public class AlertThresholds {

    public static final String PREFS_NAME = "roast_app_settings";
    public static final String KEY_DEMO_MODE = "demo_mode";

    public static final long NORMAL_REPEAT_INTERVAL_MILLIS = 5L * 60L * 1000L;
    public static final long NORMAL_CHECK_INTERVAL_MILLIS = 30L * 1000L;

    public static final long DEMO_REPEAT_INTERVAL_MILLIS = 20L * 1000L;
    public static final long DEMO_CHECK_INTERVAL_MILLIS = 5L * 1000L;

    private final SharedPreferences prefs;

    public AlertThresholds(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDemoMode() {
        return prefs.getBoolean(KEY_DEMO_MODE, false);
    }

    public void setDemoMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DEMO_MODE, enabled).apply();
    }

    /** Time between reminders while the user keeps using an app that is over its limit. */
    public long getRepeatIntervalMillis() {
        return isDemoMode() ? DEMO_REPEAT_INTERVAL_MILLIS : NORMAL_REPEAT_INTERVAL_MILLIS;
    }

    public long getCheckIntervalMillis() {
        return isDemoMode() ? DEMO_CHECK_INTERVAL_MILLIS : NORMAL_CHECK_INTERVAL_MILLIS;
    }
}
