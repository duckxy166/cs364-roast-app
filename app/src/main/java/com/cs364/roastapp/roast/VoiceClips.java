package com.cs364.roastapp.roast;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;

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

    /**
     * Voice lines play at the alarm volume, not the media volume: people keep media low while
     * scrolling, and a reminder that is as quiet as the video it interrupts is easy to miss.
     * The preview uses the same attributes so it sounds exactly like a real reminder.
     */
    public static final AudioAttributes PLAYBACK_ATTRIBUTES = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build();

    /** The volume stream that {@link #PLAYBACK_ATTRIBUTES} is controlled by. */
    public static final int VOLUME_STREAM = AudioManager.STREAM_ALARM;

    private VoiceClips() {
    }

    /** A prepared player for line {@code index}, or null if the clip cannot be loaded. */
    public static MediaPlayer createPlayer(Context context, int index) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int session = audio != null ? audio.generateAudioSessionId() : AudioManager.AUDIO_SESSION_ID_GENERATE;
        return MediaPlayer.create(context, clip(context, index), PLAYBACK_ATTRIBUTES, session);
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
