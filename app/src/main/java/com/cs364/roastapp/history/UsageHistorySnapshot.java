package com.cs364.roastapp.history;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.tracking.PermissionChecker;
import com.cs364.roastapp.tracking.TrackedAppEntity;
import java.util.*;

/** Read-only adapter for Android's daily buckets. Run on the repository worker. */
public final class UsageHistorySnapshot {
    public enum State { AVAILABLE, NO_APPS, PERMISSION_REQUIRED, UNAVAILABLE }
    public final State state;
    public final List<WeeklyUsageData.Day> days;
    public final int knownDays;
    public final long knownMillis;
    public final long startMillis, endMillis;
    private UsageHistorySnapshot(State state,List<WeeklyUsageData.Day> days,long start,long end) {
        this.state=state; this.days=days;startMillis=start;endMillis=end;
        int known=0;long total=0;
        for(WeeklyUsageData.Day d:days)if(d.usedMillis!=null){known++;total+=d.usedMillis;}
        knownDays=known;knownMillis=total;
    }
    public static UsageHistorySnapshot load(Context context) {
        Calendar cursor=Calendar.getInstance();cursor.set(Calendar.HOUR_OF_DAY,0);cursor.set(Calendar.MINUTE,0);cursor.set(Calendar.SECOND,0);cursor.set(Calendar.MILLISECOND,0);
        long end=cursor.getTimeInMillis();cursor.add(Calendar.DAY_OF_YEAR,-6);long start=cursor.getTimeInMillis();
        List<Long> dates=new ArrayList<>();for(int i=0;i<7;i++){dates.add(cursor.getTimeInMillis());cursor.add(Calendar.DAY_OF_YEAR,1);}
        Set<String> packages=new HashSet<>();
        for(TrackedAppEntity a:AppServices.getDatabase(context).trackedAppDao().getAll())if(a.packageName!=null)packages.add(a.packageName);
        if(packages.isEmpty())return new UsageHistorySnapshot(State.NO_APPS,Collections.emptyList(),start,end);
        if(!PermissionChecker.hasUsageAccess(context))return new UsageHistorySnapshot(State.PERMISSION_REQUIRED,Collections.emptyList(),start,end);
        Map<Long,Map<String,Long>> buckets=new HashMap<>();
        try {
            UsageStatsManager service=context.getSystemService(UsageStatsManager.class);
            if(service==null)return new UsageHistorySnapshot(State.UNAVAILABLE,Collections.emptyList(),start,end);
            List<UsageStats> rows=service.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,start,System.currentTimeMillis());
            if(rows==null)return new UsageHistorySnapshot(State.UNAVAILABLE,Collections.emptyList(),start,end);
            for(UsageStats row:rows)if(packages.contains(row.getPackageName())) {
                Calendar day=Calendar.getInstance();day.setTimeInMillis(row.getFirstTimeStamp());day.set(Calendar.HOUR_OF_DAY,0);day.set(Calendar.MINUTE,0);day.set(Calendar.SECOND,0);day.set(Calendar.MILLISECOND,0);
                long key=day.getTimeInMillis();if(key<start||key>end)continue;
                Map<String,Long> values=buckets.computeIfAbsent(key,k->new HashMap<>());
                values.merge(row.getPackageName(),Math.max(0L,row.getTotalTimeInForeground()),Long::sum);
            }
            return new UsageHistorySnapshot(State.AVAILABLE,WeeklyUsageData.build(dates,packages,buckets),start,end);
        }catch(SecurityException|IllegalStateException error){return new UsageHistorySnapshot(State.UNAVAILABLE,Collections.emptyList(),start,end);}
    }
}
