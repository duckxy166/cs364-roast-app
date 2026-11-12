package com.cs364.roastapp.roast;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AlertThresholds;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.settings.SettingsManager;

import java.util.Random;

public class VoiceRoastAlert implements Level3Alert {

    private static final int[] THAI_VOICE_RES_IDS = {
            R.raw.roast_th_01,
            R.raw.roast_th_02,
            R.raw.roast_th_03,
            R.raw.roast_th_04,
            R.raw.roast_th_05,
            R.raw.roast_th_06,
            R.raw.roast_th_07,
            R.raw.roast_th_08
    };

    private static final int[] ENGLISH_VOICE_RES_IDS = {
            R.raw.roast_en_01,
            R.raw.roast_en_02,
            R.raw.roast_en_03,
            R.raw.roast_en_04,
            R.raw.roast_en_05,
            R.raw.roast_en_06,
            R.raw.roast_en_07,
            R.raw.roast_en_08
    };

    private final Context appContext;
    private final RoastNotifier notifier;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private MediaPlayer mediaPlayer;
    private AudioFocusRequest audioFocusRequest;
    private int lastLineIndex = -1;
    private long lastPlayedTimeMillis = 0L;
    private String activePackageName;
    private Runnable repeatRunnable;

    public VoiceRoastAlert(Context context, RoastNotifier notifier) {
        this.appContext = context.getApplicationContext();
        this.notifier = notifier;
    }

    @Override
    public synchronized void onLevel3(String packageName, long millisOver) {
        if (packageName == null) {
            return;
        }
        this.activePackageName = packageName;
        AlertThresholds thresholds = AppServices.getAlertThresholds(appContext);
        long now = System.currentTimeMillis();
        long interval = thresholds.getRepeatVoiceIntervalMillis();

        if (lastPlayedTimeMillis == 0L || (now - lastPlayedTimeMillis >= interval)) {
            playVoiceAndNotify(packageName);
        }

        scheduleRepeat(packageName, interval);
    }

    @Override
    public synchronized void stop(String packageName) {
        if (packageName != null && !packageName.equals(activePackageName)) {
            return;
        }
        cancelRepeat();
        releasePlayer();
        activePackageName = null;
        lastPlayedTimeMillis = 0L;
    }

    private synchronized void scheduleRepeat(String packageName, long interval) {
        cancelRepeat();
        repeatRunnable = () -> {
            synchronized (VoiceRoastAlert.this) {
                if (packageName.equals(activePackageName)) {
                    playVoiceAndNotify(packageName);
                    scheduleRepeat(packageName, interval);
                }
            }
        };
        handler.postDelayed(repeatRunnable, interval);
    }

    private synchronized void cancelRepeat() {
        if (repeatRunnable != null) {
            handler.removeCallbacks(repeatRunnable);
            repeatRunnable = null;
        }
    }

    private synchronized void playVoiceAndNotify(String packageName) {
        String lang = new SettingsManager(appContext).getLanguage();
        boolean isThai = SettingsManager.LANG_TH.equalsIgnoreCase(lang);

        int count = isThai ? THAI_VOICE_RES_IDS.length : ENGLISH_VOICE_RES_IDS.length;
        if (count == 0) {
            return;
        }

        int index = random.nextInt(count);
        if (count > 1 && index == lastLineIndex) {
            index = (index + 1) % count;
        }
        lastLineIndex = index;
        lastPlayedTimeMillis = System.currentTimeMillis();

        String line = notifier.showLevel3(packageName, index);

        if (isSilentOrDnd()) {
            return;
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return;
        }

        releasePlayer();

        int soundResId = isThai ? THAI_VOICE_RES_IDS[index] : ENGLISH_VOICE_RES_IDS[index];
        requestAudioFocus();

        try {
            mediaPlayer = MediaPlayer.create(appContext, soundResId);
            if (mediaPlayer == null) {
                abandonAudioFocus();
                return;
            }
            mediaPlayer.setOnCompletionListener(mp -> {
                synchronized (VoiceRoastAlert.this) {
                    releasePlayer();
                }
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                synchronized (VoiceRoastAlert.this) {
                    releasePlayer();
                }
                return true;
            });
            mediaPlayer.start();
        } catch (Exception e) {
            releasePlayer();
        }
    }

    private boolean isSilentOrDnd() {
        AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            int ringer = am.getRingerMode();
            if (ringer == AudioManager.RINGER_MODE_SILENT || ringer == AudioManager.RINGER_MODE_VIBRATE) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = appContext.getSystemService(NotificationManager.class);
            if (nm != null) {
                int filter = nm.getCurrentInterruptionFilter();
                if (filter == NotificationManager.INTERRUPTION_FILTER_NONE
                        || filter == NotificationManager.INTERRUPTION_FILTER_ALARMS
                        || filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
                    return true;
                }
            }
        }
        return false;
    }

    private void requestAudioFocus() {
        AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .build();
            am.requestAudioFocus(audioFocusRequest);
        } else {
            am.requestAudioFocus(null, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
    }

    private void abandonAudioFocus() {
        AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                am.abandonAudioFocusRequest(audioFocusRequest);
                audioFocusRequest = null;
            }
        } else {
            am.abandonAudioFocus(null);
        }
    }

    private synchronized void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
        abandonAudioFocus();
    }
}
