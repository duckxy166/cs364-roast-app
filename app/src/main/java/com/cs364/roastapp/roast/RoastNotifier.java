package com.cs364.roastapp.roast;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.cs364.roastapp.R;
import com.cs364.roastapp.settings.SettingsManager;

import java.util.Locale;

public class RoastNotifier {

    /** The single reminder channel. */
    public static final String CHANNEL_ALERT = "roast_alert";
    /** Channels from the old three-level design; removed so they stop showing in system settings. */
    private static final String[] RETIRED_CHANNELS = {"roast_level_1", "roast_level_2", "roast_level_3"};
    /** Stored with every history record; reminders no longer have levels. */
    private static final int EVENT_LEVEL = 1;

    private static final long[] VIBRATE_PATTERN = {0L, 400L, 200L, 400L};

    private final Context appContext;

    public RoastNotifier(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * Posts the reminder for {@code packageName} showing voice line {@code lineIndex}, vibrates,
     * and records it in history. Returns the line shown.
     */
    public String showAlert(String packageName, int lineIndex) {
        Context ctx = localizedContext();
        ensureChannels(ctx);
        String appLabel = getAppLabel(packageName);
        String[] lines = ctx.getResources().getStringArray(R.array.roast_voice_lines);
        String line = lines[Math.floorMod(lineIndex, lines.length)];

        PendingIntent deleteIntent = RoastDismissReceiver.createPendingIntent(ctx, packageName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ALERT)
                .setSmallIcon(R.drawable.pid_ic_notification)
                .setColor(ContextCompat.getColor(ctx, R.color.pid_brand_blue))
                .setContentTitle(ctx.getString(R.string.roast_alert_title, appLabel))
                .setContentText(line)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(line))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVibrate(VIBRATE_PATTERN)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(true);
        notifySafely(packageName, builder);
        vibrate();
        recordEvent(packageName, appLabel, EVENT_LEVEL, line);
        return line;
    }

    private void recordEvent(String packageName, String appLabel, int level, String message) {
        if (packageName == null) {
            return;
        }
        com.cs364.roastapp.core.AppServices.getDatabase(appContext)
                .roastEventDao()
                .insert(new com.cs364.roastapp.history.RoastEventEntity(
                        packageName,
                        appLabel,
                        level,
                        message,
                        System.currentTimeMillis()
                ));
    }

    public void cancel(String packageName) {
        if (packageName == null) {
            NotificationManagerCompat.from(appContext).cancelAll();
        } else {
            NotificationManagerCompat.from(appContext).cancel(notificationId(packageName));
        }
    }

    private Context localizedContext() {
        String lang = new SettingsManager(appContext).getLanguage();
        Configuration config = new Configuration(appContext.getResources().getConfiguration());
        config.setLocale(Locale.forLanguageTag(lang));
        return appContext.createConfigurationContext(config);
    }

    private void ensureChannels(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm == null) {
            return;
        }
        for (String retired : RETIRED_CHANNELS) {
            nm.deleteNotificationChannel(retired);
        }
        NotificationChannel alert = new NotificationChannel(
                CHANNEL_ALERT,
                ctx.getString(R.string.roast_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        alert.setDescription(ctx.getString(R.string.roast_channel_desc));
        alert.enableVibration(true);
        alert.setVibrationPattern(VIBRATE_PATTERN);
        // The voice line is our sound; keep the channel's own sound from playing over it.
        alert.setSound(null, null);
        nm.createNotificationChannel(alert);
    }

    private String getAppLabel(String packageName) {
        if (packageName == null) {
            return "";
        }
        PackageManager pm = appContext.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private void notifySafely(String packageName, NotificationCompat.Builder builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(appContext).notify(notificationId(packageName), builder.build());
    }

    private void vibrate() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = appContext.getSystemService(VibratorManager.class);
            vibrator = vm != null ? vm.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        vibrator.vibrate(VibrationEffect.createWaveform(VIBRATE_PATTERN, -1));
    }

    private static int notificationId(String packageName) {
        if (packageName == null) {
            return 1000;
        }
        return 1000 + (packageName.hashCode() & 0x0FFFFFFF);
    }
}
