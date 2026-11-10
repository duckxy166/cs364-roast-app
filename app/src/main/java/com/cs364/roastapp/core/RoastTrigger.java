package com.cs364.roastapp.core;

public interface RoastTrigger {
    void onOverLimit(String packageName, long millisOver);
}
