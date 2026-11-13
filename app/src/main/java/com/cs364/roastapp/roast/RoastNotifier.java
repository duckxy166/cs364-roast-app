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

    public static final String CHANNEL_LEVEL_1 = "roast_level_1";
    public static final String CHANNEL_LEVEL_2 = "roast_level_2";
    public static final String CHANNEL_LEVEL_3 = "roast_level_3";

    private static final long[] VIBRATE_PATTERN = {0L, 400L, 200L, 400L};

    private final Context appContext;

    public RoastNotifier(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void showLevel1(String packageName) {
        Context ctx = localizedContext();
        ensureChannels(ctx);
        String appLabel = getAppLabel(packageName);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_LEVEL_1)
                .setSmallIcon(R.drawable.pid_ic_notification)
                .setColor(ContextCompat.getColor(ctx, R.color.pid_brand_blue))
                .setContentTitle(ctx.getString(R.string.roast_level1_title))
                .setContentText(ctx.getString(R.string.roast_level1_text, appLabel))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        notifySafely(packageName, builder);
    }

    public void showLevel2(String packageName) {
        Context ctx = localizedContext();
        ensureChannels(ctx);
        String appLabel = getAppLabel(packageName);
        String line = pickLevel2Line(ctx);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_LEVEL_2)
                .setSmallIcon(R.drawable.pid_ic_notification)
                .setColor(ContextCompat.getColor(ctx, R.color.pid_brand_blue))
                .setContentTitle(ctx.getString(R.string.roast_level2_title, appLabel))
                .setContentText(line)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(line))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVibrate(VIBRATE_PATTERN)
                .setAutoCancel(true);
        notifySafely(packageName, builder);
        vibrate();
    }

    public String showLevel3(String packageName, int lineIndex) {
        Context ctx = localizedContext();
        ensureChannels(ctx);
        String appLabel = getAppLabel(packageName);
        String[] lines = ctx.getResources().getStringArray(R.array.roast_level3_lines);
        String line = (lineIndex >= 0 && lineIndex < lines.length) ? lines[lineIndex] : "";

        PendingIntent deleteIntent = RoastDismissReceiver.createPendingIntent(ctx, packageName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_LEVEL_3)
                .setSmallIcon(R.drawable.pid_ic_notification)
                .setColor(ContextCompat.getColor(ctx, R.color.pid_brand_blue))
                .setContentTitle(ctx.getString(R.string.roast_level3_title, appLabel))
                .setContentText(line)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(line))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVibrate(VIBRATE_PATTERN)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(true);
        notifySafely(packageName, builder);
        vibrate();
        return line;
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
        NotificationChannel level1 = new NotificationChannel(
                CHANNEL_LEVEL_1,
                ctx.getString(R.string.roast_channel_level1_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        level1.setDescription(ctx.getString(R.string.roast_channel_level1_desc));

        NotificationChannel level2 = new NotificationChannel(
                CHANNEL_LEVEL_2,
                ctx.getString(R.string.roast_channel_level2_name),
                NotificationManager.IMPORTANCE_HIGH);
        level2.setDescription(ctx.getString(R.string.roast_channel_level2_desc));
        level2.enableVibration(true);
        level2.setVibrationPattern(VIBRATE_PATTERN);

        NotificationChannel level3 = new NotificationChannel(
                CHANNEL_LEVEL_3,
                ctx.getString(R.string.roast_channel_level3_name),
                NotificationManager.IMPORTANCE_HIGH);
        level3.setDescription(ctx.getString(R.string.roast_channel_level3_desc));
        level3.enableVibration(true);
        level3.setVibrationPattern(VIBRATE_PATTERN);

        nm.createNotificationChannel(level1);
        nm.createNotificationChannel(level2);
        nm.createNotificationChannel(level3);
    }

    private String pickLevel2Line(Context ctx) {
        return RoastLineProvider.getInstance(appContext).getNextRoastLine(ctx);
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
