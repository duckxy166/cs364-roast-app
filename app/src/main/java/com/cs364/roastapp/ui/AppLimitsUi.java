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
        View empty = activity.findViewById(summary ? R.id.pid_home_empty : R.id.pid_apps_empty);
        empty.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
        if (!summary) {
            ((TextView) activity.findViewById(R.id.pid_tracking_count)).setText(
                    activity.getString(R.string.pid_count_format, apps.size()));
        }
        if (summary && !apps.isEmpty()) {
            ((TextView) activity.findViewById(R.id.pid_home_manage)).setText(
                    activity.getString(R.string.pid_view_all_format, apps.size()));
        }
        int count = summary ? Math.min(2, apps.size()) : apps.size();
        for (int i = 0; i < count; i++) {
            App app = apps.get(i);
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
        LinearLayout content = column();
        TextView title = text(activity.getString(R.string.pid_choose_app), true); content.addView(title);
        EditText search = new EditText(activity); search.setSingleLine(true);
        search.setHint(R.string.pid_search_apps); content.addView(search);
        LinearLayout rows = column(); rows.setPadding(0, 0, 0, 0);
        android.widget.ScrollView scroll = new android.widget.ScrollView(activity);
        scroll.addView(rows); content.addView(scroll);
        Runnable refresh = () -> {
            rows.removeAllViews(); String query = search.getText().toString().toLowerCase(Locale.ROOT);
            for (App app : available) {
                if (!app.name.toString().toLowerCase(Locale.ROOT).contains(query)) continue;
                MaterialButton choice = button(app.name.toString(), false);
                if (app.icon != null) choice.setIcon(app.icon);
                choice.setOnClickListener(v -> { sheet.dismiss(); showLimit(app); }); rows.addView(choice);
            }
            if (rows.getChildCount() == 0) rows.addView(text(activity.getString(R.string.pid_no_matches), false));
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
        LinearLayout content = column(); content.addView(text(app.name.toString(), true));
        TextInputLayout input = new TextInputLayout(activity);
        input.setHint(activity.getString(R.string.pid_daily_limit));
        com.google.android.material.textfield.TextInputEditText edit = new com.google.android.material.textfield.TextInputEditText(activity);
        edit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); edit.setText(String.valueOf(app.limitMinutes));
        edit.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(3)});
        input.addView(edit); content.addView(input);
        LinearLayout presets = new LinearLayout(activity);
        for (long minutes : new long[]{15,30,60,120}) {
            MaterialButton preset = button(String.valueOf(minutes), false);
            preset.setMinWidth(0); preset.setPadding(2, 0, 2, 0);
            presets.addView(preset, new LinearLayout.LayoutParams(0, -2, 1));
            preset.setOnClickListener(v -> edit.setText(String.valueOf(minutes)));
        }
        content.addView(presets);
        content.addView(text(activity.getString(R.string.pid_daily_reset), false));
        MaterialButton save = button(activity.getString(R.string.pid_save), true);
        save.setEnabled(actions != null);
        save.setOnClickListener(v -> {
            long minutes;
            try { minutes = Long.parseLong(edit.getText().toString().trim()); } catch (NumberFormatException e) { minutes = 0; }
            if (minutes < 1) { input.setError(activity.getString(R.string.pid_minimum)); return; }
            actions.saveLimit(app, minutes); sheet.dismiss();
        });
        content.addView(save); sheet.setContentView(content); sheet.show();
    }
    private LinearLayout column() {
        LinearLayout view = new LinearLayout(activity); view.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * activity.getResources().getDisplayMetrics().density);
        view.setPadding(padding,padding,padding,padding); return view;
    }
    private TextView text(String value, boolean title) {
        TextView view = new TextView(activity); view.setText(value);
        view.setTextAppearance(title ? R.style.TextAppearance_PutItDown_Section : R.style.TextAppearance_PutItDown_Supporting);
        int pad = (int) (12 * activity.getResources().getDisplayMetrics().density);
        view.setPadding(0,pad,0,pad); return view;
    }
    private MaterialButton button(String value, boolean primary) {
        MaterialButton view = new MaterialButton(activity, null, primary
                ? com.google.android.material.R.attr.materialButtonStyle
                : com.google.android.material.R.attr.materialButtonOutlinedStyle);
        view.setText(value); return view;
    }
}
