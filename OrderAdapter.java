package com.smartbite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbite.R;
import com.smartbite.models.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    public interface OnOrderClickListener {
        void onClick(Order o);
    }

    private final List<Order>          list = new ArrayList<>();
    private final OnOrderClickListener listener;

    public OrderAdapter(List<Order> initial, OnOrderClickListener listener) {
        list.addAll(initial);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Order o = list.get(position);

        h.tvOrderId.setText(h.tvOrderId.getContext()
                .getString(R.string.order_id_prefix,
                        o.getOrderId().substring(0, 8).toUpperCase(Locale.getDefault())));

        h.tvStatus.setText(o.getStatus());

        h.tvAmount.setText(h.tvAmount.getContext()
                .getString(R.string.price_rupee,
                        String.format(Locale.getDefault(), "%.0f", o.getTotalAmount())));

        int itemCount = o.getItems() != null ? o.getItems().size() : 0;
        h.tvItemCount.setText(h.tvItemCount.getContext()
                .getResources().getQuantityString(R.plurals.item_count, itemCount, itemCount));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        h.tvTime.setText(sdf.format(new Date(o.getTimestamp())));

        h.itemView.setOnClickListener(v -> listener.onClick(o));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<Order> newList) {
        int oldSize = list.size();
        list.clear();
        notifyItemRangeRemoved(0, oldSize);
        list.addAll(newList);
        notifyItemRangeInserted(0, newList.size());
    }

    // VH must be package-private (not private) because RecyclerView.Adapter<OrderAdapter.VH>
    // exposes it through the generic type parameter. @SuppressWarnings silences the IDE warning.
    @SuppressWarnings("WeakerAccess")
    static class VH extends RecyclerView.ViewHolder {

        final TextView tvOrderId, tvStatus, tvAmount, tvItemCount, tvTime;

        VH(View v) {
            super(v);
            tvOrderId   = v.findViewById(R.id.tvOrderId);
            tvStatus    = v.findViewById(R.id.tvStatus);
            tvAmount    = v.findViewById(R.id.tvAmount);
            tvItemCount = v.findViewById(R.id.tvItemCount);
            tvTime      = v.findViewById(R.id.tvTime);
        }
    }
}