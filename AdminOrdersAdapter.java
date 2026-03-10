package com.smartbite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbite.R;
import com.smartbite.models.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.VH> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final List<Order> list = new ArrayList<>();
    @Nullable
    private final OnOrderClickListener clickListener;

    public AdminOrdersAdapter(List<Order> initial) {
        list.addAll(initial);
        this.clickListener = null;
    }

    public AdminOrdersAdapter(List<Order> initial, @Nullable OnOrderClickListener listener) {
        list.addAll(initial);
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Uses item_order_admin layout — create res/layout/item_order_admin.xml if missing
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Order order = list.get(position);

        // Order ID
        if (order.getOrderId() != null) {
            String id = order.getOrderId();
            String shortId = "#" + (id.length() > 8
                    ? id.substring(id.length() - 8).toUpperCase(Locale.getDefault())
                    : id.toUpperCase(Locale.getDefault()));
            h.tvOrderId.setText(shortId);
        } else {
            h.tvOrderId.setText(R.string.app_name); // fallback — replace with your own string
        }

        // Restaurant name
        h.tvRestaurantName.setText(
                order.getRestaurantName() != null ? order.getRestaurantName() : "—");

        // Total price
        h.tvTotal.setText(h.tvTotal.getContext()
                .getString(R.string.price_rupee,
                        String.format(Locale.getDefault(), "%.0f", order.getTotalAmount())));

        // Status badge
        String status = order.getStatus() != null ? order.getStatus() : "Unknown";
        h.tvStatus.setText(status);
        h.tvStatus.setBackgroundResource(statusBadgeBackground(status));

        // Timestamp — getTimestamp() returns long, not Long, so no null check needed
        long ts = order.getTimestamp();
        if (ts > 0) {
            h.tvTimestamp.setText(
                    new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            .format(new Date(ts)));
        } else {
            h.tvTimestamp.setText("—");
        }

        // Click listener
        if (clickListener != null) {
            h.itemView.setOnClickListener(v -> clickListener.onOrderClick(order));
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public void updateList(List<Order> newList) {
        int oldSize = list.size();
        list.clear();
        notifyItemRangeRemoved(0, oldSize);
        list.addAll(newList);
        notifyItemRangeInserted(0, newList.size());
    }

    @Nullable
    public Order getItemAt(int position) {
        if (position < 0 || position >= list.size()) return null;
        return list.get(position);
    }

    private int statusBadgeBackground(String status) {
        switch (status) {
            case "Placed":    return R.drawable.bg_badge_orange;
            case "Preparing": return R.drawable.bg_badge_orange; // use bg_badge_orange until bg_badge_blue is created
            case "Delivered": return R.drawable.bg_badge_green;
            case "Cancelled": return R.drawable.bg_badge_red;
            default:          return R.drawable.bg_badge_orange;
        }
    }

    // 'private' prevents "exposed outside visibility scope" warning
    static final class VH extends RecyclerView.ViewHolder {
        final TextView tvOrderId, tvRestaurantName, tvTotal, tvStatus, tvTimestamp;

        VH(@NonNull View v) {
            super(v);
            tvOrderId        = v.findViewById(R.id.tvOrderId);
            tvRestaurantName = v.findViewById(R.id.tvRestaurantName);
            tvTotal          = v.findViewById(R.id.tvTotal);
            tvStatus         = v.findViewById(R.id.tvStatus);
            tvTimestamp      = v.findViewById(R.id.tvTimestamp);
        }
    }
}