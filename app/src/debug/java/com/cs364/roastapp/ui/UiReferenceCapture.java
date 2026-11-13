package com.cs364.roastapp.ui;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.cs364.roastapp.R;
import com.cs364.roastapp.core.HomeActivity;
import com.cs364.roastapp.focus.FocusActivity;
import com.cs364.roastapp.onboarding.OnboardingActivity;
import com.cs364.roastapp.settings.SettingsActivity;
import com.cs364.roastapp.settings.SettingsManager;
import com.cs364.roastapp.tracking.TrackedAppsActivity;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Explicit debug instrumentation: reference fixtures never load in the normal app. */
public final class UiReferenceCapture extends Instrumentation {
    private Bundle options;
    private String prefix;
    private final List<Activity> opened = new ArrayList<>();
    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); options = arguments; start(); }
    @Override public void onStart() {
        SettingsManager manager = new SettingsManager(getTargetContext());
        String initialLanguage = manager.getLanguage(); boolean initialNight = manager.isNightMode();
        prefix = options.getString("prefix", "phone-day");
        Bundle result = new Bundle();
        try {
            runOnMainSync(() -> { manager.setLanguage(options.getString("language", "th"));
                manager.setNightMode("night".equals(options.getString("theme", "day"))); });
            pause();
            HomeActivity home = launch(HomeActivity.class);
            List<AppLimitsUi.App> examples = Arrays.asList(
                new AppLimitsUi.App("fixture.instagram", "Instagram", home.getDrawable(R.drawable.pid_ic_apps), 18, 30),
                new AppLimitsUi.App("fixture.youtube", "YouTube", home.getDrawable(R.drawable.pid_ic_play), 12, 45),
                new AppLimitsUi.App("fixture.tiktok", "TikTok", home.getDrawable(R.drawable.pid_ic_apps), 0, 30));
            runOnMainSync(() -> home.renderAppUsage(examples)); capture("home");
            runOnMainSync(() -> home.renderPermissionWarning(true)); capture("home-permission");
            runOnMainSync(() -> { home.renderPermissionWarning(false); home.renderAppUsage(java.util.Collections.emptyList()); }); capture("home-empty");
            TrackedAppsActivity apps = launch(TrackedAppsActivity.class);
            List<AppLimitsUi.App> tracked = new ArrayList<>(examples);
            AppLimitsUi.Actions actions = new AppLimitsUi.Actions() {
                public void saveLimit(AppLimitsUi.App app, long minutes) {
                    tracked.removeIf(item -> item.packageName.equals(app.packageName));
                    tracked.add(new AppLimitsUi.App(app.packageName, app.name, app.icon, app.usedMinutes, minutes));
                    apps.bindUi(tracked, examples, this);
                }
                public void remove(AppLimitsUi.App app) {
                    tracked.removeIf(item -> item.packageName.equals(app.packageName)); apps.bindUi(tracked, examples, this);
                }
            };
            runOnMainSync(() -> apps.bindUi(tracked, examples, actions)); capture("apps");
            AppLimitsUi ui = new AppLimitsUi(apps, actions);
            runOnMainSync(() -> { ui.setAvailableApps(examples); ui.showPicker(); }); capture("app-picker"); back();
            runOnMainSync(() -> ui.showLimit(examples.get(0))); capture("daily-limit"); back();
            runOnMainSync(() -> apps.findViewById(R.id.pid_app_remove).performClick()); capture("remove-confirm"); back();
            runOnMainSync(() -> apps.bindUi(java.util.Collections.emptyList(),examples,actions)); capture("apps-empty");
            OnboardingActivity onboarding = launch(OnboardingActivity.class); capture("onboarding");
            FocusActivity focus = launch(FocusActivity.class); capture("focus");
            runOnMainSync(() -> ((EditText)focus.findViewById(R.id.edit_focus_minutes)).setText("60"));
            for (int id : new int[]{R.id.btn_preset_1,R.id.btn_preset_5,R.id.btn_preset_15,R.id.btn_preset_25}) {
                final boolean[] checked = {true}; runOnMainSync(() -> checked[0] = ((MaterialButton)focus.findViewById(id)).isChecked());
                if (checked[0]) throw new AssertionError("Custom duration left a stale preset selected");
            }
            capture("focus-custom"); click(focus,R.id.btn_preset_1); click(focus,R.id.btn_focus_start); capture("focus-running");
            click(focus,R.id.btn_focus_back); capture("focus-exit-confirm"); back();
            SettingsActivity settings = launch(SettingsActivity.class); capture("settings");
            click(settings,R.id.pid_language_row); capture("settings-language"); back();
            click(settings,R.id.pid_thresholds_toggle); capture("settings-thresholds");
            click(settings,R.id.switch_demo_mode); capture("settings-demo"); click(settings,R.id.switch_demo_mode);
            runOnMainSync(() -> ReminderPreviewUi.show(settings)); capture("reminder-preview"); back();
            result.putString("stream", "UI reference captures and custom-duration state checks passed: " + prefix);
        } catch (Throwable error) {
            result.putString("stream", android.util.Log.getStackTraceString(error)); result.putString("error",error.toString());
        } finally {
            runOnMainSync(() -> { for (Activity activity : opened) if (!activity.isFinishing()) activity.finish();
                manager.setDemoMode(false); manager.setLanguage(initialLanguage); manager.setNightMode(initialNight); });
        }
        finish(result.containsKey("error") ? 1 : Activity.RESULT_OK, result);
    }
    private <T extends Activity> T launch(Class<T> type) {
        Activity activity = startActivitySync(new Intent(getTargetContext(),type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        opened.add(activity); pause(); return type.cast(activity);
    }
    private void click(Activity activity, int id) {
        runOnMainSync(() -> activity.findViewById(id).performClick()); pause();
    }
    private void back() { sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); pause(); }
    private void pause() { try { Thread.sleep(550); } catch (InterruptedException error) { Thread.currentThread().interrupt(); } }
    private void capture(String name) throws Exception {
        pause(); Bitmap bitmap = getUiAutomation().takeScreenshot();
        if (bitmap == null) throw new IllegalStateException("Screenshot unavailable");
        File directory = getTargetContext().getExternalFilesDir("ui-reference");
        try (FileOutputStream stream = new FileOutputStream(new File(directory,prefix+"-"+name+".png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        }
        bitmap.recycle();
    }
}
