package com.cs364.roastapp.roast;

import android.content.Context;
import android.widget.Toast;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AlertThresholds;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.RoastTrigger;

public final class RoastTestHelper {

    private static int step = 0;

    private RoastTestHelper() {
    }

    public static void nextStep(Context context) {
        Context app = context.getApplicationContext();
        String pkg = app.getPackageName();
        AlertThresholds t = AppServices.getAlertThresholds(app);
        RoastTrigger trigger = AppServices.getRoastTrigger(app);

        switch (step) {
            case 0:
                trigger.onOverLimit(pkg, t.getLevel1OverMillis());
                break;
            case 1:
                trigger.onOverLimit(pkg, t.getLevel2OverMillis());
                break;
            case 2:
                trigger.onOverLimit(pkg, t.getLevel3OverMillis());
                break;
            default:
                if (trigger instanceof RoastManager) {
                    ((RoastManager) trigger).resetPackage(pkg);
                }
                Toast.makeText(context, R.string.roast_test_reset, Toast.LENGTH_SHORT).show();
                step = 0;
                return;
        }
        step++;
    }
}
