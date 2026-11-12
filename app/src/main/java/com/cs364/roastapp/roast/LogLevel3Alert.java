package com.cs364.roastapp.roast;

import android.util.Log;

public class LogLevel3Alert implements Level3Alert {

    private static final String TAG = "Level3Alert";

    @Override
    public void onLevel3(String packageName, long millisOver) {
        Log.d(TAG, "level 3 handoff: pkg=" + packageName + ", millisOver=" + millisOver);
    }

    @Override
    public void stop(String packageName) {
        Log.d(TAG, "stop: pkg=" + packageName);
    }
}
