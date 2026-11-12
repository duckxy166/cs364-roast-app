package com.cs364.roastapp.roast;

import android.content.Context;
import android.util.Log;

import com.cs364.roastapp.core.AlertThresholds;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.RoastTrigger;

import java.util.HashMap;
import java.util.Map;

public class RoastManager implements RoastTrigger {

    private static final String TAG = "RoastManager";

    private final Context appContext;
    private final RoastNotifier notifier;
    private volatile Level3Alert level3Alert;

    private final Map<String, Integer> firedLevel = new HashMap<>();
    private final Map<String, Long> lastMillisOver = new HashMap<>();

    public RoastManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.notifier = new RoastNotifier(appContext);
        this.level3Alert = new LogLevel3Alert();
    }

    public void setLevel3Alert(Level3Alert alert) {
        this.level3Alert = alert != null ? alert : new LogLevel3Alert();
    }

    @Override
    public void onOverLimit(String packageName, long millisOver) {
        if (packageName == null) {
            return;
        }
        AlertThresholds thresholds = AppServices.getAlertThresholds(appContext);
        int level = thresholds.getAlertLevel(millisOver);

        int previous;
        synchronized (this) {
            Long last = lastMillisOver.get(packageName);
            if (last != null && millisOver < last) {
                firedLevel.remove(packageName);
            }
            lastMillisOver.put(packageName, millisOver);

            Integer fired = firedLevel.get(packageName);
            previous = fired != null ? fired : 0;
            if (level > previous) {
                firedLevel.put(packageName, level);
            }
        }

        Log.d(TAG, "onOverLimit pkg=" + packageName + " over=" + millisOver
                + " level=" + level + " previous=" + previous);

        if (level == 3) {
            level3Alert.onLevel3(packageName, millisOver);
            return;
        }
        if (level <= previous) {
            return;
        }
        if (level == 1) {
            notifier.showLevel1(packageName);
        } else if (level == 2) {
            notifier.showLevel2(packageName);
        }
    }

    public void resetPackage(String packageName) {
        synchronized (this) {
            firedLevel.remove(packageName);
            lastMillisOver.remove(packageName);
        }
        level3Alert.stop(packageName);
        notifier.cancel(packageName);
    }
}
