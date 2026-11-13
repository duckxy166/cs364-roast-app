package com.cs364.roastapp.ui;

import android.app.Activity;
import android.content.Intent;
import com.cs364.roastapp.R;
import com.cs364.roastapp.core.HomeActivity;
import com.cs364.roastapp.focus.FocusActivity;
import com.cs364.roastapp.settings.SettingsActivity;
import com.cs364.roastapp.tracking.TrackedAppsActivity;
import com.google.android.material.navigation.NavigationBarView;
import java.util.function.IntConsumer;

/** Presentation navigation only; no tracking, permissions or persistence. */
public final class PutItDownNavigation {
    private PutItDownNavigation() {}
    public static void bind(Activity activity, int current) {
        bind(activity, current, destination -> open(activity, destination));
    }
    public static void bind(Activity activity, int current, IntConsumer navigate) {
        NavigationBarView bar = activity.findViewById(R.id.pid_bottom_nav);
        if (bar == null) return;
        bar.setSelectedItemId(current);
        bar.setOnItemSelectedListener(item -> {
            if (item.getItemId() != current) navigate.accept(item.getItemId());
            return item.getItemId() == current;
        });
    }
    public static void open(Activity activity, int destination) {
        Class<?> target = destination == R.id.pid_nav_home ? HomeActivity.class
                : destination == R.id.btn_nav_tracking ? TrackedAppsActivity.class
                : destination == R.id.btn_nav_focus ? FocusActivity.class : SettingsActivity.class;
        activity.startActivity(new Intent(activity, target).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }
}
