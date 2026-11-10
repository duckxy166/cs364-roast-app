package com.cs364.roastapp.core;

import android.content.Context;
import android.content.SharedPreferences;

public class AlertThresholds {

    public static final String PREFS_NAME = "roast_app_settings";
    public static final String KEY_DEMO_MODE = "demo_mode";

    public static final long LEVEL_1_OVER_MILLIS = 0L;

    public static final long NORMAL_LEVEL_2_OVER_MILLIS = 5L * 60L * 1000L;
    public static final long NORMAL_LEVEL_3_OVER_MILLIS = 10L * 60L * 1000L;
    public static final long NORMAL_REPEAT_VOICE_INTERVAL_MILLIS = 5L * 60L * 1000L;
    public static final long NORMAL_CHECK_INTERVAL_MILLIS = 30L * 1000L;

    public static final long DEMO_LEVEL_2_OVER_MILLIS = 10L * 1000L;
    public static final long DEMO_LEVEL_3_OVER_MILLIS = 20L * 1000L;
    public static final long DEMO_REPEAT_VOICE_INTERVAL_MILLIS = 20L * 1000L;
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

    public long getLevel1OverMillis() {
        return LEVEL_1_OVER_MILLIS;
    }

    public long getLevel2OverMillis() {
        return isDemoMode() ? DEMO_LEVEL_2_OVER_MILLIS : NORMAL_LEVEL_2_OVER_MILLIS;
    }

    public long getLevel3OverMillis() {
        return isDemoMode() ? DEMO_LEVEL_3_OVER_MILLIS : NORMAL_LEVEL_3_OVER_MILLIS;
    }

    public long getRepeatVoiceIntervalMillis() {
        return isDemoMode()
                ? DEMO_REPEAT_VOICE_INTERVAL_MILLIS
                : NORMAL_REPEAT_VOICE_INTERVAL_MILLIS;
    }

    public long getCheckIntervalMillis() {
        return isDemoMode() ? DEMO_CHECK_INTERVAL_MILLIS : NORMAL_CHECK_INTERVAL_MILLIS;
    }

    public int getAlertLevel(long millisOver) {
        if (millisOver < LEVEL_1_OVER_MILLIS) {
            return 0;
        }
        if (millisOver >= getLevel3OverMillis()) {
            return 3;
        }
        if (millisOver >= getLevel2OverMillis()) {
            return 2;
        }
        return 1;
    }
}
