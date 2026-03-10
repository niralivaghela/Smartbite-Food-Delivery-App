package com.smartbite.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.smartbite.databinding.ActivityOrderHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private ActivityOrderHistoryBinding binding;

    // Fallback demo data
    private static final Object[][] DEMO_ORDERS = {
            {"#ORD001", "Spice Garden",  "Butter Chicken, Dal Makhani",    "₹408", "Delivered", "Today, 1:30 PM",       "r1"},
            {"#ORD002", "Pizza Palace",  "Margherita Pizza, Garlic Bread", "₹398", "Delivered", "Yesterday, 7:45 PM",   "r2"},
            {"#ORD003", "Burger Barn",   "Classic Burger, Veg Roll",       "₹298", "Delivered", "2 days ago, 12:10 PM", "r3"},
            {"#ORD004", "Dragon Wok",    "Hakka Noodles, Momos",           "₹308", "Cancelled", "3 days ago, 8:00 PM",  "r4"},
            {"#ORD005", "South Tadka",   "Masala Dosa, Idli Sambhar",      "₹228", "Delivered", "5 days ago, 9:15 AM",  "r5"},
    };

    private List<Object[]> allOrders = new ArrayList<>();
    private String activeFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            binding.toolbar.setTitle("My Orders");
            binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Try to load from Firestore, fallback to demo data
        loadOrdersFromFirestore();
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadOrdersFromFirestore() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            loadDemoOrders();
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("customerId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        loadDemoOrders();
                        return;
                    }
                    allOrders.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        String orderId      = "#" + doc.getString("orderId") != null
                                ? doc.getString("orderId").substring(0, Math.min(7, doc.getString("orderId").length())).toUpperCase() : "???";
                        String restaurant   = doc.getString("restaurantName") != null ? doc.getString("restaurantName") : "Restaurant";
                        String items        = "Order items";
                        String total        = "₹" + (doc.getDouble("totalAmount") != null ? doc.getDouble("totalAmount").intValue() : 0);
                        String status       = doc.getString("status") != null ? capitalise(doc.getString("status")) : "Placed";
                        String time         = formatTimestamp(doc.getLong("timestamp"));
                        String restaurantId = doc.getString("restaurantId") != null ? doc.getString("restaurantId") : "";
                        allOrders.add(new Object[]{orderId, restaurant, items, total, status, time, restaurantId});
                    }
                    buildUI();
                })
                .addOnFailureListener(e -> loadDemoOrders());
    }

    private void loadDemoOrders() {
        allOrders.clear();
        for (Object[] o : DEMO_ORDERS) allOrders.add(o);
        buildUI();
    }

    // ── UI build ──────────────────────────────────────────────────────────────

    private void buildUI() {
        LinearLayout content = binding.contentLayout;
        if (content == null) return;
        content.removeAllViews();

        // ── Filter chips ──────────────────────────────────────────────────────
        String[] filters = {"All", "Delivered", "Cancelled", "Preparing"};
        LinearLayout chipRow = new LinearLayout(this);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        chipRow.setPadding(0, 0, 0, dp(12));
        for (String f : filters) chipRow.addView(buildChip(f));
        content.addView(chipRow);

        // ── Search bar ────────────────────────────────────────────────────────
        EditText search = new EditText(this);
        search.setHint("🔍  Search orders...");
        search.setBackgroundResource(android.R.drawable.edit_text);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        sp.setMargins(0, 0, 0, dp(12));
        search.setLayoutParams(sp);
        search.setPadding(dp(12), 0, dp(12), 0);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterAndRender(content, s.toString().toLowerCase().trim());
            }
        });
        content.addView(search);

        renderOrders(content, allOrders);
    }

    private void filterAndRender(LinearLayout parent, String query) {
        // Remove everything after chip row + search bar (index 2 onwards)
        while (parent.getChildCount() > 2) parent.removeViewAt(2);

        List<Object[]> filtered = new ArrayList<>();
        for (Object[] o : allOrders) {
            String status = (String) o[4];
            boolean matchFilter = activeFilter.equals("All") || status.equalsIgnoreCase(activeFilter);
            boolean matchQuery  = query.isEmpty()
                    || ((String)o[1]).toLowerCase().contains(query)
                    || ((String)o[2]).toLowerCase().contains(query);
            if (matchFilter && matchQuery) filtered.add(o);
        }
        renderOrders(parent, filtered);
    }

    private void renderOrders(LinearLayout parent, List<Object[]> list) {
        if (list.isEmpty()) {
            parent.addView(buildEmptyState());
            return;
        }
        int delay = 0;
        for (Object[] o : list) {
            CardView card = buildOrderCard(
                    (String)o[0], (String)o[1], (String)o[2],
                    (String)o[3], (String)o[4], (String)o[5], (String)o[6]);
            card.setAlpha(0f);
            card.setTranslationY(30f);
            parent.addView(card);
            card.animate().alpha(1f).translationY(0f).setDuration(300)
                    .setStartDelay(delay)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            delay += 60;
        }
    }

    // ── Card builder ──────────────────────────────────────────────────────────

    private CardView buildOrderCard(String orderId, String restaurant, String items,
                                    String total, String status, String time, String restaurantId) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cp);
        card.setCardElevation(4f); card.setRadius(dp(14)); card.setCardBackgroundColor(Color.WHITE);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(16), dp(16), dp(16), dp(12));

        // Row 1: Restaurant + status badge
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvR = new TextView(this);
        tvR.setText("🍽 " + restaurant);
        tvR.setTextSize(16f); tvR.setTypeface(null, Typeface.BOLD);
        tvR.setTextColor(Color.parseColor("#1A1A1A"));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvR.setLayoutParams(rp);

        TextView tvStatus = new TextView(this);
        tvStatus.setText(status);
        tvStatus.setTextSize(11f); tvStatus.setTypeface(null, Typeface.BOLD);
        tvStatus.setPadding(dp(8), dp(4), dp(8), dp(4));
        boolean delivered = status.equalsIgnoreCase("delivered");
        boolean cancelled = status.equalsIgnoreCase("cancelled");
        GradientDrawable badge = new GradientDrawable();
        badge.setCornerRadius(dp(20));
        if (delivered)      { badge.setColor(Color.parseColor("#E8F5E9")); tvStatus.setTextColor(Color.parseColor("#2E7D32")); }
        else if (cancelled) { badge.setColor(Color.parseColor("#FFEBEE")); tvStatus.setTextColor(Color.parseColor("#C62828")); }
        else                { badge.setColor(Color.parseColor("#FFF3E0")); tvStatus.setTextColor(Color.parseColor("#E65100")); }
        tvStatus.setBackground(badge);
        row1.addView(tvR); row1.addView(tvStatus);

        // Time + order ID
        TextView tvTime = new TextView(this);
        tvTime.setText(orderId + "  •  " + time);
        tvTime.setTextSize(11f); tvTime.setTextColor(Color.parseColor("#888888"));
        tvTime.setPadding(0, dp(4), 0, dp(4));

        // Items
        TextView tvItems = new TextView(this);
        tvItems.setText(items);
        tvItems.setTextSize(13f); tvItems.setTextColor(Color.parseColor("#555555"));
        tvItems.setPadding(0, dp(2), 0, dp(2));

        // Divider
        View div = new View(this);
        LinearLayout.LayoutParams dp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dp1.setMargins(0, dp(8), 0, dp(8));
        div.setLayoutParams(dp1); div.setBackgroundColor(Color.parseColor("#F0F0F0"));

        // Row 2: Total + Reorder
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvT = new TextView(this);
        tvT.setText("Total: " + total);
        tvT.setTextSize(14f); tvT.setTypeface(null, Typeface.BOLD);
        tvT.setTextColor(Color.parseColor("#1A1A1A"));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvT.setLayoutParams(tp);

        TextView btnRe = new TextView(this);
        btnRe.setText(cancelled ? "View" : "Reorder");
        btnRe.setTextSize(13f); btnRe.setTypeface(null, Typeface.BOLD);
        btnRe.setTextColor(Color.WHITE);
        btnRe.setPadding(dp(16), dp(8), dp(16), dp(8));
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(cancelled ? Color.parseColor("#757575") : Color.parseColor("#FC8019"));
        btnBg.setCornerRadius(dp(8));
        btnRe.setBackground(btnBg);
        btnRe.setOnClickListener(v -> {
            animatePress(v);
            Intent i = new Intent(this, RestaurantDetailActivity.class);
            i.putExtra("restaurantId",   restaurantId);
            i.putExtra("restaurantName", restaurant);
            startActivity(i);
        });
        row2.addView(tvT); row2.addView(btnRe);

        inner.addView(row1); inner.addView(tvTime); inner.addView(tvItems);
        inner.addView(div);  inner.addView(row2);
        card.addView(inner);

        // Ripple on tap
        card.setOnClickListener(v -> animatePress(v));
        return card;
    }

    // ── Chip builder ──────────────────────────────────────────────────────────

    private TextView buildChip(String label) {
        TextView chip = new TextView(this);
        chip.setText(label);
        chip.setTextSize(13f);
        chip.setPadding(dp(16), dp(6), dp(16), dp(6));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(cp);
        updateChipStyle(chip, label.equals(activeFilter));

        chip.setOnClickListener(v -> {
            activeFilter = label;
            LinearLayout parent = (LinearLayout) chip.getParent();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View c = parent.getChildAt(i);
                if (c instanceof TextView) updateChipStyle((TextView) c, ((TextView) c).getText().toString().equals(activeFilter));
            }
            filterAndRender(binding.contentLayout, "");
        });
        return chip;
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        if (selected) {
            bg.setColor(Color.parseColor("#FC8019"));
            chip.setTextColor(Color.WHITE);
            chip.setTypeface(null, Typeface.BOLD);
        } else {
            bg.setColor(Color.parseColor("#F5F5F5"));
            chip.setTextColor(Color.parseColor("#555555"));
            chip.setTypeface(null, Typeface.NORMAL);
        }
        chip.setBackground(bg);
    }

    private View buildEmptyState() {
        LinearLayout empty = new LinearLayout(this);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(60), 0, 0);
        empty.setLayoutParams(p);

        TextView icon = new TextView(this); icon.setText("📦"); icon.setTextSize(56f); icon.setGravity(Gravity.CENTER);
        TextView msg  = new TextView(this); msg.setText("No orders found"); msg.setTextSize(18f);
        msg.setTypeface(null, Typeface.BOLD); msg.setTextColor(Color.parseColor("#1A1A1A")); msg.setGravity(Gravity.CENTER);
        msg.setPadding(0, dp(12), 0, dp(4));
        TextView sub  = new TextView(this); sub.setText("Try a different filter or search term");
        sub.setTextSize(13f); sub.setTextColor(Color.parseColor("#888888")); sub.setGravity(Gravity.CENTER);

        empty.addView(icon); empty.addView(msg); empty.addView(sub);
        return empty;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase().replace("_", " ");
    }

    private String formatTimestamp(Long ts) {
        if (ts == null) return "Recently";
        long diff = System.currentTimeMillis() - ts;
        long hours = diff / 3_600_000;
        if (hours < 1)  return "Just now";
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        if (days == 1)  return "Yesterday";
        return days + " days ago";
    }

    private void animatePress(View v) {
        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()).start();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}