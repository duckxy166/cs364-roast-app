package com.cs364.roastapp.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Conservative presentation model: absent Android buckets never become zero usage. */
public final class WeeklyUsageData {
    public static final class Day {
        public final long startMillis;
        public final Long usedMillis;
        public Day(long startMillis, Long usedMillis) { this.startMillis=startMillis; this.usedMillis=usedMillis; }
    }
    private WeeklyUsageData() {}
    public static List<Day> build(List<Long> days, Set<String> packages,
            Map<Long, Map<String, Long>> buckets) {
        List<Day> result=new ArrayList<>();
        for(long day:days) {
            Map<String,Long> values=buckets.get(day);
            boolean complete=!packages.isEmpty() && values!=null && values.keySet().containsAll(packages);
            long total=0;
            if(complete) for(String pkg:packages) total+=Math.max(0L,values.get(pkg));
            result.add(new Day(day,complete?total:null));
        }
        return result;
    }
}
