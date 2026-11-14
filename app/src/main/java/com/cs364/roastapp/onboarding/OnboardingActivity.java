package com.cs364.roastapp.onboarding;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.HomeActivity;
import com.cs364.roastapp.tracking.PermissionChecker;

public class OnboardingActivity extends AppCompatActivity {

    private boolean usageRequested;
    private boolean notificationsRequested;

    private final ActivityResultLauncher<String> notificationLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> refresh());

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

    /** Permissions are changed in system screens, so re-read them every time we come back. */
    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        boolean usage = PermissionChecker.hasUsageAccess(this);
        boolean notifications = PermissionChecker.hasNotifications(this);
        bindPermissions(usage, notifications,
                this::requestUsageAccess, this::requestNotifications, this::continueHome);
        showHint(usage, notifications);
    }

    /** After a refused/ignored request, say what to do next; otherwise show the normal note. */
    private void showHint(boolean usage, boolean notifications) {
        TextView note = findViewById(R.id.pid_onboarding_note);
        StringBuilder hint = new StringBuilder();
        if (usageRequested && !usage) {
            hint.append(getString(R.string.onboarding_hint_usage));
        }
        if (notificationsRequested && !notifications) {
            if (hint.length() > 0) {
                hint.append("\n\n");
            }
            hint.append(getString(R.string.onboarding_hint_notifications));
        }
        note.setText(hint.length() > 0 ? hint.toString() : getString(R.string.pid_onboarding_note));
    }

    private void requestUsageAccess() {
        usageRequested = true;
        try {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,
                    Uri.parse("package:" + getPackageName())));
        } catch (ActivityNotFoundException | SecurityException e) {
            try {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            } catch (ActivityNotFoundException ignored) {
                refresh(); // no settings screen on this device; the hint explains the manual steps
            }
        }
    }

    private void requestNotifications() {
        notificationsRequested = true;
        if (Build.VERSION.SDK_INT >= 33) {
            boolean canAsk = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                    || !OnboardingPrefs.notificationAsked(this);
            if (canAsk) {
                OnboardingPrefs.markNotificationAsked(this);
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        openNotificationSettings();
    }

    private void openNotificationSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
        } catch (ActivityNotFoundException e) {
            refresh();
        }
    }

    private void continueHome() {
        if (!PermissionChecker.hasAll(this)) {
            refresh();
            return;
        }
        OnboardingPrefs.markDone(this);
        startActivity(new Intent(this, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }
}
