package com.cs364.roastapp.tracking;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cs364.roastapp.core.UsageSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Real {@link UsageSource} backed by {@link UsageStatsManager}.
 *
 * <p>Replays usage events and stores, per package, the foreground intervals that overlap today
 * (since local midnight). Events from the hours before midnight are replayed too, only to know
 * which apps were already open when the day started. The same pass also tells which package is in
 * the foreground right now, and lets each tracked app count from a custom mid-day reset timestamp.
 * Results are cached for a second.
 */
public class UsageStatsUsageSource implements UsageSource {

    private static final String TAG = "UsageStatsSource";
    private static final long CACHE_MILLIS = 1000L;
    /**
     * How far before midnight events are read. An app that was opened before midnight and is
     * still in front has no new event today, so yesterday's events are needed to know it is open.
     */
    private static final long LOOKBACK_MILLIS = 12L * 60L * 60L * 1000L;

    // Same values as MOVE_TO_FOREGROUND / MOVE_TO_BACKGROUND (API < 29) and
    // ACTIVITY_RESUMED / ACTIVITY_PAUSED / ACTIVITY_STOPPED (API 29+),
    // SCREEN_NON_INTERACTIVE (API 28+), DEVICE_SHUTDOWN / DEVICE_STARTUP (API 29+).
    private static final int EVENT_RESUMED = 1;
    private static final int EVENT_PAUSED = 2;
    private static final int EVENT_SCREEN_OFF = 16;
    private static final int EVENT_STOPPED = 23;
    private static final int EVENT_DEVICE_SHUTDOWN = 26;
    private static final int EVENT_DEVICE_STARTUP = 27;

    private static final class Snapshot {
        final long takenAt;
        final long dayStart;
        final Map<String, List<long[]>> intervals;
        @Nullable
        final String foreground;

        Snapshot(long takenAt, long dayStart, Map<String, List<long[]>> intervals,
                 @Nullable String foreground) {
            this.takenAt = takenAt;
            this.dayStart = dayStart;
            this.intervals = intervals;
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
        return getUsedMillisToday(packageName, 0L);
    }

    @Override
    public long getUsedMillisToday(String packageName, long sinceMillis) {
        if (packageName == null) {
            return 0L;
        }
        Snapshot current = snapshot();
        List<long[]> ranges = current.intervals.get(packageName);
        if (ranges == null || ranges.isEmpty()) {
            return 0L;
        }
        long cutoff = Math.max(current.dayStart, sinceMillis);
        long total = 0L;
        for (long[] range : ranges) {
            long overlap = range[1] - Math.max(range[0], cutoff);
            if (overlap > 0L) {
                total += overlap;
            }
        }
        return total;
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
        Map<String, List<long[]>> intervals = new HashMap<>();
        if (usageStatsManager == null) {
            return new Snapshot(now, dayStart, intervals, null);
        }
        // package -> resumed activity classes, when it first became foreground, and latest resume
        Map<String, Set<String>> open = new HashMap<>();
        Map<String, Long> since = new HashMap<>();
        Map<String, Long> lastResumed = new HashMap<>();
        try {
            UsageEvents events = usageStatsManager.queryEvents(dayStart - LOOKBACK_MILLIS, now);
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
                        lastResumed.put(pkg, time);
                        classes.add(classOf(event));
                        break;
                    }
                    case EVENT_PAUSED:
                    case EVENT_STOPPED: {
                        Set<String> classes = pkg == null ? null : open.get(pkg);
                        if (classes == null || classes.isEmpty()) {
                            break; // resumed before the events we read, ignore
                        }
                        classes.remove(classOf(event));
                        if (classes.isEmpty()) {
                            recordInterval(intervals, pkg, since.get(pkg), time, dayStart);
                        }
                        break;
                    }
                    case EVENT_SCREEN_OFF:
                    case EVENT_DEVICE_SHUTDOWN:
                    case EVENT_DEVICE_STARTUP: {
                        for (Map.Entry<String, Set<String>> entry : open.entrySet()) {
                            if (!entry.getValue().isEmpty()) {
                                recordInterval(intervals, entry.getKey(),
                                        since.get(entry.getKey()), time, dayStart);
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
            String pkg = entry.getKey();
            recordInterval(intervals, pkg, since.get(pkg), now, dayStart);
            Long resumedAt = lastResumed.get(pkg);
            long keyTime = resumedAt != null ? resumedAt : since.get(pkg);
            if (keyTime >= latest) {
                latest = keyTime;
                foreground = pkg;
            }
        }
        return new Snapshot(now, dayStart, intervals, foreground);
    }

    private static String classOf(UsageEvents.Event event) {
        String name = event.getClassName();
        return name == null ? "" : name;
    }

    private static void recordInterval(Map<String, List<long[]>> intervals, String pkg,
                                       @Nullable Long start, long end, long dayStart) {
        if (start == null || end <= dayStart || end <= start) {
            return;
        }
        intervals.computeIfAbsent(pkg, k -> new ArrayList<>())
                .add(new long[]{Math.max(start, dayStart), end});
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
