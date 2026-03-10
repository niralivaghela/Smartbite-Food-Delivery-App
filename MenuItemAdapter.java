package com.smartbite.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.smartbite.R;
import com.smartbite.models.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.VH> {

    public interface OnAddToCartListener {
        void onAddToCart(MenuItem item);
    }

    private final List<MenuItem> list = new ArrayList<>();
    private final OnAddToCartListener listener;

    public MenuItemAdapter(List<MenuItem> initial, OnAddToCartListener listener) {
        list.addAll(initial);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MenuItem item = list.get(position);

        h.tvName.setText(item.getName());
        h.tvDesc.setText(item.getDescription());
        h.tvRating.setText(String.valueOf(item.getRating()));

        h.tvPrice.setText(h.tvPrice.getContext()
                .getString(R.string.price_rupee,
                        String.format(Locale.getDefault(), "%.0f", item.getPrice())));

        // Veg/non-veg as compound drawable on tvName instead of a separate ImageView
        int badgeRes = item.isVeg() ? R.drawable.bg_badge_green : R.drawable.bg_badge_red;
        Drawable badge = ContextCompat.getDrawable(h.tvName.getContext(), badgeRes);
        if (badge != null) badge.setBounds(0, 0, 14, 14);
        h.tvName.setCompoundDrawables(badge, null, null, null);

        String image = item.getImage();
        if (image != null && !image.isEmpty()) {
            Glide.with(h.ivFood.getContext())
                    .load(image)
                    .placeholder(R.drawable.bg_skeleton)
                    .into(h.ivFood);
        }

        boolean available = item.isAvailable();
        h.btnAddToCart.setEnabled(available);
        h.btnAddToCart.setText(available
                ? h.btnAddToCart.getContext().getString(R.string.action_add)
                : h.btnAddToCart.getContext().getString(R.string.status_unavailable));

        h.btnAddToCart.setOnClickListener(v -> listener.onAddToCart(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<MenuItem> newList) {
        int oldSize = list.size();
        list.clear();
        notifyItemRangeRemoved(0, oldSize);
        list.addAll(newList);
        notifyItemRangeInserted(0, newList.size());
    }

    @SuppressWarnings("WeakerAccess")
    static class VH extends RecyclerView.ViewHolder {

        final ImageView ivFood;
        final TextView tvName, tvDesc, tvPrice, tvRating;
        final Button btnAddToCart;

        VH(View v) {
            super(v);
            ivFood       = v.findViewById(R.id.ivFood);
            tvName       = v.findViewById(R.id.tvName);
            tvDesc       = v.findViewById(R.id.tvDesc);
            tvPrice      = v.findViewById(R.id.tvPrice);
            tvRating     = v.findViewById(R.id.tvRating);
            btnAddToCart = v.findViewById(R.id.btnAddToCart);
        }
    }
}