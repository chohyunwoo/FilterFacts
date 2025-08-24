package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// 제목만 보이는 아주 단순한 어댑터
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {

    public interface OnItemClick {
        void onClick(ConversationItem item);
    }

    private final List<ConversationItem> data;
    private final OnItemClick onItemClick;

    public ConversationAdapter(List<ConversationItem> data, OnItemClick onItemClick) {
        this.data = data;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ConversationItem item = data.get(position);
        h.title.setText(item.getTitle());
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView title;
        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
        }
    }
}