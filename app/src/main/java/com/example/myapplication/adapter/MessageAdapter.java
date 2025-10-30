package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.example.myapplication.common.ChatItem;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends ListAdapter<ChatItem, RecyclerView.ViewHolder> {
    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    public MessageAdapter() { super(DIFF); }

    @Override public int getItemViewType(int position) {
        ChatItem item = getItem(position);
        return item != null && item.isUser ? TYPE_USER : TYPE_AI;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_USER) ? R.layout.item_message_user : R.layout.item_message_ai;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        
        // 아이템 뷰의 레이아웃 파라미터 수정
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        if (params != null) {
            params.topMargin = 0;
            params.bottomMargin = 0;
            v.setLayoutParams(params);
        }
        
        return new RecyclerView.ViewHolder(v) {};
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TextView tv = holder.itemView.findViewById(R.id.tvMsg);
        ChatItem item = getItem(position);
        tv.setText(item == null ? "" : item.text);
    }

    private static final DiffUtil.ItemCallback<ChatItem> DIFF = new DiffUtil.ItemCallback<ChatItem>() {
        @Override public boolean areItemsTheSame(@NonNull ChatItem oldItem, @NonNull ChatItem newItem) {
            return oldItem == newItem;
        }
        @Override public boolean areContentsTheSame(@NonNull ChatItem oldItem, @NonNull ChatItem newItem) {
            return oldItem.isUser == newItem.isUser &&
                   ((oldItem.text == null && newItem.text == null) || (oldItem.text != null && oldItem.text.contentEquals(newItem.text)));
        }
    };

    public void add(ChatItem item) {
        List<ChatItem> currentList = new ArrayList<>(getCurrentList());
        currentList.add(item);
        submitList(currentList);
    }

    public void clear() {
        submitList(new ArrayList<>());
    }
}