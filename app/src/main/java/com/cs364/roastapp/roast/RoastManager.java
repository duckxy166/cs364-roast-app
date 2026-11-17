package com.cs364.roastapp.roast;

import android.content.Context;
import android.util.Log;

import com.cs364.roastapp.core.RoastTrigger;

public class RoastManager implements RoastTrigger {

    private static final String TAG = "RoastManager";

    private final RoastNotifier notifier;
    private final VoiceRoastAlert alert;

    public RoastManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.notifier = new RoastNotifier(appContext);
        this.alert = new VoiceRoastAlert(appContext, notifier);
    }

    @Override
    public void onOverLimit(String packageName, long millisOver) {
        if (packageName == null || millisOver < 0L) {
            return;
        }
        Log.d(TAG, "onOverLimit pkg=" + packageName + " over=" + millisOver);
        alert.onOverLimit(packageName);
    }

    @Override
    public void onLeftApp(String packageName) {
        if (packageName != null) {
            resetPackage(packageName);
        }
    }

    public void resetPackage(String packageName) {
        alert.stop(packageName);
        notifier.cancel(packageName);
    }

    /** The reminder notification was swiped away: stop the voice now, keep reminding later. */
    public void silence(String packageName) {
        alert.silence(packageName);
    }
}
