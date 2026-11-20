package com.cs364.roastapp.tracking;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AlertThresholds;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.HomeActivity;
import com.cs364.roastapp.core.RoastTrigger;
import com.cs364.roastapp.core.UsageSource;
import com.cs364.roastapp.settings.SettingsManager;

import java.util.List;
import java.util.Locale;

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
    /** Sent by the alarm that wakes the service for its next check. */
    private static final String ACTION_TICK = "com.cs364.roastapp.tracking.TICK";
    private static final int TICK_REQUEST_CODE = 7002;
    /** Ignore back-to-back ticks (for example fallback Handler + delayed alarm waking together). */
    private static final long MIN_TICK_GAP_MILLIS = 2000L;

    private static volatile boolean running;

    private HandlerThread workerThread;
    private Handler worker;
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean loopStarted;
    private long lastTickElapsed;
    private final Runnable tickRunnable = this::tick;

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
        boolean alarmTick = intent != null && ACTION_TICK.equals(intent.getAction());
        if (!loopStarted || alarmTick) {
            loopStarted = true;
            worker.removeCallbacks(tickRunnable);
            worker.post(tickRunnable);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        cancelTickAlarm();
        worker.removeCallbacksAndMessages(null);
        workerThread.quitSafely();
        super.onDestroy();
    }

    private void tick() {
        long nowElapsed = SystemClock.elapsedRealtime();
        if (lastTickElapsed != 0L && nowElapsed - lastTickElapsed < MIN_TICK_GAP_MILLIS) {
            return;
        }
        lastTickElapsed = nowElapsed;
        Context context = getApplicationContext();
        AlertThresholds thresholds = AppServices.getAlertThresholds(context);
        try {
            List<TrackedAppEntity> tracked = AppServices.getDatabase(context).getAllTrackedApps();
            if (tracked.isEmpty()) {
                cancelTickAlarm();
                stopSelf();
                return;
            }
            if (PermissionChecker.hasUsageAccess(context)) {
                check(context, tracked);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "check failed", e);
        }
        scheduleNextTick(thresholds.getCheckIntervalMillis());
    }

    /**
     * Plans the next check. Some devices (Xiaomi, OPPO, vivo ...) pause this app while another
     * app is in front, and a Handler timer does not run while paused. An exact alarm from
     * AlarmManager is delivered by the system, which wakes the app up for the check.
     *
     * <p>Note: on devices with strict battery saver (such as Xiaomi HyperOS), battery saver must
     * be disabled or this app exempted so the system does not freeze background checks.
     */
    private void scheduleNextTick(long delayMillis) {
        // Fallback timer: used when the alarm is not allowed, harmless when the alarm fires first
        // because the alarm removes it in onStartCommand().
        worker.removeCallbacks(tickRunnable);
        worker.postDelayed(tickRunnable, delayMillis + 2000L);

        AlarmManager alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarms == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarms.canScheduleExactAlarms()) {
            return;
        }
        try {
            // RTC (not RTC_WAKEUP): no need to wake the CPU while the screen is off.
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC,
                    System.currentTimeMillis() + delayMillis, tickPendingIntent());
        } catch (SecurityException e) {
            Log.w(TAG, "exact alarm not allowed", e);
        }
    }

    private void cancelTickAlarm() {
        AlarmManager alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarms != null) {
            alarms.cancel(tickPendingIntent());
        }
    }

    private PendingIntent tickPendingIntent() {
        Intent intent = new Intent(this, UsageTrackingService.class).setAction(ACTION_TICK);
        int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        return PendingIntent.getForegroundService(this, TICK_REQUEST_CODE, intent, flags);
    }

    private void check(Context context, List<TrackedAppEntity> tracked) {
        UsageSource source = AppServices.getUsageSource(context);
        RoastTrigger trigger = AppServices.getRoastTrigger(context);

        String foreground = source.getForegroundPackage();
        TrackedAppEntity current = find(tracked, foreground);

        long millisOver = Long.MIN_VALUE;
        if (current != null) {
            millisOver = source.getUsedMillisToday(foreground, current.resetAtMillis)
                    - current.limitMinutes * 60_000L;
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
        // The service keeps the language it was created with, so read the strings in the language
        // chosen in Settings.
        Context ctx = localizedContext();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
            // Creating a channel that already exists only updates its name.
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    ctx.getString(R.string.tracking_service_channel), NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            manager.createNotificationChannel(channel);
        }
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.pid_ic_notification)
                .setContentTitle(ctx.getString(R.string.tracking_service_title))
                .setContentText(ctx.getString(R.string.tracking_service_text))
                .setContentIntent(open)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private Context localizedContext() {
        String lang = new SettingsManager(this).getLanguage();
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(Locale.forLanguageTag(lang));
        return createConfigurationContext(config);
    }
}
