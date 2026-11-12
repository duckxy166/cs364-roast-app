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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.core_activity_home);

        Button btnOnboarding = findViewById(R.id.btn_nav_onboarding);
        Button btnTracking = findViewById(R.id.btn_nav_tracking);
        Button btnFocus = findViewById(R.id.btn_nav_focus);
        Button btnSettings = findViewById(R.id.btn_nav_settings);

        btnOnboarding.setOnClickListener(v ->
                startActivity(new Intent(this, OnboardingActivity.class)));
        btnTracking.setOnClickListener(v ->
                startActivity(new Intent(this, TrackedAppsActivity.class)));
        btnFocus.setOnClickListener(v ->
                startActivity(new Intent(this, FocusActivity.class)));
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

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
