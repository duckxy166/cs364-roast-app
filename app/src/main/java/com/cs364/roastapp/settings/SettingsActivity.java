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
    private TextView thresholdsSummaryText;

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
        thresholdsSummaryText = findViewById(R.id.text_settings_thresholds_summary);

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

        isBindingUi = false;
    }

    private void setupListeners() {
        if (languageGroup != null) {
            languageGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (isBindingUi) {
                    return;
                }
                if (checkedId == R.id.radio_lang_th) {
                    settingsManager.setLanguage(SettingsManager.LANG_TH);
                } else if (checkedId == R.id.radio_lang_en) {
                    settingsManager.setLanguage(SettingsManager.LANG_EN);
                }
            });
        }

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
    }

    private void updateThresholdsSummary(boolean isDemoMode) {
        if (thresholdsSummaryText != null) {
            thresholdsSummaryText.setText(
                    isDemoMode
                            ? R.string.settings_thresholds_summary_demo
                            : R.string.settings_thresholds_summary_normal
            );
        }
    }
}
