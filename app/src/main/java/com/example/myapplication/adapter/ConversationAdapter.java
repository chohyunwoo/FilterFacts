package com.example.myapplication.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.example.myapplication.common.ConversationItem;

public class ConversationAdapter extends ListAdapter<ConversationItem, ConversationAdapter.Holder> {

    public interface OnItemClick {
        void onClick(ConversationItem item);
    }

    private final OnItemClick onItemClick;

    public ConversationAdapter(OnItemClick onItemClick) { super(DIFF); this.onItemClick = onItemClick; }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ConversationItem item = getItem(position);
        h.title.setText(item.getTitle());
         h.title.setTextColor(Color.WHITE);
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(item);
        });
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView title;
        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
        }
    }

    private static final DiffUtil.ItemCallback<ConversationItem> DIFF = new DiffUtil.ItemCallback<ConversationItem>() {
        @Override public boolean areItemsTheSame(@NonNull ConversationItem o, @NonNull ConversationItem n) { return o.getId() == n.getId(); }
        @Override public boolean areContentsTheSame(@NonNull ConversationItem o, @NonNull ConversationItem n) { return o.getTitle().equals(n.getTitle()); }
    };
}