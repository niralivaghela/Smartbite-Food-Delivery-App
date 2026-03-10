package com.smartbite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.smartbite.R;
import com.smartbite.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class AdminRestaurantAdapter
        extends RecyclerView.Adapter<AdminRestaurantAdapter.VH> {

    // ── Interface ─────────────────────────────────────────────────────────────

    public interface OnAdminRestaurantAction {
        void onApprove(Restaurant r);
        void onSuspend(Restaurant r);
        void onDelete(Restaurant r);
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private final List<Restaurant>          list     = new ArrayList<>();
    private final OnAdminRestaurantAction   listener;

    // ── Constructor ───────────────────────────────────────────────────────────

    public AdminRestaurantAdapter(List<Restaurant> initial,
                                  OnAdminRestaurantAction listener) {
        list.addAll(initial);
        this.listener = listener;
    }

    // ── RecyclerView.Adapter ──────────────────────────────────────────────────

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // FIX: use dedicated admin layout that has btnApprove/btnSuspend/btnDelete
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Restaurant r = list.get(position);

        h.tvName.setText(r.getName());
        h.tvCuisine.setText(r.getCuisine());

        boolean approved = r.isApproved();
        h.tvStatus.setText(approved
                ? R.string.status_approved
                : R.string.status_pending);
        h.tvStatus.setBackgroundResource(approved
                ? R.drawable.bg_badge_green
                : R.drawable.bg_badge_orange);

        String image = r.getImage();
        if (image != null && !image.isEmpty()) {
            Glide.with(h.ivRestaurant.getContext())
                    .load(image)
                    .placeholder(R.drawable.bg_skeleton)
                    .into(h.ivRestaurant);
        }

        h.btnApprove.setOnClickListener(v -> listener.onApprove(r));
        h.btnSuspend.setOnClickListener(v -> listener.onSuspend(r));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override
    public int getItemCount() { return list.size(); }

    // ── Public API ────────────────────────────────────────────────────────────

    public void updateList(List<Restaurant> newList) {
        int oldSize = list.size();
        list.clear();
        notifyItemRangeRemoved(0, oldSize);
        list.addAll(newList);
        notifyItemRangeInserted(0, newList.size());
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────
    // FIX: 'private' suppresses "exposed outside visibility scope" warning

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivRestaurant;
        final TextView  tvName, tvCuisine, tvStatus;
        final Button    btnApprove, btnSuspend, btnDelete;

        VH(@NonNull View v) {
            super(v);
            ivRestaurant = v.findViewById(R.id.ivRestaurant);
            tvName       = v.findViewById(R.id.tvName);
            tvCuisine    = v.findViewById(R.id.tvCuisine);
            tvStatus     = v.findViewById(R.id.tvOpenClosed);
            btnApprove   = v.findViewById(R.id.btnApprove);
            btnSuspend   = v.findViewById(R.id.btnSuspend);
            btnDelete    = v.findViewById(R.id.btnDelete);
        }
    }
}