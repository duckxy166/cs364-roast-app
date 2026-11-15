package com.cs364.roastapp.history;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cs364.roastapp.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        holder.textLevel.setText(ctx.getString(R.string.history_item_level, item.level));
        // The last level is the loud one (audio); flag it so it stands out when scanning.
        holder.textLevel.setTextColor(ContextCompat.getColor(ctx,
                item.level >= 3 ? R.color.pid_error : R.color.pid_on_surface_variant));
        holder.textMessage.setText(item.message != null ? item.message : "");
        holder.textTime.setText(dateFormat.format(new Date(item.timestampMillis)));
        holder.itemView.setContentDescription(ctx.getString(R.string.history_item_header, label, item.level)
                + ". " + holder.textMessage.getText() + ". " + holder.textTime.getText());

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
        final TextView textLevel;
        final TextView textMessage;
        final TextView textTime;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.image_history_item_icon);
            textHeader = itemView.findViewById(R.id.text_history_item_header);
            textLevel = itemView.findViewById(R.id.text_history_item_level);
            textMessage = itemView.findViewById(R.id.text_history_item_message);
            textTime = itemView.findViewById(R.id.text_history_item_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_history_item);
        }
    }
}
