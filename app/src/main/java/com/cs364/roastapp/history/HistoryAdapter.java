package com.cs364.roastapp.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.textHeader.setText(ctx.getString(R.string.history_item_header, label, item.level));
        holder.textMessage.setText(item.message != null ? item.message : "");
        holder.textTime.setText(dateFormat.format(new Date(item.timestampMillis)));

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

        final TextView textHeader;
        final TextView textMessage;
        final TextView textTime;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textHeader = itemView.findViewById(R.id.text_history_item_header);
            textMessage = itemView.findViewById(R.id.text_history_item_message);
            textTime = itemView.findViewById(R.id.text_history_item_time);
            btnDelete = itemView.findViewById(R.id.btn_delete_history_item);
        }
    }
}
