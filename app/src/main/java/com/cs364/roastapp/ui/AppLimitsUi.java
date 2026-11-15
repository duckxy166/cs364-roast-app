package com.cs364.roastapp.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.cs364.roastapp.R;
import com.cs364.roastapp.core.ConfirmDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** UI-only contract. The tracking owner supplies data and handles every mutation. */
public final class AppLimitsUi {
    public static final class App {
        public final String packageName;
        public final CharSequence name;
        public final Drawable icon;
        public final long usedMinutes;
        public final long limitMinutes;
        public App(String packageName, CharSequence name, Drawable icon, long usedMinutes, long limitMinutes) {
            this.packageName = packageName; this.name = name; this.icon = icon;
            this.usedMinutes = Math.max(0, usedMinutes); this.limitMinutes = Math.max(1, limitMinutes);
        }
    }
    public interface Actions {
        void saveLimit(App app, long minutes);
        void remove(App app);
    }
    /** Home shows the first few limits; the Apps tab lists them all. */
    private static final int SUMMARY_ROWS = 4;
    private final Activity activity;
    private final Actions actions;
    private List<App> available = Collections.emptyList();
    public AppLimitsUi(Activity activity, Actions actions) {
        this.activity = activity; this.actions = actions;
    }
    public void setAvailableApps(List<App> apps) {
        available = Collections.unmodifiableList(new ArrayList<>(apps));
    }
    public void render(List<App> apps, boolean summary) {
        LinearLayout container = activity.findViewById(summary ? R.id.container_menu_buttons : R.id.pid_apps_container);
        container.removeAllViews();
        boolean none = apps.isEmpty();
        // The rows share one grouped surface, so an empty group must not leave a blank card behind.
        container.setVisibility(none ? View.GONE : View.VISIBLE);
        activity.findViewById(summary ? R.id.pid_home_empty : R.id.pid_apps_empty)
                .setVisibility(none ? View.VISIBLE : View.GONE);
        if (summary) {
            activity.findViewById(R.id.pid_home_hero).setVisibility(none ? View.GONE : View.VISIBLE);
            activity.findViewById(R.id.pid_home_apps_header).setVisibility(none ? View.GONE : View.VISIBLE);
            if (!none) {
                long totalMinutes = 0;
                for (App app : apps) totalMinutes += app.usedMinutes;
                ((TextView) activity.findViewById(R.id.pid_home_total)).setText(
                        UsageTimeText.format(activity, totalMinutes * 60000L));
                ((TextView) activity.findViewById(R.id.pid_home_manage)).setText(
                        activity.getString(R.string.pid_view_all_format, apps.size()));
            }
        } else {
            TextView countLabel = activity.findViewById(R.id.pid_tracking_count);
            countLabel.setText(activity.getString(R.string.pid_count_format, apps.size()));
            countLabel.setVisibility(none ? View.GONE : View.VISIBLE);
        }
        int count = summary ? Math.min(SUMMARY_ROWS, apps.size()) : apps.size();
        for (int i = 0; i < count; i++) {
            App app = apps.get(i);
            if (i > 0) container.addView(rowDivider());
            View card = LayoutInflater.from(activity).inflate(summary ? R.layout.pid_app_summary : R.layout.pid_app_card, container, false);
            ((TextView) card.findViewById(R.id.pid_app_name)).setText(app.name);
            ((TextView) card.findViewById(R.id.pid_app_usage)).setText(activity.getString(R.string.pid_usage_format, app.usedMinutes, app.limitMinutes));
            if (app.icon != null) ((ImageView) card.findViewById(R.id.pid_app_icon)).setImageDrawable(app.icon);
            TextView remaining = card.findViewById(R.id.pid_app_remaining);
            long difference = app.limitMinutes - app.usedMinutes;
            remaining.setText(difference > 0 ? activity.getString(R.string.pid_remaining_format, difference)
                    : difference == 0 ? activity.getString(R.string.pid_at_limit)
                    : activity.getString(R.string.pid_exceeded_format, -difference));
            android.widget.ProgressBar progress = card.findViewById(R.id.pid_app_progress);
            progress.setProgress((int) Math.min(100, app.usedMinutes * 100.0 / app.limitMinutes));
            progress.setContentDescription(app.name + ": " + remaining.getText());
            if (difference <= 0) {
                remaining.setTextColor(ContextCompat.getColor(activity, R.color.pid_error));
                progress.setProgressTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.pid_error)));
            }
            if (!summary) {
                card.findViewById(R.id.pid_app_edit).setEnabled(actions != null);
                card.findViewById(R.id.pid_app_remove).setEnabled(actions != null);
                card.findViewById(R.id.pid_app_edit).setContentDescription(activity.getString(R.string.pid_edit_limit) + " " + app.name);
                card.findViewById(R.id.pid_app_remove).setContentDescription(activity.getString(R.string.pid_remove) + " " + app.name);
                card.findViewById(R.id.pid_app_edit).setOnClickListener(v -> showLimit(app));
                card.findViewById(R.id.pid_app_remove).setOnClickListener(v -> ConfirmDialog.show(activity,
                        R.string.pid_remove_title, R.string.pid_remove_message, () -> actions.remove(app)));
            }
            container.addView(card);
        }
    }
    public void showPicker() {
        BottomSheetDialog sheet = new BottomSheetDialog(activity);
        View content = LayoutInflater.from(activity).inflate(R.layout.pid_sheet_picker, null, false);
        EditText search = content.findViewById(R.id.pid_picker_search);
        LinearLayout rows = content.findViewById(R.id.pid_picker_rows);
        Runnable refresh = () -> {
            rows.removeAllViews(); String query = search.getText().toString().toLowerCase(Locale.ROOT);
            for (App app : available) {
                if (!app.name.toString().toLowerCase(Locale.ROOT).contains(query)) continue;
                View choice = LayoutInflater.from(activity).inflate(R.layout.pid_picker_row, rows, false);
                ((TextView) choice.findViewById(R.id.pid_picker_name)).setText(app.name);
                // Launcher icons keep their own colours; never tint them into silhouettes.
                if (app.icon != null) ((ImageView) choice.findViewById(R.id.pid_picker_icon)).setImageDrawable(app.icon);
                choice.setOnClickListener(v -> { sheet.dismiss(); showLimit(app); }); rows.addView(choice);
            }
            if (rows.getChildCount() == 0) rows.addView(emptyNote(activity.getString(R.string.pid_no_matches)));
        };
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { refresh.run(); }
            public void afterTextChanged(Editable e) {}
        });
        refresh.run(); sheet.setContentView(content); sheet.show();
    }
    public void showLimit(App app) {
        BottomSheetDialog sheet = new BottomSheetDialog(activity);
        View content = LayoutInflater.from(activity).inflate(R.layout.pid_sheet_limit, null, false);
        ((TextView) content.findViewById(R.id.pid_limit_title)).setText(app.name);
        TextInputLayout input = content.findViewById(R.id.pid_limit_input);
        EditText edit = content.findViewById(R.id.pid_limit_edit);
        edit.setText(String.valueOf(app.limitMinutes));
        int[] presetIds = {R.id.pid_limit_preset_1, R.id.pid_limit_preset_2, R.id.pid_limit_preset_3, R.id.pid_limit_preset_4};
        long[] presetMinutes = {15, 30, 60, 120};
        for (int i = 0; i < presetIds.length; i++) {
            long minutes = presetMinutes[i];
            MaterialButton preset = content.findViewById(presetIds[i]);
            preset.setText(String.valueOf(minutes));
            preset.setOnClickListener(v -> edit.setText(String.valueOf(minutes)));
        }
        MaterialButton save = content.findViewById(R.id.pid_limit_save);
        save.setEnabled(actions != null);
        save.setOnClickListener(v -> {
            long minutes;
            try { minutes = Long.parseLong(edit.getText().toString().trim()); } catch (NumberFormatException e) { minutes = 0; }
            if (minutes < 1) { input.setError(activity.getString(R.string.pid_minimum)); return; }
            actions.saveLimit(app, minutes); sheet.dismiss();
        });
        sheet.setContentView(content); sheet.show();
    }
    /** Hairline between rows of the group, inset to line up with the app names. */
    private View rowDivider() {
        float density = activity.getResources().getDisplayMetrics().density;
        View divider = new View(activity);
        divider.setBackgroundColor(ContextCompat.getColor(activity, R.color.pid_outline_variant));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, Math.round(density)));
        params.setMarginStart(Math.round(64 * density));
        divider.setLayoutParams(params);
        return divider;
    }
    private TextView emptyNote(String value) {
        TextView view = new TextView(activity); view.setText(value);
        view.setTextAppearance(R.style.TextAppearance_PutItDown_Supporting);
        int pad = (int) (24 * activity.getResources().getDisplayMetrics().density);
        view.setPadding(pad, pad / 2, pad, pad / 2); return view;
    }
}
