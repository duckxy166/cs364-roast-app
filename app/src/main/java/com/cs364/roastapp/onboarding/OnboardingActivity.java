package com.cs364.roastapp.onboarding;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;

public class OnboardingActivity extends AppCompatActivity {
    /** The permissions owner supplies actual requests and rebinds state after returning from Android. */
    public void bindPermissions(boolean usageAllowed, boolean notificationsAllowed,
            Runnable requestUsage, Runnable requestNotifications, Runnable continueHome) {
        Button usage = findViewById(R.id.pid_permission_usage);
        Button notifications = findViewById(R.id.pid_permission_notifications);
        usage.setText(usageAllowed ? R.string.pid_permission_allowed : R.string.pid_enable_permission);
        notifications.setText(notificationsAllowed ? R.string.pid_permission_allowed : R.string.pid_enable_permission);
        usage.setEnabled(!usageAllowed); notifications.setEnabled(!notificationsAllowed);
        usage.setOnClickListener(v -> requestUsage.run());
        notifications.setOnClickListener(v -> requestNotifications.run());
        findViewById(R.id.pid_onboarding_continue).setEnabled(usageAllowed && notificationsAllowed);
        findViewById(R.id.pid_onboarding_continue).setOnClickListener(v -> continueHome.run());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_activity);

        com.cs364.roastapp.ui.PutItDownNavigation.bind(this, R.id.pid_nav_home);
        Button backBtn = findViewById(R.id.btn_onboarding_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }
}
