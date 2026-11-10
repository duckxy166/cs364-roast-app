package com.cs364.roastapp.core;

import android.util.Log;

public class LogRoastTrigger implements RoastTrigger {

    private static final String TAG = "RoastTrigger";

    @Override
    public void onOverLimit(String packageName, long millisOver) {
        Log.d(TAG, "onOverLimit: package=" + packageName + ", millisOver=" + millisOver);
    }
}
