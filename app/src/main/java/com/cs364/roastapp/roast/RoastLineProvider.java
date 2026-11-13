package com.cs364.roastapp.roast;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.ArrayRes;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AlertThresholds;
import com.cs364.roastapp.core.AppServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RoastLineProvider {

    public static final String KEY_PERSONA = "roast_persona";
    public static final String PERSONA_STRICT = "strict";
    public static final String PERSONA_SARCASTIC = "sarcastic";
    public static final String PERSONA_MOM = "mom";

    private static volatile RoastLineProvider instance;

    private final Context appContext;
    private final SharedPreferences prefs;
    private final Random random = new Random();
    private String lastLine;

    private RoastLineProvider(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = this.appContext.getSharedPreferences(
                AlertThresholds.PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    public static RoastLineProvider getInstance(Context context) {
        if (instance == null) {
            synchronized (RoastLineProvider.class) {
                if (instance == null) {
                    instance = new RoastLineProvider(context);
                }
            }
        }
        return instance;
    }

    public String getSelectedPersona() {
        String saved = prefs.getString(KEY_PERSONA, PERSONA_STRICT);
        if (PERSONA_SARCASTIC.equals(saved) || PERSONA_MOM.equals(saved)) {
            return saved;
        }
        return PERSONA_STRICT;
    }

    public void setSelectedPersona(String persona) {
        String normalized = PERSONA_STRICT;
        if (PERSONA_SARCASTIC.equals(persona) || PERSONA_MOM.equals(persona)) {
            normalized = persona;
        }
        prefs.edit().putString(KEY_PERSONA, normalized).apply();
        lastLine = null;
    }

    public synchronized String getNextRoastLine(Context localizedContext) {
        List<String> pool = new ArrayList<>();
        String[] personaLines = localizedContext.getResources()
                .getStringArray(getArrayResForPersona(getSelectedPersona()));
        Collections.addAll(pool, personaLines);

        List<CustomRoastEntity> customItems = AppServices.getDatabase(appContext)
                .customRoastDao()
                .getAll();
        for (CustomRoastEntity item : customItems) {
            if (item.text != null && !item.text.trim().isEmpty()) {
                pool.add(item.text.trim());
            }
        }

        if (pool.isEmpty()) {
            return "";
        }
        int index = random.nextInt(pool.size());
        String picked = pool.get(index);
        if (pool.size() > 1 && picked.equals(lastLine)) {
            picked = pool.get((index + 1) % pool.size());
        }
        lastLine = picked;
        return picked;
    }

    @ArrayRes
    private static int getArrayResForPersona(String persona) {
        if (PERSONA_SARCASTIC.equals(persona)) {
            return R.array.roast_persona_sarcastic_lines;
        }
        if (PERSONA_MOM.equals(persona)) {
            return R.array.roast_persona_mom_lines;
        }
        return R.array.roast_persona_strict_lines;
    }
}
