package com.cs364.roastapp.history;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cs364.roastapp.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(RoastEventEntity item);
    }

    private final List<RoastEventEntity> items = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public HistoryAdapter(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void submitList(List<RoastEventEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /**
     * A reminder is saved as the text shown at that moment, so an old record can be in the
     * other language. Reminder lines have the same order in Thai and English, so find which
     * line it is and show that line in the language the app uses now.
     */
    private static String messageInCurrentLanguage(Context ctx, String saved) {
        if (saved == null) {
            return "";
        }
        String[] current = ctx.getResources().getStringArray(R.array.roast_voice_lines);
        for (String language : new String[]{"th", "en"}) {
            Configuration config = new Configuration(ctx.getResources().getConfiguration());
            config.setLocale(new Locale(language));
            String[] lines = ctx.createConfigurationContext(config)
                    .getResources().getStringArray(R.array.roast_voice_lines);
            for (int i = 0; i < lines.length && i < current.length; i++) {
                if (saved.equals(lines[i])) {
                    return current[i];
                }
            }
        }
        return saved;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoastEventEntity item = items.get(position);
        Context ctx = holder.itemView.getContext();

        String label = (item.appLabel != null && !item.appLabel.isEmpty())
                ? item.appLabel
                : item.packageName;
        holder.textHeader.setText(label);
        holder.textMessage.setText(messageInCurrentLanguage(ctx, item.message));
        holder.textTime.setText(dateFormat.format(new Date(item.timestampMillis)));
        holder.itemView.setContentDescription(label + ". " + holder.textMessage.getText()
                + ". " + holder.textTime.getText());

        Drawable icon = null;
        try {
            icon = ctx.getPackageManager().getApplicationIcon(item.packageName);
        } catch (PackageManager.NameNotFoundException uninstalled) {
            // The app may have been removed since the reminder fired; show the name only.
        }
        holder.imageIcon.setImageDrawable(icon);
        holder.imageIcon.setVisibility(icon != null ? View.VISIBLE : View.GONE);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView imageIcon;
        final TextView textHeader;
        final TextView textMessage;
        final TextView textTime;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.image_history_item_icon);
            textHeader = itemView.findViewById(R.id.text_history_item_header);
            textMessage = itemView.findViewById(R.id.text_history_item_message);
            textTime = itemView.findViewById(R.id.text_history_item_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_history_item);
        }
    }
}
