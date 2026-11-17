package com.cs364.roastapp.roast;

import android.content.Context;
import android.widget.Toast;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.RoastTrigger;

/** Debug button: first tap fires a reminder for this app, second tap resets. */
public final class RoastTestHelper {

    private static boolean fired = false;

    private RoastTestHelper() {
    }

    public static void nextStep(Context context) {
        Context app = context.getApplicationContext();
        String pkg = app.getPackageName();
        RoastTrigger trigger = AppServices.getRoastTrigger(app);
        if (!fired) {
            trigger.onOverLimit(pkg, 0L);
            fired = true;
            return;
        }
        if (trigger instanceof RoastManager) {
            ((RoastManager) trigger).resetPackage(pkg);
        }
        Toast.makeText(context, R.string.roast_test_reset, Toast.LENGTH_SHORT).show();
        fired = false;
    }
}
