package com.cs364.roastapp.core;

import androidx.annotation.Nullable;

public interface UsageSource {
    long getUsedMillisToday(String packageName);

    /**
     * Time used today by {@code packageName}, counting only usage at or after {@code sinceMillis}.
     * When {@code sinceMillis} is earlier than local midnight (for example 0 or a reset from an
     * earlier day), this counts from local midnight as normal.
     */
    default long getUsedMillisToday(String packageName, long sinceMillis) {
        return getUsedMillisToday(packageName);
    }

    /** Package currently in the foreground, or null when unknown / screen off. */
    @Nullable
    default String getForegroundPackage() {
        return null;
    }
}
