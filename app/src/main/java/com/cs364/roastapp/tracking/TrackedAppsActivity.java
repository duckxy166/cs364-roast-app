package com.cs364.roastapp.tracking;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;
import com.cs364.roastapp.ui.AppLimitsUi;

import java.util.List;

public class TrackedAppsActivity extends AppCompatActivity {
    private AppLimitsUi ui;
    private TrackedAppsRepository repository;

    private final AppLimitsUi.Actions actions = new AppLimitsUi.Actions() {
        @Override
        public void saveLimit(AppLimitsUi.App app, long minutes) {
            TrackedAppsRepository.IO.execute(() -> {
                repository.saveLimit(app.packageName, minutes);
                TrackingServiceController.sync(TrackedAppsActivity.this);
                reload();
            });
        }

        @Override
        public void remove(AppLimitsUi.App app) {
            TrackedAppsRepository.IO.execute(() -> {
                repository.remove(app.packageName);
                TrackingServiceController.sync(TrackedAppsActivity.this);
                reload();
            });
        }
    };

    public void bindUi(List<AppLimitsUi.App> tracked, List<AppLimitsUi.App> available,
            AppLimitsUi.Actions actions) {
        ui = new AppLimitsUi(this, actions);
        ui.setAvailableApps(available);
        ui.render(tracked, false);
        findViewById(R.id.pid_apps_add).setOnClickListener(v -> ui.showPicker());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_activity);
        repository = new TrackedAppsRepository(this);

        com.cs364.roastapp.ui.PutItDownNavigation.bind(this, R.id.btn_nav_tracking);
        bindUi(java.util.Collections.emptyList(), java.util.Collections.emptyList(), actions);
        Button backBtn = findViewById(R.id.btn_tracking_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TrackedAppsRepository.IO.execute(this::reload);
    }

    /** Loads from the database on the worker thread, then renders on the UI thread. */
    private void reload() {
        List<AppLimitsUi.App> tracked = repository.loadTracked();
        List<AppLimitsUi.App> available = repository.loadAvailable();
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                bindUi(tracked, available, actions);
            }
        });
    }
}
