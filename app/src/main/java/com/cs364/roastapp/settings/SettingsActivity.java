package com.cs364.roastapp.settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settingsManager;
    private RadioGroup languageGroup;
    private RadioButton radioLangTh;
    private RadioButton radioLangEn;
    private RadioGroup themeGroup;
    private RadioButton radioThemeDay;
    private RadioButton radioThemeNight;
    private MaterialSwitch demoModeSwitch;
    private MaterialSwitch voiceSwitch;

    private boolean isBindingUi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsManager = new SettingsManager(this);
        settingsManager.applySavedSettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        languageGroup = findViewById(R.id.radio_group_language);
        radioLangTh = findViewById(R.id.radio_lang_th);
        radioLangEn = findViewById(R.id.radio_lang_en);

        themeGroup = findViewById(R.id.radio_group_theme);
        radioThemeDay = findViewById(R.id.radio_theme_day);
        radioThemeNight = findViewById(R.id.radio_theme_night);

        demoModeSwitch = findViewById(R.id.switch_demo_mode);
        voiceSwitch = findViewById(R.id.switch_voice_alerts);

        com.cs364.roastapp.ui.PutItDownNavigation.bind(this, R.id.btn_nav_settings);
        android.view.View languageRow = findViewById(R.id.pid_language_row);
        ((TextView) findViewById(R.id.pid_current_language)).setText(
                SettingsManager.LANG_TH.equals(settingsManager.getLanguage()) ? "ไทย" : "English");
        findViewById(R.id.pid_reminder_preview).setOnClickListener(v ->
                com.cs364.roastapp.ui.ReminderPreviewUi.show(this));
        languageRow.setOnClickListener(v -> {
            com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(this);
            android.widget.LinearLayout content = new android.widget.LinearLayout(this);
            content.setOrientation(android.widget.LinearLayout.VERTICAL);
            int padding = (int) (24 * getResources().getDisplayMetrics().density);
            content.setPadding(0, padding, 0, padding / 2);
            TextView title = new TextView(this);
            title.setText(R.string.pid_language);
            title.setTextAppearance(R.style.TextAppearance_PutItDown_Section);
            title.setPadding(padding, 0, padding, padding / 2);
            content.addView(title);
            // Same row anatomy as the app picker: label on the left, a check on the current choice.
            for (String language : new String[]{SettingsManager.LANG_TH, SettingsManager.LANG_EN}) {
                android.view.View choice = getLayoutInflater().inflate(R.layout.pid_picker_row, content, false);
                ((TextView) choice.findViewById(R.id.pid_picker_name)).setText(
                        SettingsManager.LANG_TH.equals(language) ? "ไทย" : "English");
                android.widget.ImageView mark = choice.findViewById(R.id.pid_picker_icon);
                boolean current = settingsManager.getLanguage().equals(language);
                mark.setImageResource(R.drawable.pid_ic_check);
                mark.setImageTintList(android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(this, R.color.pid_secondary)));
                mark.setVisibility(current ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
                choice.setSelected(current);
                choice.setOnClickListener(view -> { sheet.dismiss(); settingsManager.setLanguage(language); });
                content.addView(choice);
            }
            sheet.setContentView(content);
            sheet.getBehavior().setSkipCollapsed(true);
            sheet.getBehavior().setState(
                    com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            sheet.show();
        });
        bindCurrentSettingsToUi();
        setupListeners();

        Button backBtn = findViewById(R.id.btn_settings_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }

    private void bindCurrentSettingsToUi() {
        isBindingUi = true;

        String currentLang = settingsManager.getLanguage();
        if (SettingsManager.LANG_TH.equalsIgnoreCase(currentLang)) {
            if (radioLangTh != null) {
                radioLangTh.setChecked(true);
            }
        } else {
            if (radioLangEn != null) {
                radioLangEn.setChecked(true);
            }
        }

        boolean isNight = settingsManager.isNightMode();
        if (isNight) {
            if (radioThemeNight != null) {
                radioThemeNight.setChecked(true);
            }
        } else {
            if (radioThemeDay != null) {
                radioThemeDay.setChecked(true);
            }
        }

        boolean isDemo = settingsManager.isDemoMode();
        if (demoModeSwitch != null) {
            demoModeSwitch.setChecked(isDemo);
        }
        updateThresholdsSummary(isDemo);
        voiceSwitch.setChecked(settingsManager.isVoiceEnabled());

        isBindingUi = false;
    }

    private void setupListeners() {
        // Language selection is handled by the pid_language_row BottomSheetDialog;
        // do not attach a listener to the hidden languageGroup, or onRestoreInstanceState
        // after AppCompatDelegate.setApplicationLocales() recreate() will revert the choice.

        if (themeGroup != null) {
            themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (isBindingUi) {
                    return;
                }
                if (checkedId == R.id.radio_theme_night) {
                    settingsManager.setNightMode(true);
                } else if (checkedId == R.id.radio_theme_day) {
                    settingsManager.setNightMode(false);
                }
            });
        }

        if (demoModeSwitch != null) {
            demoModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isBindingUi) {
                    return;
                }
                settingsManager.setDemoMode(isChecked);
                updateThresholdsSummary(isChecked);
            });
        }

        voiceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isBindingUi) {
                settingsManager.setVoiceEnabled(isChecked);
            }
        });
    }

    private void updateThresholdsSummary(boolean isDemoMode) {
        TextView summary = findViewById(R.id.pid_schedule_summary);
        summary.setText(isDemoMode ? R.string.pid_schedule_demo : R.string.pid_schedule_normal);
    }
}
