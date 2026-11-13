package com.cs364.roastapp.focus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.ConfirmDialog;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FocusActivity extends AppCompatActivity {

    private static final String KEY_IS_SESSION_ACTIVE = "key_is_session_active";
    private static final String KEY_REMAINING_MILLIS = "key_remaining_millis";
    private static final String KEY_SELECTED_MINUTES = "key_selected_minutes";
    private static final String KEY_WAS_PAUSED_DURING_SESSION = "key_was_paused_during_session";

    private static final long DEFAULT_MINUTES = 25L;
    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long TICK_INTERVAL_MILLIS = 100L;

    private TextView timerText;
    private TextView statusText;
    private View durationControlsLayout;
    private EditText minutesEdit;
    private Button startBtn;
    private Button abandonBtn;
    private TextView completedCountText;
    private TextView historyListText;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private boolean isSessionActive = false;
    private boolean isTickerRunning = false;
    private boolean wasPausedDuringSession = false;
    private long selectedDurationMinutes = DEFAULT_MINUTES;
    private long remainingMillis = DEFAULT_MINUTES * MILLIS_PER_MINUTE;
    private long targetEndElapsedRealtime = 0L;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isSessionActive || !isTickerRunning) {
                return;
            }
            long now = SystemClock.elapsedRealtime();
            remainingMillis = Math.max(0L, targetEndElapsedRealtime - now);
            updateTimerDisplay(remainingMillis);

            if (remainingMillis <= 0L) {
                onFocusSessionCompleted();
            } else {
                timerHandler.postDelayed(this, TICK_INTERVAL_MILLIS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_activity);

        timerText = findViewById(R.id.text_focus_timer);
        statusText = findViewById(R.id.text_focus_status);
        durationControlsLayout = findViewById(R.id.layout_focus_duration_controls);
        minutesEdit = findViewById(R.id.edit_focus_minutes);
        startBtn = findViewById(R.id.btn_focus_start);
        abandonBtn = findViewById(R.id.btn_focus_abandon);
        completedCountText = findViewById(R.id.text_focus_completed_count);
        historyListText = findViewById(R.id.text_focus_history_list);

        com.cs364.roastapp.ui.PutItDownNavigation.bind(this, R.id.btn_nav_focus, destination -> {
            Runnable navigate = () -> com.cs364.roastapp.ui.PutItDownNavigation.open(this, destination);
            if (isSessionActive) {
                ConfirmDialog.show(this, R.string.focus_confirm_abandon_title,
                        R.string.focus_confirm_abandon_message,
                        () -> { abandonActiveSession(); navigate.run(); }, () -> {});
            } else { navigate.run(); }
        });
        setupPresetButtons();
        setupMinutesInputListener();

        if (savedInstanceState != null) {
            isSessionActive = savedInstanceState.getBoolean(KEY_IS_SESSION_ACTIVE, false);
            selectedDurationMinutes = savedInstanceState.getLong(KEY_SELECTED_MINUTES, DEFAULT_MINUTES);
            remainingMillis = savedInstanceState.getLong(
                    KEY_REMAINING_MILLIS,
                    selectedDurationMinutes * MILLIS_PER_MINUTE
            );
            wasPausedDuringSession = savedInstanceState.getBoolean(KEY_WAS_PAUSED_DURING_SESSION, false);
        }

        if (startBtn != null) {
            startBtn.setOnClickListener(v -> startFocusSession());
        }

        if (abandonBtn != null) {
            abandonBtn.setOnClickListener(v -> confirmAbandonSession(false));
        }

        Button backBtn = findViewById(R.id.btn_focus_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> handleBackNavigation());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });

        syncUiWithState();
        refreshSessionHistory();
    }

    private void setupPresetButtons() {
        bindPresetButton(R.id.btn_preset_1, 1L);
        bindPresetButton(R.id.btn_preset_5, 5L);
        bindPresetButton(R.id.btn_preset_15, 15L);
        bindPresetButton(R.id.btn_preset_25, 25L);
    }

    private void bindPresetButton(int buttonId, long minutes) {
        Button presetBtn = findViewById(buttonId);
        if (presetBtn != null) {
            presetBtn.setOnClickListener(v -> {
                if (isSessionActive) {
                    return;
                }
                selectedDurationMinutes = minutes;
                remainingMillis = minutes * MILLIS_PER_MINUTE;
                if (minutesEdit != null) {
                    minutesEdit.setText(String.valueOf(minutes));
                    minutesEdit.setSelection(minutesEdit.getText().length());
                }
                updateTimerDisplay(remainingMillis);
                statusText.setText(R.string.focus_status_ready);
            });
        }
    }

    private void setupMinutesInputListener() {
        if (minutesEdit == null) {
            return;
        }
        minutesEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isSessionActive) {
                    return;
                }
                updatePresetSelection();
                long parsed = parseMinutesInput();
                if (parsed > 0L) {
                    selectedDurationMinutes = parsed;
                    remainingMillis = parsed * MILLIS_PER_MINUTE;
                    updateTimerDisplay(remainingMillis);
                }
            }
        });
    }

    // Presentation only: custom input must never leave a different preset selected.
    private void updatePresetSelection() {
        int[] ids = {R.id.btn_preset_1, R.id.btn_preset_5, R.id.btn_preset_15, R.id.btn_preset_25};
        long[] minutes = {1L, 5L, 15L, 25L};
        long value = parseMinutesInput();
        for (int i = 0; i < ids.length; i++) {
            com.google.android.material.button.MaterialButton button = findViewById(ids[i]);
            if (button != null) {
                button.setCheckable(true);
                button.setChecked(value == minutes[i]);
            }
        }
    }

    private long parseMinutesInput() {
        if (minutesEdit == null || minutesEdit.getText() == null) {
            return -1L;
        }
        String raw = minutesEdit.getText().toString().trim();
        if (raw.isEmpty()) {
            return -1L;
        }
        try {
            long value = Long.parseLong(raw);
            return value >= 1L ? value : -1L;
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private void startFocusSession() {
        long minutes = parseMinutesInput();
        if (minutes < 1L) {
            Toast.makeText(this, R.string.focus_error_invalid_minutes, Toast.LENGTH_SHORT).show();
            return;
        }
        selectedDurationMinutes = minutes;
        remainingMillis = minutes * MILLIS_PER_MINUTE;
        isSessionActive = true;
        wasPausedDuringSession = false;

        syncUiWithState();
        statusText.setText(R.string.pid_focusing);
        startTickerFromRemaining();
    }

    private void startTickerFromRemaining() {
        timerHandler.removeCallbacks(tickRunnable);
        targetEndElapsedRealtime = SystemClock.elapsedRealtime() + remainingMillis;
        isTickerRunning = true;
        updateTimerDisplay(remainingMillis);
        timerHandler.post(tickRunnable);
    }

    private void pauseTickerAndSaveRemaining() {
        if (isSessionActive && isTickerRunning) {
            long now = SystemClock.elapsedRealtime();
            remainingMillis = Math.max(0L, targetEndElapsedRealtime - now);
        }
        isTickerRunning = false;
        timerHandler.removeCallbacks(tickRunnable);
        updateTimerDisplay(remainingMillis);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSessionActive) {
            if (wasPausedDuringSession) {
                statusText.setText(R.string.focus_status_paused_resumed);
            } else {
                statusText.setText(R.string.pid_focusing);
            }
            startTickerFromRemaining();
        }
    }

    @Override
    protected void onPause() {
        if (isSessionActive) {
            pauseTickerAndSaveRemaining();
            wasPausedDuringSession = true;
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (isSessionActive && isTickerRunning) {
            long now = SystemClock.elapsedRealtime();
            remainingMillis = Math.max(0L, targetEndElapsedRealtime - now);
        }
        outState.putBoolean(KEY_IS_SESSION_ACTIVE, isSessionActive);
        outState.putLong(KEY_REMAINING_MILLIS, remainingMillis);
        outState.putLong(KEY_SELECTED_MINUTES, selectedDurationMinutes);
        outState.putBoolean(KEY_WAS_PAUSED_DURING_SESSION, wasPausedDuringSession);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        timerHandler.removeCallbacks(tickRunnable);
        super.onDestroy();
    }

    private void onFocusSessionCompleted() {
        isSessionActive = false;
        isTickerRunning = false;
        wasPausedDuringSession = false;
        timerHandler.removeCallbacks(tickRunnable);

        FocusSessionEntity entity = new FocusSessionEntity(
                selectedDurationMinutes,
                System.currentTimeMillis(),
                true
        );
        AppServices.getDatabase(this).focusSessionDao().insert(entity);

        remainingMillis = selectedDurationMinutes * MILLIS_PER_MINUTE;
        syncUiWithState();
        statusText.setText(R.string.focus_status_completed);
        Toast.makeText(this, R.string.focus_status_completed, Toast.LENGTH_SHORT).show();
        refreshSessionHistory();
    }

    private void handleBackNavigation() {
        if (isSessionActive) {
            confirmAbandonSession(true);
        } else {
            finish();
        }
    }

    private void confirmAbandonSession(boolean finishActivityOnConfirm) {
        ConfirmDialog.show(
                this,
                R.string.focus_confirm_abandon_title,
                R.string.focus_confirm_abandon_message,
                () -> {
                    abandonActiveSession();
                    if (finishActivityOnConfirm) {
                        finish();
                    }
                },
                () -> {
                    // User cancelled: stay in Focus Mode and keep the timer running
                }
        );
    }

    private void abandonActiveSession() {
        isSessionActive = false;
        isTickerRunning = false;
        wasPausedDuringSession = false;
        timerHandler.removeCallbacks(tickRunnable);
        remainingMillis = selectedDurationMinutes * MILLIS_PER_MINUTE;
        syncUiWithState();
        statusText.setText(R.string.focus_status_ready);
    }

    private void syncUiWithState() {
        updateTimerDisplay(remainingMillis);
        findViewById(R.id.text_focus_title).setVisibility(isSessionActive ? View.GONE : View.VISIBLE);
        ((TextView) findViewById(R.id.text_focus_subtitle)).setText(isSessionActive
                ? R.string.pid_focus_intro : R.string.pid_focus_desc);
        findViewById(R.id.pid_timer_container).setVisibility(isSessionActive ? View.VISIBLE : View.GONE);
        updatePresetSelection();
        if (durationControlsLayout != null) {
            durationControlsLayout.setVisibility(isSessionActive ? View.GONE : View.VISIBLE);
        }
        if (startBtn != null) {
            startBtn.setVisibility(isSessionActive ? View.GONE : View.VISIBLE);
        }
        if (abandonBtn != null) {
            abandonBtn.setVisibility(isSessionActive ? View.VISIBLE : View.GONE);
        }
    }

    private void updateTimerDisplay(long millis) {
        if (timerText == null) {
            return;
        }
        long totalSeconds = millis <= 0L ? 0L : (millis + 999L) / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        String formatted = String.format(Locale.US, "%02d:%02d", minutes, seconds);
        timerText.setText(formatted);
    }

    private void refreshSessionHistory() {
        FocusSessionDao dao = AppServices.getDatabase(this).focusSessionDao();
        int completedCount = dao.getCompletedCount();
        if (completedCountText != null) {
            completedCountText.setText(getString(R.string.pid_session_count, completedCount));
        }

        if (historyListText != null) {
            List<FocusSessionEntity> sessions = dao.getAllSessions();
            if (sessions == null || sessions.isEmpty()) {
                historyListText.setText(R.string.pid_first_session);
            } else {
                StringBuilder sb = new StringBuilder();
                DateFormat timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
                int limit = Math.min(sessions.size(), 5);
                for (int i = 0; i < limit; i++) {
                    FocusSessionEntity item = sessions.get(i);
                    if (i > 0) {
                        sb.append("\n");
                    }
                    String formattedDate = timeFormat.format(new Date(item.completedAtMillis));
                    sb.append(getString(R.string.focus_history_item_format, item.durationMinutes, formattedDate));
                }
                historyListText.setText(sb.toString());
            }
        }
    }
}
