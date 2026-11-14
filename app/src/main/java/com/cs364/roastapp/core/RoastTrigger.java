package com.cs364.roastapp.core;

public interface RoastTrigger {
    void onOverLimit(String packageName, long millisOver);

    /** The user left a package that was over its limit; implementations should stop any alerts. */
    default void onLeftApp(String packageName) {
    }
}
