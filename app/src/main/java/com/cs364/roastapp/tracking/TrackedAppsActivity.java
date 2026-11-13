package com.cs364.roastapp.tracking;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;

public class TrackedAppsActivity extends AppCompatActivity {
    private com.cs364.roastapp.ui.AppLimitsUi ui;
    public void bindUi(java.util.List<com.cs364.roastapp.ui.AppLimitsUi.App> tracked,
            java.util.List<com.cs364.roastapp.ui.AppLimitsUi.App> available,
            com.cs364.roastapp.ui.AppLimitsUi.Actions actions) {
        ui = new com.cs364.roastapp.ui.AppLimitsUi(this, actions);
        ui.setAvailableApps(available);
        ui.render(tracked, false);
        findViewById(R.id.pid_apps_add).setOnClickListener(v -> ui.showPicker());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_activity);

        com.cs364.roastapp.ui.PutItDownNavigation.bind(this, R.id.btn_nav_tracking);
        bindUi(java.util.Collections.emptyList(), java.util.Collections.emptyList(), null);
        Button backBtn = findViewById(R.id.btn_tracking_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }
}
