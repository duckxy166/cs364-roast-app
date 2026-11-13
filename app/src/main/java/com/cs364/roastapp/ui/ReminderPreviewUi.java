package com.cs364.roastapp.ui;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cs364.roastapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

/** Visual preview only. Does not call notification, vibrator, service or audio APIs. */
public final class ReminderPreviewUi {
    private ReminderPreviewUi() {}
    public static void show(Activity activity) {
        BottomSheetDialog sheet = new BottomSheetDialog(activity);
        LinearLayout content = new LinearLayout(activity); content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * activity.getResources().getDisplayMetrics().density);
        content.setPadding(padding,padding,padding,padding);
        TextView title = new TextView(activity); title.setText(R.string.pid_preview_title);
        title.setTextAppearance(R.style.TextAppearance_PutItDown_Section); content.addView(title);
        TextView note = new TextView(activity); note.setText(R.string.pid_preview_note);
        note.setTextAppearance(R.style.TextAppearance_PutItDown_Supporting); content.addView(note);
        TextView body = new TextView(activity); body.setPadding(0,padding,0,padding);
        body.setTextAppearance(R.style.TextAppearance_PutItDown_Heading); body.setText(R.string.pid_preview_1); content.addView(body);
        MaterialButton next = new MaterialButton(activity); next.setText(R.string.pid_next);
        int[] level = {0}; int[] messages = {R.string.pid_preview_1,R.string.pid_preview_2,R.string.pid_preview_3};
        next.setOnClickListener(v -> { level[0] = (level[0] + 1) % messages.length; body.setText(messages[level[0]]); });
        content.addView(next);
        MaterialButton close = new MaterialButton(activity, null, com.google.android.material.R.attr.borderlessButtonStyle);
        close.setText(R.string.pid_close); close.setOnClickListener(v -> sheet.dismiss()); content.addView(close);
        sheet.setContentView(content); sheet.show();
    }
}
