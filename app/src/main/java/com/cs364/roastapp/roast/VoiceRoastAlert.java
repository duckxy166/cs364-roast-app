package com.cs364.roastapp.roast;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;

import com.cs364.roastapp.core.AlertThresholds;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.settings.SettingsManager;

import java.util.Random;

/**
 * The over-limit reminder: notification + vibration + a voice line, the moment an app goes over
 * its limit, repeated every {@link AlertThresholds#getRepeatIntervalMillis()} while it stays in
 * front. Repeats are driven by the tracking service's checks, so nothing fires after the user
 * has left the app.
 */
public class VoiceRoastAlert {
    private final Context appContext;
    private final RoastNotifier notifier;
    private final Random random = new Random();

    private MediaPlayer mediaPlayer;
    private AudioFocusRequest audioFocusRequest;
    private int lastLineIndex = -1;
    private long lastAlertMillis = 0L;
    private String activePackageName;

    public VoiceRoastAlert(Context context, RoastNotifier notifier) {
        this.appContext = context.getApplicationContext();
        this.notifier = notifier;
    }

    /** Called on every tracking check while {@code packageName} is in front and over its limit. */
    public synchronized void onOverLimit(String packageName) {
        if (packageName == null) {
            return;
        }
        if (!packageName.equals(activePackageName)) {
            stop(activePackageName);
            activePackageName = packageName;
        }
        long interval = AppServices.getAlertThresholds(appContext).getRepeatIntervalMillis();
        long now = System.currentTimeMillis();
        if (lastAlertMillis == 0L || now - lastAlertMillis >= interval) {
            alert(packageName);
        }
    }

    /** The user left the app (or the day reset): end the cycle so the next overrun alerts at once. */
    public synchronized void stop(String packageName) {
        if (packageName != null && !packageName.equals(activePackageName)) {
            return;
        }
        releasePlayer();
        activePackageName = null;
        lastAlertMillis = 0L;
    }

    /** The notification was swiped away: stop this clip, but keep the repeat schedule. */
    public synchronized void silence(String packageName) {
        if (packageName == null || packageName.equals(activePackageName)) {
            releasePlayer();
        }
    }

    private void alert(String packageName) {
        int count = VoiceClips.count();
        int index = random.nextInt(count);
        if (count > 1 && index == lastLineIndex) {
            index = (index + 1) % count;
        }
        lastLineIndex = index;
        lastAlertMillis = System.currentTimeMillis();

        notifier.showAlert(packageName, index);

        if (!new SettingsManager(appContext).isVoiceEnabled() || isSilentOrDnd()) {
            return;
        }
        play(index);
    }

    private void play(int index) {
        releasePlayer();
        requestAudioFocus();
        try {
            mediaPlayer = VoiceClips.createPlayer(appContext, index);
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
        NotificationManager nm = appContext.getSystemService(NotificationManager.class);
        if (nm != null) {
            int filter = nm.getCurrentInterruptionFilter();
            return filter == NotificationManager.INTERRUPTION_FILTER_NONE
                    || filter == NotificationManager.INTERRUPTION_FILTER_ALARMS
                    || filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY;
        }
        return false;
    }

    private void requestAudioFocus() {
        AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(VoiceClips.PLAYBACK_ATTRIBUTES)
                    .build();
            am.requestAudioFocus(audioFocusRequest);
        } else {
            am.requestAudioFocus(null, VoiceClips.VOLUME_STREAM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
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
