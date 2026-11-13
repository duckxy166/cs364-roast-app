package com.cs364.roastapp.roast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cs364.roastapp.R;

import java.util.ArrayList;
import java.util.List;

public class CustomRoastAdapter extends RecyclerView.Adapter<CustomRoastAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(CustomRoastEntity item);
    }

    private final List<CustomRoastEntity> items = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;

    public CustomRoastAdapter(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void submitList(List<CustomRoastEntity> newItems) {
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
                .inflate(R.layout.roast_item_custom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomRoastEntity item = items.get(position);
        holder.textLine.setText(item.text);
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

        final TextView textLine;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textLine = itemView.findViewById(R.id.text_custom_roast_line);
            btnDelete = itemView.findViewById(R.id.btn_delete_custom_roast);
        }
    }
}
