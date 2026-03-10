package com.smartbite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbite.R;
import com.smartbite.models.Order;

import java.util.List;
import java.util.Locale;

public class OrderManageAdapter extends RecyclerView.Adapter<OrderManageAdapter.VH> {

    private final Context ctx;
    private List<Order> list;
    private final OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onAccept(Order o);
        void onReject(Order o);
        void onMarkReady(Order o);
    }

    public OrderManageAdapter(Context ctx, List<Order> list, OnOrderActionListener l) {
        this.ctx = ctx;
        this.list = list;
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_manage_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Order o = list.get(pos);
        h.tvOrderId.setText(ctx.getString(R.string.order_id_format, o.getOrderId().substring(0, 8).toUpperCase(Locale.getDefault())));
        h.tvStatus.setText(o.getStatus());
        h.tvAmount.setText(ctx.getString(R.string.amount_format, o.getTotalAmount()));
        int cnt = o.getItems() != null ? o.getItems().size() : 0;
        h.tvItems.setText(ctx.getResources().getQuantityString(R.plurals.item_count, cnt, cnt));

        h.btnAccept.setOnClickListener(v -> listener.onAccept(o));
        h.btnReject.setOnClickListener(v -> listener.onReject(o));
        h.btnMarkReady.setOnClickListener(v -> listener.onMarkReady(o));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public Order getOrderAt(int position) {
        if (position < 0 || position >= list.size()) return null;
        return list.get(position);
    }

    public void updateList(List<Order> newList) {
        list = newList;
        notifyItemRangeChanged(0, list.size());
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvAmount, tvItems;
        Button btnAccept, btnReject, btnMarkReady;

        VH(View v) {
            super(v);
            tvOrderId = v.findViewById(R.id.tvOrderId);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvAmount = v.findViewById(R.id.tvAmount);
            tvItems = v.findViewById(R.id.tvItems);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnReject = v.findViewById(R.id.btnReject);
            btnMarkReady = v.findViewById(R.id.btnMarkReady);
        }
    }
}