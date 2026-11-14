package com.cs364.roastapp.core;

import androidx.annotation.Nullable;

public interface UsageSource {
    long getUsedMillisToday(String packageName);

    /** Package currently in the foreground, or null when unknown / screen off. */
    @Nullable
    default String getForegroundPackage() {
        return null;
    }
}
