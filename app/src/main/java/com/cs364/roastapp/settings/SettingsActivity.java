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
            sheet.show();
        });
        findViewById(R.id.pid_thresholds_toggle).setOnClickListener(v -> {
            boolean expanded = thresholdsSummaryText.getVisibility() == android.view.View.VISIBLE;
            thresholdsSummaryText.setVisibility(expanded ? android.view.View.GONE : android.view.View.VISIBLE);
            ((Button) v).setText(expanded ? R.string.pid_show_thresholds : R.string.pid_hide_thresholds);
        });
        bindCurrentSettingsToUi();
        setupListeners();

        Button backBtn = findViewById(R.id.btn_settings_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }

    @Override protected void onResume(){
        super.onResume();
        String persona=com.cs364.roastapp.roast.RoastLineProvider.getInstance(this).getSelectedPersona();
        int res=com.cs364.roastapp.roast.RoastLineProvider.PERSONA_MOM.equals(persona)?R.string.pid_persona_mom:com.cs364.roastapp.roast.RoastLineProvider.PERSONA_SARCASTIC.equals(persona)?R.string.pid_persona_sarcastic:R.string.pid_persona_strict;
        String name=getString(res).split("\\n|\n",2)[0];
        Button row=findViewById(R.id.btn_nav_roast_persona);
        ((TextView)findViewById(R.id.pid_persona_current)).setText(name);
        row.setContentDescription(getString(R.string.review_style_selected,name));
        row.setOnClickListener(v->startActivity(new android.content.Intent(this,com.cs364.roastapp.roast.RoastPersonaActivity.class)));
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
    }

    private void updateThresholdsSummary(boolean isDemoMode) {
        TextView summary = findViewById(R.id.pid_schedule_summary);
        summary.setText(isDemoMode ? R.string.pid_schedule_demo : R.string.pid_schedule_normal);
        if (thresholdsSummaryText != null) {
            thresholdsSummaryText.setVisibility(isDemoMode ? android.view.View.VISIBLE : android.view.View.GONE);
            ((Button) findViewById(R.id.pid_thresholds_toggle)).setText(
                    isDemoMode ? R.string.pid_hide_thresholds : R.string.pid_show_thresholds);
            thresholdsSummaryText.setText(
                    isDemoMode
                            ? R.string.settings_thresholds_summary_demo
                            : R.string.settings_thresholds_summary_normal
            );
        }
    }
}
