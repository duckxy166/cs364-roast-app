package com.cs364.roastapp.tracking;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.cs364.roastapp.core.AppServices;

/** Starts / stops {@link UsageTrackingService} to match the tracked-app list and permissions. */
public final class TrackingServiceController {

    private static final String TAG = "TrackingController";

    private TrackingServiceController() {
    }

    /**
     * Makes the service state match what it should be: stopped when nothing is tracked, running
     * when there are tracked apps and usage access is granted.
     *
     * @return false only when the service should be running but could not be started
     */
    public static boolean sync(Context context) {
        Context app = context.getApplicationContext();
        if (AppServices.getDatabase(app).trackedAppDao().count() == 0) {
            app.stopService(new Intent(app, UsageTrackingService.class));
            return true;
        }
        if (!PermissionChecker.hasUsageAccess(app)) {
            return true; // nothing to monitor yet; the UI shows the missing-permission warning
        }
        if (UsageTrackingService.isRunning()) {
            return true;
        }
        try {
            ContextCompat.startForegroundService(app, new Intent(app, UsageTrackingService.class));
            return true;
        } catch (RuntimeException e) {
            Log.w(TAG, "could not start tracking service", e);
            return false;
        }
    }
}
