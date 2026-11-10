package com.cs364.roastapp.core;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.cs364.roastapp.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class ConfirmDialog {

    private ConfirmDialog() {
    }

    public static void show(
            Context context,
            @StringRes int titleResId,
            @StringRes int messageResId,
            Runnable onConfirm
    ) {
        show(context, titleResId, messageResId, onConfirm, null);
    }

    public static void show(
            Context context,
            @StringRes int titleResId,
            @StringRes int messageResId,
            Runnable onConfirm,
            @Nullable Runnable onCancel
    ) {
        show(
                context,
                context.getString(titleResId),
                context.getString(messageResId),
                onConfirm,
                onCancel
        );
    }

    public static void show(
            Context context,
            CharSequence title,
            CharSequence message,
            Runnable onConfirm
    ) {
        show(context, title, message, onConfirm, null);
    }

    public static void show(
            Context context,
            CharSequence title,
            CharSequence message,
            Runnable onConfirm,
            @Nullable Runnable onCancel
    ) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.core_confirm_yes, (dialog, which) -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                })
                .setNegativeButton(R.string.core_confirm_cancel, (dialog, which) -> {
                    if (onCancel != null) {
                        onCancel.run();
                    }
                })
                .setOnCancelListener(dialog -> {
                    if (onCancel != null) {
                        onCancel.run();
                    }
                })
                .show();
    }
}
