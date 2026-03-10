package com.smartbite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.smartbite.R;
import com.smartbite.models.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuManageAdapter extends RecyclerView.Adapter<MenuManageAdapter.VH> {

    public interface OnMenuActionListener {
        void onEdit(MenuItem i);
        void onDelete(MenuItem i);
        void onToggleAvailability(MenuItem i, boolean available);
    }

    private final List<MenuItem> list = new ArrayList<>();
    private final OnMenuActionListener listener;

    public MenuManageAdapter(List<MenuItem> initial, OnMenuActionListener listener) {
        list.addAll(initial);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_manage, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MenuItem item = list.get(position);

        h.tvName.setText(item.getName());
        h.tvPrice.setText(h.tvPrice.getContext()
                .getString(R.string.price_rupee,
                        String.format(Locale.getDefault(), "%.0f", item.getPrice())));

        String image = item.getImage();
        if (image != null && !image.isEmpty()) {
            Glide.with(h.ivFood.getContext())
                    .load(image)
                    .placeholder(R.drawable.bg_skeleton)
                    .into(h.ivFood);
        }

        // Clear listener before setChecked to avoid triggering callback during bind
        h.switchAvailable.setOnCheckedChangeListener(null);
        h.switchAvailable.setChecked(item.isAvailable());
        h.switchAvailable.setOnCheckedChangeListener(
                (btn, checked) -> listener.onToggleAvailability(item, checked));

        h.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Nullable
    public MenuItem getItemAt(int position) {
        if (position < 0 || position >= list.size()) return null;
        return list.get(position);
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
        final TextView tvName, tvPrice;
        final Button btnEdit, btnDelete;
        final SwitchMaterial switchAvailable;

        VH(View v) {
            super(v);
            ivFood          = v.findViewById(R.id.ivFood);
            tvName          = v.findViewById(R.id.tvName);
            tvPrice         = v.findViewById(R.id.tvPrice);
            btnEdit         = v.findViewById(R.id.btnEdit);
            btnDelete       = v.findViewById(R.id.btnDelete);
            switchAvailable = v.findViewById(R.id.switchAvailable);
        }
    }
}