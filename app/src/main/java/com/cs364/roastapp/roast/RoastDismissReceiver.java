package com.cs364.roastapp.roast;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.RoastTrigger;

public class RoastDismissReceiver extends BroadcastReceiver {

    public static final String ACTION_DISMISS_LEVEL3 = "com.cs364.roastapp.ACTION_DISMISS_LEVEL3";
    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_DISMISS_LEVEL3.equals(intent.getAction())) {
            return;
        }
        String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
        RoastTrigger trigger = AppServices.getRoastTrigger(context);
        if (trigger instanceof RoastManager rm) {
            rm.resetPackage(packageName);
        }
    }

    public static PendingIntent createPendingIntent(Context context, String packageName) {
        Intent intent = new Intent(context, RoastDismissReceiver.class);
        intent.setAction(ACTION_DISMISS_LEVEL3);
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        int requestCode = 2000 + (packageName != null ? (packageName.hashCode() & 0x0FFFFFFF) : 0);
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }
}
