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
        findViewById(R.id.btn_nav_onboarding).setVisibility(missing ? android.view.View.VISIBLE : android.view.View.GONE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    protected void onStart() {
        super.onStart();
        RoastTrigger trigger = AppServices.getRoastTrigger(this);
        if (trigger instanceof com.cs364.roastapp.roast.RoastManager rm) {
            rm.resetPackage(null);
        }
    }
}
