package com.cs364.roastapp.core;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;
import com.cs364.roastapp.focus.FocusActivity;
import com.cs364.roastapp.onboarding.OnboardingActivity;
import com.cs364.roastapp.settings.SettingsActivity;
import com.cs364.roastapp.tracking.TrackedAppsActivity;

public class HomeActivity extends AppCompatActivity {
    public void renderAppUsage(java.util.List<com.cs364.roastapp.ui.AppLimitsUi.App> apps) {
        new com.cs364.roastapp.ui.AppLimitsUi(this, null).render(apps, true);
    }
    public void renderPermissionWarning(boolean missing) {
        renderMonitoringState(missing ? com.cs364.roastapp.ui.MonitoringStatusUi.State.MISSING_PERMISSIONS
                : com.cs364.roastapp.ui.MonitoringStatusUi.State.READY,
                () -> startActivity(new Intent(this, OnboardingActivity.class)));
    }


    public void renderMonitoringState(com.cs364.roastapp.ui.MonitoringStatusUi.State state, Runnable recovery) {
        com.cs364.roastapp.ui.MonitoringStatusUi.render(this, state, recovery);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // First launch: onboarding comes first; it brings the user back here once permissions are granted.
        if (!com.cs364.roastapp.onboarding.OnboardingPrefs.isDone(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.core_activity_home);

        com.cs364.roastapp.ui.PutItDownNavigation.bind(this, R.id.pid_nav_home);
        findViewById(R.id.btn_nav_onboarding).setOnClickListener(v ->
                startActivity(new Intent(this, OnboardingActivity.class)));
        findViewById(R.id.pid_home_manage).setOnClickListener(v ->
                startActivity(new Intent(this, TrackedAppsActivity.class)));
        findViewById(R.id.pid_home_add).setOnClickListener(v ->
                startActivity(new Intent(this, TrackedAppsActivity.class)));
        findViewById(R.id.pid_home_focus).setOnClickListener(v ->
                startActivity(new Intent(this, FocusActivity.class)));

        findViewById(R.id.btn_nav_roast_persona).setOnClickListener(v ->
                startActivity(new Intent(this, com.cs364.roastapp.roast.RoastPersonaActivity.class)));

        findViewById(R.id.btn_roast_test).setOnClickListener(v ->
                com.cs364.roastapp.roast.RoastTestHelper.nextStep(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (findViewById(R.id.pid_monitoring_panel) == null) {
            return; // redirected to onboarding, no content view
        }
        boolean permissionsMissing = !com.cs364.roastapp.tracking.PermissionChecker.hasAll(this);
        com.cs364.roastapp.tracking.TrackedAppsRepository.IO.execute(() -> {
            boolean serviceOk = com.cs364.roastapp.tracking.TrackingServiceController.sync(this);
            java.util.List<com.cs364.roastapp.ui.AppLimitsUi.App> apps =
                    new com.cs364.roastapp.tracking.TrackedAppsRepository(this).loadTracked();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                renderAppUsage(apps);
                if (permissionsMissing) {
                    renderPermissionWarning(true);
                } else if (!serviceOk) {
                    renderMonitoringState(com.cs364.roastapp.ui.MonitoringStatusUi.State.STOPPED,
                            () -> com.cs364.roastapp.tracking.TrackedAppsRepository.IO.execute(
                                    () -> com.cs364.roastapp.tracking.TrackingServiceController.sync(this)));
                } else {
                    renderPermissionWarning(false);
                }
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RoastTrigger trigger = AppServices.getRoastTrigger(this);
        if (trigger instanceof com.cs364.roastapp.roast.RoastManager rm) {
            rm.resetPackage(null);
        }
    }
}
