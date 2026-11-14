package com.cs364.roastapp.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Restarts the detector only if there are tracked apps and usage access is granted.
            TrackingServiceController.sync(context);
        }
    }
}
