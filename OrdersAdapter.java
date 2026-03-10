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

import java.util.List;
import java.util.Map;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.VH> {

    public interface OnReorderListener {
        void onReorder(Map<String, Object> order);
    }

    private final Context              ctx;
    private final List<Map<String, Object>> list;
    private final OnReorderListener    listener;

    public OrdersAdapter(Context ctx, List<Map<String, Object>> list,
                         OnReorderListener listener) {
        this.ctx      = ctx;
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_past_order, parent, false);
        return new VH(v);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Map<String, Object> order = list.get(position);

        String restaurant = (String) order.getOrDefault("restaurantName", "Restaurant");
        h.tvRestaurantName.setText(restaurant);

        // Order ID short form
        String orderId = (String) order.getOrDefault("orderId", "");
        if (orderId.length() > 6) orderId = "#" + orderId.substring(0, 6).toUpperCase();
        h.tvOrderId.setText(orderId);

        // Status
        String status = (String) order.getOrDefault("status", "delivered");
        h.tvStatus.setText(getStatusEmoji(status) + " " + capitalize(status.replace("_", " ")));
        h.tvStatus.setTextColor(status.equals("delivered") ? 0xFF27AE60 : 0xFFE74C3C);

        // Items
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        if (items != null && !items.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(items.size(), 2); i++) {
                if (i > 0) sb.append(", ");
                sb.append(items.get(i).get("name"));
            }
            if (items.size() > 2) sb.append(" +" + (items.size() - 2) + " more");
            h.tvItems.setText(sb.toString());
        }

        // Amount
        Object amount = order.get("totalAmount");
        if (amount != null) {
            h.tvAmount.setText("₹" + amount);
        }

        h.btnReorder.setOnClickListener(v -> listener.onReorder(order));
    }

    @Override public int getItemCount() { return list.size(); }

    private String getStatusEmoji(String status) {
        switch (status) {
            case "delivered":  return "✅";
            case "cancelled":  return "❌";
            case "preparing":  return "👨‍🍳";
            default:           return "📋";
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvRestaurantName, tvOrderId, tvStatus, tvItems, tvAmount;
        final Button   btnReorder;

        VH(@NonNull View v) {
            super(v);
            tvRestaurantName = v.findViewById(R.id.tvRestaurantName);
            tvOrderId        = v.findViewById(R.id.tvOrderId);
            tvStatus         = v.findViewById(R.id.tvOrderStatus);
            tvItems          = v.findViewById(R.id.tvOrderItems);
            tvAmount         = v.findViewById(R.id.tvOrderAmount);
            btnReorder       = v.findViewById(R.id.btnReorder);
        }
    }
}