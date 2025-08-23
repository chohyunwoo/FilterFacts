package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    private final List<ChatItem> items;

    public MessageAdapter(List<ChatItem> items) {
        this.items = items;
    }

    @Override public int getItemViewType(int position) {
        return items.get(position).isUser ? TYPE_USER : TYPE_AI;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_USER) ? R.layout.item_message_user : R.layout.item_message_ai;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new RecyclerView.ViewHolder(v) {};
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TextView tv = holder.itemView.findViewById(R.id.tvMsg);
        tv.setText(items.get(position).text);
    }

    @Override public int getItemCount() { return items.size(); }

    public void add(ChatItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }
}