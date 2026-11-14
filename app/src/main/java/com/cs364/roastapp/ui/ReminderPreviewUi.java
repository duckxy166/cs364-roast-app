package com.cs364.roastapp.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.cs364.roastapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

/** Visual preview only. Never calls notification, vibrator, service or audio APIs. */
public final class ReminderPreviewUi {
    private ReminderPreviewUi() {}
    public static void show(Activity activity) {
        BottomSheetDialog sheet = new BottomSheetDialog(activity);
        View content = LayoutInflater.from(activity).inflate(R.layout.pid_reminder_preview, null, false);
        int[] ids = {R.id.pid_preview_level_1,R.id.pid_preview_level_2,R.id.pid_preview_level_3};
        int[] titles = {R.string.roast_level1_title,R.string.roast_level2_title,R.string.roast_level3_title};
        int[] messages = {R.string.pid_preview_1,R.string.pid_preview_2,R.string.pid_preview_3};
        int[] descriptions = {R.string.pid_preview_delivery_1,R.string.pid_preview_delivery_2,R.string.pid_preview_delivery_3};
        java.util.function.IntConsumer select = level -> {
            ((TextView)content.findViewById(R.id.pid_preview_notification_title)).setText(activity.getString(titles[level], "Instagram"));
            ((TextView)content.findViewById(R.id.pid_preview_notification_body)).setText(messages[level]);
            ((TextView)content.findViewById(R.id.pid_preview_delivery)).setText(descriptions[level]);
            for (int i=0;i<ids.length;i++) {
                MaterialButton button = content.findViewById(ids[i]); button.setCheckable(true); button.setChecked(i==level);
            }
        };
        for (int i=0;i<ids.length;i++) { final int level=i; content.findViewById(ids[i]).setOnClickListener(v -> select.accept(level)); }
        content.findViewById(R.id.pid_preview_close).setOnClickListener(v -> sheet.dismiss());
        select.accept(0); sheet.setContentView(content); sheet.show();
    }
}
