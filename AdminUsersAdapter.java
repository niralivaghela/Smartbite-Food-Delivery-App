package com.smartbite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.smartbite.R;
import com.smartbite.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ── Interface ─────────────────────────────────────────────────────────────

    public interface OnUserActionListener {
        void onUserClick(User user);
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private final Context context;
    private final List<User> list = new ArrayList<>();
    private final OnUserActionListener listener;

    // ── Constructor ───────────────────────────────────────────────────────────

    public AdminUsersAdapter(Context context, List<User> initial, OnUserActionListener listener) {
        this.context  = context;
        this.listener = listener;
        list.addAll(initial);
    }

    // ── RecyclerView.Adapter ──────────────────────────────────────────────────

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VH h = (VH) holder;
        User user = list.get(position);

        h.tvName.setText(user.getName() != null ? user.getName() : "Unknown");
        h.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "—");

        // Role badge
        String role = user.getRole() != null ? user.getRole() : "customer";
        h.tvRole.setText(role);
        h.tvRole.setBackgroundResource(
                role.equalsIgnoreCase("restaurant") ? R.drawable.bg_badge_blue : R.drawable.bg_badge_orange);

        // Banned badge
        boolean banned = Boolean.TRUE.equals(user.getBanned());
        h.tvBanned.setVisibility(banned ? View.VISIBLE : View.GONE);

        // Avatar
        String pic = user.getProfilePic();
        if (pic != null && !pic.isEmpty()) {
            Glide.with(context).load(pic)
                    .placeholder(R.drawable.bg_skeleton)
                    .circleCrop()
                    .into(h.ivAvatar);
        } else {
            h.ivAvatar.setImageResource(R.drawable.bg_skeleton);
        }

        h.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() { return list.size(); }

    // ── Public API ────────────────────────────────────────────────────────────

    public void updateList(List<User> newList) {
        int oldSize = list.size();
        list.clear();
        notifyItemRangeRemoved(0, oldSize);
        list.addAll(newList);
        notifyItemRangeInserted(0, newList.size());
    }

    @Nullable
    public User getUserAt(int position) {
        if (position < 0 || position >= list.size()) return null;
        return list.get(position);
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    private static final class VH extends RecyclerView.ViewHolder {
        final ImageView ivAvatar;
        final TextView  tvName, tvEmail, tvRole, tvBanned;

        VH(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvName   = v.findViewById(R.id.tvName);
            tvEmail  = v.findViewById(R.id.tvEmail);
            tvRole   = v.findViewById(R.id.tvRole);
            tvBanned = v.findViewById(R.id.tvBanned);
        }
    }
}