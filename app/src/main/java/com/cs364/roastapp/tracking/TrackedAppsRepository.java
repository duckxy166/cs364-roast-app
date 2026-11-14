package com.cs364.roastapp.tracking;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.UsageSource;
import com.cs364.roastapp.ui.AppLimitsUi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Bridges the tracked_app table, PackageManager and UsageSource to the UI model. */
public final class TrackedAppsRepository {

    /** Shared single-thread executor for database / package scans kept off the UI thread. */
    public static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private final Context context;
    private final PackageManager packageManager;
    private final TrackedAppDao dao;

    public TrackedAppsRepository(Context context) {
        this.context = context.getApplicationContext();
        this.packageManager = this.context.getPackageManager();
        this.dao = AppServices.getDatabase(this.context).trackedAppDao();
    }

    /** Tracked apps with today's usage; apps that were uninstalled are dropped from the table. */
    public List<AppLimitsUi.App> loadTracked() {
        UsageSource usage = AppServices.getUsageSource(context);
        List<AppLimitsUi.App> result = new ArrayList<>();
        for (TrackedAppEntity entity : dao.getAll()) {
            try {
                CharSequence label = packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(entity.packageName, 0));
                Drawable icon = packageManager.getApplicationIcon(entity.packageName);
                long usedMinutes = usage.getUsedMillisToday(entity.packageName) / 60_000L;
                result.add(new AppLimitsUi.App(entity.packageName, label, icon,
                        usedMinutes, entity.limitMinutes));
            } catch (PackageManager.NameNotFoundException e) {
                dao.deleteByPackage(entity.packageName);
            }
        }
        return result;
    }

    /** Launchable installed apps that are not tracked yet (and not this app). */
    public List<AppLimitsUi.App> loadAvailable() {
        Set<String> skip = new HashSet<>();
        skip.add(context.getPackageName());
        for (TrackedAppEntity entity : dao.getAll()) {
            skip.add(entity.packageName);
        }
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<AppLimitsUi.App> result = new ArrayList<>();
        for (ResolveInfo info : packageManager.queryIntentActivities(launcher, 0)) {
            String pkg = info.activityInfo.packageName;
            if (!skip.add(pkg)) {
                continue;
            }
            result.add(new AppLimitsUi.App(pkg, info.loadLabel(packageManager),
                    info.loadIcon(packageManager), 0, TrackedAppEntity.DEFAULT_LIMIT_MINUTES));
        }
        result.sort((a, b) -> a.name.toString().toLowerCase(Locale.ROOT)
                .compareTo(b.name.toString().toLowerCase(Locale.ROOT)));
        return result;
    }

    /** Adds the app, or updates its limit when it is already tracked. */
    public void saveLimit(String packageName, long minutes) {
        long limit = Math.max(1L, minutes);
        if (dao.updateLimit(packageName, limit) == 0) {
            TrackedAppEntity entity = new TrackedAppEntity();
            entity.packageName = packageName;
            entity.limitMinutes = limit;
            dao.insert(entity);
        }
    }

    public void remove(String packageName) {
        dao.deleteByPackage(packageName);
    }
}
