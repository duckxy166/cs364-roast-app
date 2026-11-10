package com.cs364.roastapp.core;

public interface UsageSource {
    long getUsedMillisToday(String packageName);
}
