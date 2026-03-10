package com.smartbite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.smartbite.R;
import com.smartbite.database.CartEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    private final Context ctx;
    private final OnCartActionListener listener;
    private List<CartEntity> list;

    public interface OnCartActionListener {
        void onIncrease(@NonNull CartEntity item);
        void onDecrease(@NonNull CartEntity item);
        void onRemove(@NonNull CartEntity item);
    }

    public CartAdapter(@NonNull Context ctx,
                       @NonNull List<CartEntity> list,
                       @NonNull OnCartActionListener listener) {
        this.ctx      = ctx;
        this.list     = list != null ? list : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_cart, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartEntity item = list.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(ctx.getString(R.string.label_rupee_amount_float,
                String.format(Locale.getDefault(), "%.0f", item.getPrice())));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvTotal.setText(ctx.getString(R.string.label_rupee_amount_float,
                String.format(Locale.getDefault(), "%.0f", item.getTotalPrice())));

        if (item.getImage() != null && !item.getImage().isEmpty()) {
            Glide.with(ctx)
                    .load(item.getImage())
                    .placeholder(R.drawable.bg_skeleton)
                    .centerCrop()
                    .into(holder.ivFood);
        }

        holder.btnPlus.setOnClickListener(v -> listener.onIncrease(item));
        holder.btnMinus.setOnClickListener(v -> listener.onDecrease(item));
        holder.btnRemove.setOnClickListener(v -> listener.onRemove(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Returns the item at the given position.
     * Used by CartActivity's swipe-to-delete ItemTouchHelper.
     */
    @NonNull
    public CartEntity getItemAt(int position) {
        return list.get(position);
    }

    public void updateList(List<CartEntity> newList) {
        if (newList == null) newList = new ArrayList<>();
        final List<CartEntity> oldList      = this.list;
        final List<CartEntity> finalNewList = newList;

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return finalNewList.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return oldList.get(oldPos).getItemId()
                        .equals(finalNewList.get(newPos).getItemId());
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                CartEntity o = oldList.get(oldPos);
                CartEntity n = finalNewList.get(newPos);
                return o.getQuantity() == n.getQuantity()
                        && o.getPrice()    == n.getPrice()
                        && o.getName().equals(n.getName());
            }
        });

        this.list = finalNewList;
        diff.dispatchUpdatesTo(this);
    }

    public static class VH extends RecyclerView.ViewHolder {
        final ImageView   ivFood;
        final TextView    tvName, tvPrice, tvQuantity, tvTotal;
        final ImageButton btnPlus, btnMinus, btnRemove;

        public VH(@NonNull View v) {
            super(v);
            ivFood     = v.findViewById(R.id.ivFood);
            tvName     = v.findViewById(R.id.tvName);
            tvPrice    = v.findViewById(R.id.tvPrice);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            tvTotal    = v.findViewById(R.id.tvTotal);
            btnPlus    = v.findViewById(R.id.btnPlus);
            btnMinus   = v.findViewById(R.id.btnMinus);
            btnRemove  = v.findViewById(R.id.btnRemove);
        }
    }
}