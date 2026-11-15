package com.cs364.roastapp.ui;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AlertThresholds;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.roast.VoiceClips;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

/**
 * Shows what a reminder looks like and lets the user hear each voice line. Plays on the media
 * stream whatever the ringer mode is (the user asked to hear it), and never posts a notification.
 */
public final class ReminderPreviewUi {
    private static final String SAMPLE_APP = "Instagram";

    private ReminderPreviewUi() {}

    public static void show(Activity activity) {
        new Session(activity).open();
    }

    private static final class Session {
        private final Activity activity;
        private final BottomSheetDialog sheet;
        private final View content;
        private final String[] lines;
        private final MaterialButton play;
        private MediaPlayer player;
        private int index = 0;

        Session(Activity activity) {
            this.activity = activity;
            this.sheet = new BottomSheetDialog(activity);
            this.content = LayoutInflater.from(activity).inflate(R.layout.pid_reminder_preview, null, false);
            this.lines = activity.getResources().getStringArray(R.array.roast_voice_lines);
            this.play = content.findViewById(R.id.pid_preview_play);
        }

        void open() {
            ((TextView) content.findViewById(R.id.pid_preview_notification_title))
                    .setText(activity.getString(R.string.roast_alert_title, SAMPLE_APP));
            AlertThresholds thresholds = AppServices.getAlertThresholds(activity);
            ((TextView) content.findViewById(R.id.pid_preview_delivery)).setText(
                    thresholds.isDemoMode() ? R.string.pid_preview_delivery_demo : R.string.pid_preview_delivery);
            play.setOnClickListener(v -> { if (player != null) stop(); else start(); });
            content.findViewById(R.id.pid_preview_next).setOnClickListener(v -> {
                boolean wasPlaying = player != null;
                stop();
                index = (index + 1) % lines.length;
                render();
                if (wasPlaying) start();
            });
            content.findViewById(R.id.pid_preview_close).setOnClickListener(v -> sheet.dismiss());
            sheet.setOnDismissListener(d -> stop());
            render();
            sheet.setContentView(content);
            sheet.show();
        }

        private void render() {
            ((TextView) content.findViewById(R.id.pid_preview_notification_body)).setText(lines[index]);
            ((TextView) content.findViewById(R.id.pid_preview_counter))
                    .setText(activity.getString(R.string.pid_preview_counter, index + 1, lines.length));
        }

        private void start() {
            AudioManager audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            boolean muted = audio != null && audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
            content.findViewById(R.id.pid_preview_volume).setVisibility(muted ? View.VISIBLE : View.GONE);
            AudioAttributes media = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            int session = audio != null ? audio.generateAudioSessionId() : AudioManager.AUDIO_SESSION_ID_GENERATE;
            player = MediaPlayer.create(activity, VoiceClips.clip(activity, index), media, session);
            if (player == null) return;
            player.setOnCompletionListener(mp -> stop());
            player.setOnErrorListener((mp, what, extra) -> { stop(); return true; });
            player.start();
            play.setText(R.string.pid_preview_stop);
            play.setIconResource(R.drawable.pid_ic_stop);
        }

        private void stop() {
            if (player != null) {
                try {
                    if (player.isPlaying()) player.stop();
                    player.release();
                } catch (IllegalStateException ignored) {
                    // Already released by the framework after an error.
                }
                player = null;
            }
            play.setText(R.string.pid_preview_play);
            play.setIconResource(R.drawable.pid_ic_play);
        }
    }
}
