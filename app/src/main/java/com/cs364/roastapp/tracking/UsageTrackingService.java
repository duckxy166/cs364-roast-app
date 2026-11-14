package com.cs364.roastapp.tracking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AlertThresholds;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.HomeActivity;
import com.cs364.roastapp.core.RoastTrigger;
import com.cs364.roastapp.core.UsageSource;

import java.util.List;

/**
 * Foreground service that periodically checks whether the app in the foreground is a tracked app
 * that has used up its daily limit, and if so calls {@link RoastTrigger#onOverLimit}.
 *
 * <p>Runs on its own thread so it keeps working when the app's UI is closed. It stops itself when
 * there are no tracked apps left.
 */
public class UsageTrackingService extends Service {

    private static final String TAG = "UsageTrackingService";
    public static final String CHANNEL_ID = "usage_tracking";
    private static final int NOTIFICATION_ID = 7001;

    private static volatile boolean running;

    private HandlerThread workerThread;
    private Handler worker;
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean loopStarted;

    /** Package we last reported as over limit and have not yet reported as left. */
    @Nullable
    private String reportedOverPackage;

    public static boolean isRunning() {
        return running;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        workerThread = new HandlerThread("usage-tracking");
        workerThread.start();
        worker = new Handler(workerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        if (!loopStarted) {
            loopStarted = true;
            worker.post(this::tick);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        worker.removeCallbacksAndMessages(null);
        workerThread.quitSafely();
        super.onDestroy();
    }

    private void tick() {
        Context context = getApplicationContext();
        AlertThresholds thresholds = AppServices.getAlertThresholds(context);
        try {
            List<TrackedAppEntity> tracked = AppServices.getDatabase(context).trackedAppDao().getAll();
            if (tracked.isEmpty()) {
                stopSelf();
                return;
            }
            if (PermissionChecker.hasUsageAccess(context)) {
                check(context, tracked);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "check failed", e);
        }
        worker.postDelayed(this::tick, thresholds.getCheckIntervalMillis());
    }

    private void check(Context context, List<TrackedAppEntity> tracked) {
        UsageSource source = AppServices.getUsageSource(context);
        RoastTrigger trigger = AppServices.getRoastTrigger(context);

        String foreground = source.getForegroundPackage();
        TrackedAppEntity current = find(tracked, foreground);

        long millisOver = Long.MIN_VALUE;
        if (current != null) {
            millisOver = source.getUsedMillisToday(foreground) - current.limitMinutes * 60_000L;
        }
        boolean overNow = current != null && millisOver >= 0L;

        // Left the over-limit app (or the limit was raised): tell the trigger once, then go quiet.
        if (reportedOverPackage != null && !(overNow && reportedOverPackage.equals(foreground))) {
            String left = reportedOverPackage;
            reportedOverPackage = null;
            main.post(() -> trigger.onLeftApp(left));
        }

        if (overNow) {
            reportedOverPackage = foreground;
            long over = millisOver;
            main.post(() -> trigger.onOverLimit(foreground, over));
        }
    }

    @Nullable
    private static TrackedAppEntity find(List<TrackedAppEntity> tracked, @Nullable String packageName) {
        if (packageName == null) {
            return null;
        }
        for (TrackedAppEntity entity : tracked) {
            if (entity.packageName.equals(packageName)) {
                return entity;
            }
        }
        return null;
    }

    private Notification buildNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null
                && manager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.tracking_service_channel), NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            manager.createNotificationChannel(channel);
        }
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.pid_ic_notification)
                .setContentTitle(getString(R.string.tracking_service_title))
                .setContentText(getString(R.string.tracking_service_text))
                .setContentIntent(open)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }
}
