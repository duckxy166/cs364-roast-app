package com.cs364.roastapp.ui;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.cs364.roastapp.R;

/** Feature-supplied display state; this class neither checks permissions nor starts a service. */
public final class MonitoringStatusUi {
    public enum State { READY, MISSING_PERMISSIONS, STOPPED }
    private MonitoringStatusUi() {}
    public static void render(Activity activity, State state, Runnable recovery) {
        View panel = activity.findViewById(R.id.pid_monitoring_panel);
        panel.setVisibility(state == State.READY ? View.GONE : View.VISIBLE);
        if (state == State.READY) return;
        boolean missing = state == State.MISSING_PERMISSIONS;
        ((ImageView)activity.findViewById(R.id.pid_monitor_icon)).setImageResource(missing ? R.drawable.pid_ic_permission : R.drawable.pid_ic_pause);
        ((TextView)activity.findViewById(R.id.pid_monitor_title)).setText(missing ? R.string.pid_monitor_missing : R.string.pid_monitor_stopped);
        ((TextView)activity.findViewById(R.id.pid_monitor_description)).setText(missing ? R.string.pid_monitor_missing_desc : R.string.pid_monitor_stopped_desc);
        Button action = activity.findViewById(R.id.btn_nav_onboarding);
        action.setText(missing ? R.string.pid_monitor_check : R.string.pid_monitor_resume);
        action.setEnabled(recovery != null);
        action.setOnClickListener(v -> { if (recovery != null) recovery.run(); });
    }
}
