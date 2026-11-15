package com.cs364.roastapp.roast;

import android.content.Context;

import androidx.annotation.RawRes;

import com.cs364.roastapp.R;
import com.cs364.roastapp.settings.SettingsManager;

/**
 * The recorded reminder lines (docs/voice/README.md). Clip {@code i} says exactly
 * {@code R.array.roast_voice_lines[i]} in the same language, so text and audio always match.
 */
public final class VoiceClips {

    private static final int[] THAI = {
            R.raw.roast_th_01, R.raw.roast_th_02, R.raw.roast_th_03, R.raw.roast_th_04,
            R.raw.roast_th_05, R.raw.roast_th_06, R.raw.roast_th_07, R.raw.roast_th_08
    };

    private static final int[] ENGLISH = {
            R.raw.roast_en_01, R.raw.roast_en_02, R.raw.roast_en_03, R.raw.roast_en_04,
            R.raw.roast_en_05, R.raw.roast_en_06, R.raw.roast_en_07, R.raw.roast_en_08
    };

    private VoiceClips() {
    }

    public static int count() {
        return THAI.length;
    }

    /** Clip for line {@code index} in the app's current language. */
    @RawRes
    public static int clip(Context context, int index) {
        boolean thai = SettingsManager.LANG_TH.equals(new SettingsManager(context).getLanguage());
        int[] clips = thai ? THAI : ENGLISH;
        return clips[Math.floorMod(index, clips.length)];
    }
}
