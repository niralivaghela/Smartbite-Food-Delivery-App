package com.smartbite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbite.R;
import com.smartbite.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER   = 0;
    private static final int TYPE_BOT    = 1;

    private final List<ChatMessage> list = new ArrayList<>();

    public ChatAdapter(List<ChatMessage> initial) {
        list.addAll(initial);
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).isUser() ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            return new UserVH(inflater.inflate(R.layout.item_chat_user, parent, false));
        }
        return new BotVH(inflater.inflate(R.layout.item_chat_bot, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = list.get(position);
        if (holder instanceof UserVH) {
            ((UserVH) holder).tvMessage.setText(m.getMessage());
        } else if (holder instanceof BotVH) {
            ((BotVH) holder).tvMessage.setText(m.isTyping() ? "● ● ●" : m.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addMessage(ChatMessage m) {
        list.add(m);
        notifyItemInserted(list.size() - 1);
    }

    public void removeTypingIndicator() {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).isTyping()) {
                list.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    static class UserVH extends RecyclerView.ViewHolder {
        final TextView tvMessage;
        UserVH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvMessage);
        }
    }

    @SuppressWarnings("WeakerAccess")
    static class BotVH extends RecyclerView.ViewHolder {
        final TextView tvMessage;
        BotVH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvMessage);
        }
    }
}