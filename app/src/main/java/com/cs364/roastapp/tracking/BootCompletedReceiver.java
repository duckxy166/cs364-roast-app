package com.cs364.roastapp.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Stub for Issue #7: restart UsageTrackingService on boot when tracked apps exist
    }
}
