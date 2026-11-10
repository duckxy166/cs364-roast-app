package com.cs364.roastapp.core;

import java.util.HashMap;
import java.util.Map;

public class FakeUsageSource implements UsageSource {

    private static final long DEFAULT_FAKE_USED_MILLIS = 15L * 60L * 1000L;
    private final Map<String, Long> overrides = new HashMap<>();

    @Override
    public long getUsedMillisToday(String packageName) {
        if (packageName == null) {
            return 0L;
        }
        Long custom = overrides.get(packageName);
        if (custom != null) {
            return custom;
        }
        return DEFAULT_FAKE_USED_MILLIS;
    }

    public void setUsedMillisToday(String packageName, long usedMillis) {
        overrides.put(packageName, Math.max(0L, usedMillis));
    }
}
