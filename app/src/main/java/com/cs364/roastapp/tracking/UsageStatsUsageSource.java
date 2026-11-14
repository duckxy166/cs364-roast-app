package com.cs364.roastapp.tracking;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cs364.roastapp.core.UsageSource;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Real {@link UsageSource} backed by {@link UsageStatsManager}.
 *
 * <p>Replays today's usage events (since local midnight) and sums, per package, the time at least
 * one of its activities was resumed. The same pass also tells which package is in the foreground
 * right now, so one query serves both questions. Results are cached for a second.
 */
public class UsageStatsUsageSource implements UsageSource {

    private static final String TAG = "UsageStatsSource";
    private static final long CACHE_MILLIS = 1000L;

    // Same values as MOVE_TO_FOREGROUND / MOVE_TO_BACKGROUND (API < 29) and
    // ACTIVITY_RESUMED / ACTIVITY_PAUSED (API 29+), and SCREEN_NON_INTERACTIVE (API 28+).
    private static final int EVENT_RESUMED = 1;
    private static final int EVENT_PAUSED = 2;
    private static final int EVENT_SCREEN_OFF = 16;

    private static final class Snapshot {
        final long takenAt;
        final long dayStart;
        final Map<String, Long> usedMillis;
        @Nullable
        final String foreground;

        Snapshot(long takenAt, long dayStart, Map<String, Long> usedMillis, @Nullable String foreground) {
            this.takenAt = takenAt;
            this.dayStart = dayStart;
            this.usedMillis = usedMillis;
            this.foreground = foreground;
        }
    }

    private final UsageStatsManager usageStatsManager;
    private Snapshot cache;

    public UsageStatsUsageSource(Context context) {
        this.usageStatsManager = (UsageStatsManager) context.getApplicationContext()
                .getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public long getUsedMillisToday(String packageName) {
        if (packageName == null) {
            return 0L;
        }
        Long used = snapshot().usedMillis.get(packageName);
        return used != null ? used : 0L;
    }

    @Nullable
    @Override
    public String getForegroundPackage() {
        return snapshot().foreground;
    }

    private synchronized Snapshot snapshot() {
        long now = System.currentTimeMillis();
        long dayStart = startOfDay(now);
        if (cache != null && cache.dayStart == dayStart && now >= cache.takenAt
                && now - cache.takenAt < CACHE_MILLIS) {
            return cache;
        }
        cache = compute(now, dayStart);
        return cache;
    }

    private Snapshot compute(long now, long dayStart) {
        Map<String, Long> used = new HashMap<>();
        if (usageStatsManager == null) {
            return new Snapshot(now, dayStart, used, null);
        }
        // package -> resumed activity classes, and when the package first became foreground
        Map<String, Set<String>> open = new HashMap<>();
        Map<String, Long> since = new HashMap<>();
        try {
            UsageEvents events = usageStatsManager.queryEvents(dayStart, now);
            UsageEvents.Event event = new UsageEvents.Event();
            while (events != null && events.hasNextEvent()) {
                events.getNextEvent(event);
                String pkg = event.getPackageName();
                long time = event.getTimeStamp();
                switch (event.getEventType()) {
                    case EVENT_RESUMED: {
                        if (pkg == null) {
                            break;
                        }
                        Set<String> classes = open.computeIfAbsent(pkg, k -> new HashSet<>());
                        if (classes.isEmpty()) {
                            since.put(pkg, time);
                        }
                        classes.add(classOf(event));
                        break;
                    }
                    case EVENT_PAUSED: {
                        Set<String> classes = pkg == null ? null : open.get(pkg);
                        if (classes == null || classes.isEmpty()) {
                            break; // resumed before midnight, ignore
                        }
                        classes.remove(classOf(event));
                        if (classes.isEmpty()) {
                            add(used, pkg, time - since.get(pkg));
                        }
                        break;
                    }
                    case EVENT_SCREEN_OFF: {
                        for (Map.Entry<String, Set<String>> entry : open.entrySet()) {
                            if (!entry.getValue().isEmpty()) {
                                add(used, entry.getKey(), time - since.get(entry.getKey()));
                                entry.getValue().clear();
                            }
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "queryEvents failed (usage access missing?)", e);
        }

        String foreground = null;
        long latest = Long.MIN_VALUE;
        for (Map.Entry<String, Set<String>> entry : open.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            long start = since.get(entry.getKey());
            add(used, entry.getKey(), now - start);
            if (start >= latest) {
                latest = start;
                foreground = entry.getKey();
            }
        }
        return new Snapshot(now, dayStart, used, foreground);
    }

    private static String classOf(UsageEvents.Event event) {
        String name = event.getClassName();
        return name == null ? "" : name;
    }

    private static void add(Map<String, Long> used, String pkg, long millis) {
        if (millis <= 0L) {
            return;
        }
        used.merge(pkg, millis, Long::sum);
    }

    static long startOfDay(long now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
