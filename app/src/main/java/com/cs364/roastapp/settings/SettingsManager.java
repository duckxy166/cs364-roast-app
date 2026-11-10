package com.cs364.roastapp.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.cs364.roastapp.core.AlertThresholds;
import com.cs364.roastapp.core.AppServices;

import java.util.Locale;

public class SettingsManager {

    public static final String KEY_LANGUAGE = "app_language";
    public static final String KEY_NIGHT_MODE = "app_night_mode";

    public static final String LANG_EN = "en";
    public static final String LANG_TH = "th";

    private final Context appContext;
    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = this.appContext.getSharedPreferences(
                AlertThresholds.PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    public String getLanguage() {
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        Locale firstLocale = currentLocales.isEmpty() ? null : currentLocales.get(0);
        if (firstLocale != null) {
            String lang = firstLocale.getLanguage();
            if (LANG_TH.equalsIgnoreCase(lang)) {
                return LANG_TH;
            }
            if (LANG_EN.equalsIgnoreCase(lang)) {
                return LANG_EN;
            }
        }
        String saved = prefs.getString(KEY_LANGUAGE, null);
        if (saved != null) {
            return LANG_TH.equalsIgnoreCase(saved) ? LANG_TH : LANG_EN;
        }
        String defaultLang = Locale.getDefault().getLanguage();
        return LANG_TH.equalsIgnoreCase(defaultLang) ? LANG_TH : LANG_EN;
    }

    public void setLanguage(String languageTag) {
        String normalized = LANG_TH.equalsIgnoreCase(languageTag) ? LANG_TH : LANG_EN;
        prefs.edit().putString(KEY_LANGUAGE, normalized).apply();
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(normalized);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    public boolean isNightMode() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            return true;
        }
        if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            return false;
        }
        return prefs.getBoolean(KEY_NIGHT_MODE, false);
    }

    public void setNightMode(boolean nightMode) {
        prefs.edit().putBoolean(KEY_NIGHT_MODE, nightMode).apply();
        int targetMode = nightMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }
    }

    public boolean isDemoMode() {
        return AppServices.getAlertThresholds(appContext).isDemoMode();
    }

    public void setDemoMode(boolean enabled) {
        AppServices.getAlertThresholds(appContext).setDemoMode(enabled);
    }

    public void applySavedSettings() {
        boolean savedNight = prefs.getBoolean(KEY_NIGHT_MODE, false);
        int targetMode = savedNight
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }

        String savedLang = prefs.getString(KEY_LANGUAGE, null);
        if (savedLang != null) {
            LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
            Locale firstLocale = currentLocales.isEmpty() ? null : currentLocales.get(0);
            String currentTag = firstLocale != null ? firstLocale.getLanguage() : "";
            if (!savedLang.equalsIgnoreCase(currentTag)) {
                AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(savedLang)
                );
            }
        }
    }
}
