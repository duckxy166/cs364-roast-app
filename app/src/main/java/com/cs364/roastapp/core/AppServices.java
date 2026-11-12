package com.cs364.roastapp.core;

import android.content.Context;

public final class AppServices {

    private static volatile AppDatabase database;
    private static volatile UsageSource usageSource;
    private static volatile RoastTrigger roastTrigger;
    private static volatile AlertThresholds alertThresholds;

    private AppServices() {
    }

    public static AppDatabase getDatabase(Context context) {
        if (database == null) {
            synchronized (AppServices.class) {
                if (database == null) {
                    database = AppDatabase.getInstance(context);
                }
            }
        }
        return database;
    }

    public static UsageSource getUsageSource(Context context) {
        if (usageSource == null) {
            synchronized (AppServices.class) {
                if (usageSource == null) {
                    usageSource = new FakeUsageSource();
                }
            }
        }
        return usageSource;
    }

    public static void setUsageSource(UsageSource source) {
        synchronized (AppServices.class) {
            usageSource = source;
        }
    }

    public static RoastTrigger getRoastTrigger(Context context) {
        if (roastTrigger == null) {
            synchronized (AppServices.class) {
                if (roastTrigger == null) {
                    roastTrigger = new com.cs364.roastapp.roast.RoastManager(context);
                }
            }
        }
        return roastTrigger;
    }

    public static void setRoastTrigger(RoastTrigger trigger) {
        synchronized (AppServices.class) {
            roastTrigger = trigger;
        }
    }

    public static AlertThresholds getAlertThresholds(Context context) {
        if (alertThresholds == null) {
            synchronized (AppServices.class) {
                if (alertThresholds == null) {
                    alertThresholds = new AlertThresholds(context);
                }
            }
        }
        return alertThresholds;
    }
}
