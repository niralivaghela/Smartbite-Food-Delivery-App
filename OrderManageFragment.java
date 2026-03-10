package com.smartbite.fragments.restaurant;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.smartbite.R;
import com.smartbite.adapters.OrderManageAdapter;
import com.smartbite.models.Order;
import com.smartbite.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * OrderManageFragment — Upgraded (Restaurant Side)
 *
 * NEW / ADVANCED FEATURES:
 * ─────────────────────────────────────────────────────────────────
 * 1. Real-time Firestore listener → orders pop in instantly without
 *    any pull-to-refresh.
 * 2. Per-order auto-accept countdown (30-second timer) — if restaurant
 *    does not act, order is auto-accepted and restaurant is notified.
 * 3. Sound alert (notification ringtone) on new order arrival.
 * 4. Haptic vibration (VibrationEffect pattern) on new order.
 * 5. Swipe-right → Accept, swipe-left → Reject with colored
 *    background reveal (ItemTouchHelper).
 * 6. Live animated KPI tiles: "New / Preparing / Ready" update in
 *    real-time with count-up animation.
 * 7. Today's revenue tile updates in real-time.
 * 8. Animated LIVE dot pulses in header.
 * 9. Filter chips (All / New / Preparing / Ready) with instant
 *    re-filter as data changes.
 * 10. "Last refreshed" timestamp updates every second.
 * ─────────────────────────────────────────────────────────────────
 */
public class OrderManageFragment extends Fragment {

    // ── Firebase ──────────────────────────────────────────────────────
    private FirebaseFirestore db;
    private ListenerRegistration ordersListener;
    private String restaurantId;

    // ── State ─────────────────────────────────────────────────────────
    private final List<Order> allOrders      = new ArrayList<>();
    private final List<Order> displayedOrders = new ArrayList<>();
    private OrderManageAdapter adapter;
    private String activeFilter = "All";
    private boolean soundEnabled  = true;
    private boolean hapticEnabled = true;

    // ── Views ─────────────────────────────────────────────────────────
    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmerOrders;
    private View layoutEmpty;
    private TextView tvCountNew, tvCountPreparing, tvCountReady;
    private TextView tvTodayRevenue, tvTodayOrderCount;
    private TextView tvLastRefresh;
    private View liveDot;
    private SwitchMaterial switchSound, switchHaptic;
    private ChipGroup chipGroupFilter;

    // ── Handlers ──────────────────────────────────────────────────────
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── Auto-accept timers per orderId ────────────────────────────────
    private final java.util.Map<String, CountDownTimer> autoAcceptTimers = new java.util.HashMap<>();

    // ═════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ═════════════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        restaurantId = user.getUid();

        bindViews(v);
        setupRecyclerView();
        setupFilterChips();
        setupAlertToggles();
        setupLivePulse();
        showShimmer();
        attachRealtimeListener();
        startClock();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ordersListener != null) ordersListener.remove();
        mainHandler.removeCallbacksAndMessages(null);
        for (CountDownTimer t : autoAcceptTimers.values()) t.cancel();
        autoAcceptTimers.clear();
    }

    // ═════════════════════════════════════════════════════════════════
    //  VIEW BINDING
    // ═════════════════════════════════════════════════════════════════

    private void bindViews(View v) {
        recyclerView      = v.findViewById(R.id.recyclerView);
        shimmerOrders     = v.findViewById(R.id.shimmerOrders);
        layoutEmpty       = v.findViewById(R.id.layoutEmpty);
        tvCountNew        = v.findViewById(R.id.tvCountNew);
        tvCountPreparing  = v.findViewById(R.id.tvCountPreparing);
        tvCountReady      = v.findViewById(R.id.tvCountReady);
        tvTodayRevenue    = v.findViewById(R.id.tvTodayRevenue);
        tvTodayOrderCount = v.findViewById(R.id.tvTodayOrderCount);
        tvLastRefresh     = v.findViewById(R.id.tvLastRefresh);
        liveDot           = v.findViewById(R.id.liveDot);
        switchSound       = v.findViewById(R.id.switchSound);
        switchHaptic      = v.findViewById(R.id.switchHaptic);
        chipGroupFilter   = v.findViewById(R.id.chipGroupFilter);
    }

    // ═════════════════════════════════════════════════════════════════
    //  RECYCLER + SWIPE  ← KEY UPGRADE
    // ═════════════════════════════════════════════════════════════════

    private void setupRecyclerView() {
        adapter = new OrderManageAdapter(requireContext(), new ArrayList<>(),
                new OrderManageAdapter.OnOrderActionListener() {
                    @Override
                    public void onAccept(Order order) {
                        acceptOrder(order);
                    }
                    @Override
                    public void onReject(Order order) {
                        showRejectDialog(order);
                    }
                    @Override
                    public void onMarkReady(Order order) {
                        advanceStatus(order);
                    }
                });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // ItemTouchHelper: swipe right = accept, swipe left = reject
        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView rv,
                                          @NonNull RecyclerView.ViewHolder vh,
                                          @NonNull RecyclerView.ViewHolder target) { return false; }
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                        int pos = vh.getAdapterPosition();
                        if (pos < 0 || pos >= displayedOrders.size()) return;
                        Order order = displayedOrders.get(pos);
                        if (direction == ItemTouchHelper.RIGHT) {
                            // Accept
                            acceptOrder(order);
                            showActionToast("✅ Accepted: #" + shortId(order.getOrderId()));
                        } else {
                            // Reject — confirm first
                            adapter.notifyItemChanged(pos); // restore swipe position
                            showRejectDialog(order);
                        }
                    }
                    @Override
                    public void onChildDraw(@NonNull android.graphics.Canvas c,
                                            @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder vh,
                                            float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {
                        // Draw green/red background behind swiped card
                        View itemView = vh.itemView;
                        android.graphics.Paint paint = new android.graphics.Paint();
                        if (dX > 0) {
                            paint.setColor(android.graphics.Color.parseColor("#10B981")); // green
                            c.drawRect(itemView.getLeft(), itemView.getTop(),
                                    itemView.getLeft() + dX, itemView.getBottom(), paint);
                        } else if (dX < 0) {
                            paint.setColor(android.graphics.Color.parseColor("#EF4444")); // red
                            c.drawRect(itemView.getRight() + dX, itemView.getTop(),
                                    itemView.getRight(), itemView.getBottom(), paint);
                        }
                        super.onChildDraw(c, recyclerView, vh, dX, dY, actionState, isCurrentlyActive);
                    }
                };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    // ═════════════════════════════════════════════════════════════════
    //  REAL-TIME FIRESTORE LISTENER  ← KEY UPGRADE
    // ═════════════════════════════════════════════════════════════════

    private void attachRealtimeListener() {
        ordersListener = db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("restaurantId", restaurantId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (!isAdded() || snap == null) return;
                    hideShimmer();

                    // Handle document changes for alerts
                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Order newOrder = dc.getDocument().toObject(Order.class);
                            if ("Placed".equalsIgnoreCase(newOrder.getStatus())) {
                                onNewOrderArrived(newOrder);
                            }
                        }
                    }

                    allOrders.clear();
                    allOrders.addAll(snap.toObjects(Order.class));
                    applyFilter();
                    updateKpiTiles();
                    updateTimestamp();
                });
    }

    // ═════════════════════════════════════════════════════════════════
    //  NEW ORDER ARRIVED  ← KEY UPGRADE
    // ═════════════════════════════════════════════════════════════════

    private void onNewOrderArrived(Order order) {
        if (!isAdded()) return;

        // Sound
        if (soundEnabled) playNotificationSound();

        // Haptic
        if (hapticEnabled) vibrate();

        // Animate the "New" KPI tile
        if (tvCountNew != null) {
            tvCountNew.animate().scaleX(1.4f).scaleY(1.4f)
                    .setDuration(150)
                    .withEndAction(() -> tvCountNew.animate()
                            .scaleX(1f).scaleY(1f).setDuration(150)
                            .setInterpolator(new BounceInterpolator()).start())
                    .start();
        }

        // Auto-accept countdown (30 s)
        startAutoAcceptTimer(order);
    }

    private void startAutoAcceptTimer(Order order) {
        String orderId = order.getOrderId();
        if (orderId == null || autoAcceptTimers.containsKey(orderId)) return;

        CountDownTimer timer = new CountDownTimer(30_000, 1000) {
            @Override public void onTick(long ms) {
                // Could update a per-card countdown via adapter
            }
            @Override public void onFinish() {
                autoAcceptTimers.remove(orderId);
                // Auto-accept
                if (isAdded()) acceptOrder(order);
            }
        }.start();
        autoAcceptTimers.put(orderId, timer);
    }

    // ═════════════════════════════════════════════════════════════════
    //  KPI TILES
    // ═════════════════════════════════════════════════════════════════

    private void updateKpiTiles() {
        long todayMs = getTodayStart();
        int cNew = 0, cPreparing = 0, cReady = 0;
        int todayOrders = 0;
        double todayRev = 0;

        for (Order o : allOrders) {
            String s = o.getStatus();
            if ("Placed".equalsIgnoreCase(s) || "Confirmed".equalsIgnoreCase(s)) cNew++;
            else if ("Preparing".equalsIgnoreCase(s)) cPreparing++;
            else if ("Ready".equalsIgnoreCase(s)) cReady++;

            long ts = o.getTimestamp();
            if (ts >= todayMs) {
                todayOrders++;
                todayRev += o.getTotalAmount();
            }
        }

        setKpi(tvCountNew,       cNew);
        setKpi(tvCountPreparing, cPreparing);
        setKpi(tvCountReady,     cReady);

        if (tvTodayOrderCount != null) tvTodayOrderCount.setText(String.valueOf(todayOrders));
        if (tvTodayRevenue    != null) animateRupee(tvTodayRevenue, (int) todayRev);
    }

    private void setKpi(TextView tv, int val) {
        if (tv == null) return;
        ValueAnimator va = ValueAnimator.ofInt(0, val);
        va.setDuration(600);
        va.addUpdateListener(a -> tv.setText(String.valueOf(a.getAnimatedValue())));
        va.start();
    }

    private void animateRupee(TextView tv, int target) {
        ValueAnimator va = ValueAnimator.ofInt(0, target);
        va.setDuration(800);
        va.addUpdateListener(a -> tv.setText(String.format(Locale.getDefault(), "₹%d", (int) a.getAnimatedValue())));
        va.start();
    }

    // ═════════════════════════════════════════════════════════════════
    //  FILTER CHIPS
    // ═════════════════════════════════════════════════════════════════

    private void setupFilterChips() {
        if (chipGroupFilter == null) return;
        chipGroupFilter.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            activeFilter = id == R.id.chipNew       ? "New"
                    : id == R.id.chipPreparing ? "Preparing"
                    : id == R.id.chipReady     ? "Ready" : "All";
            applyFilter();
        });
    }

    private void applyFilter() {
        List<Order> filtered;
        if ("All".equals(activeFilter)) {
            filtered = new ArrayList<>(allOrders);
        } else if ("New".equals(activeFilter)) {
            filtered = allOrders.stream()
                    .filter(o -> "Placed".equalsIgnoreCase(o.getStatus())
                            || "Confirmed".equalsIgnoreCase(o.getStatus()))
                    .collect(Collectors.toList());
        } else {
            filtered = allOrders.stream()
                    .filter(o -> activeFilter.equalsIgnoreCase(o.getStatus()))
                    .collect(Collectors.toList());
        }
        adapter.updateList(filtered);
        displayedOrders.clear();
        displayedOrders.addAll(filtered);
        if (layoutEmpty  != null) layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // ═════════════════════════════════════════════════════════════════
    //  ORDER ACTIONS
    // ═════════════════════════════════════════════════════════════════

    private void acceptOrder(Order order) {
        cancelAutoAccept(order.getOrderId());
        db.collection(Constants.COLLECTION_ORDERS).document(order.getOrderId())
                .update("status", Constants.STATUS_CONFIRMED)
                .addOnSuccessListener(u -> showActionToast("✅ Order accepted"));
    }

    private void showRejectDialog(Order order) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reject order?")
                .setMessage("This will cancel the customer's order.")
                .setPositiveButton("Reject", (d, i) -> {
                    cancelAutoAccept(order.getOrderId());
                    db.collection(Constants.COLLECTION_ORDERS).document(order.getOrderId())
                            .update("status", Constants.STATUS_CANCELLED)
                            .addOnSuccessListener(u -> showActionToast("❌ Order rejected"));
                })
                .setNegativeButton("Keep", null)
                .show();
    }

    private void advanceStatus(Order order) {
        String current = order.getStatus();
        String next = "Confirmed".equalsIgnoreCase(current) ? "Preparing"
                : "Preparing".equalsIgnoreCase(current) ? "Ready"
                : "Ready".equalsIgnoreCase(current)     ? "Delivered" : null;
        if (next == null) return;
        db.collection(Constants.COLLECTION_ORDERS).document(order.getOrderId())
                .update("status", next)
                .addOnSuccessListener(u -> showActionToast("Order → " + next));
    }

    private void cancelAutoAccept(String orderId) {
        CountDownTimer t = autoAcceptTimers.remove(orderId);
        if (t != null) t.cancel();
    }

    // ═════════════════════════════════════════════════════════════════
    //  ALERTS (Sound & Haptic)
    // ═════════════════════════════════════════════════════════════════

    private void setupAlertToggles() {
        if (switchSound  != null) switchSound.setOnCheckedChangeListener(
                (b, on) -> soundEnabled = on);
        if (switchHaptic != null) switchHaptic.setOnCheckedChangeListener(
                (b, on) -> hapticEnabled = on);
    }

    private void playNotificationSound() {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(requireContext(), uri);
            if (r != null) r.play();
        } catch (Exception ignored) {}
    }

    private void vibrate() {
        try {
            Vibrator v = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(new long[]{0,200,100,200}, -1));
            } else {
                v.vibrate(new long[]{0,200,100,200}, -1);
            }
        } catch (Exception ignored) {}
    }

    // ═════════════════════════════════════════════════════════════════
    //  LIVE PULSE + CLOCK
    // ═════════════════════════════════════════════════════════════════

    private void setupLivePulse() {
        if (liveDot == null) return;
        ObjectAnimator pulse = ObjectAnimator.ofFloat(liveDot, "alpha", 1f, 0.1f, 1f);
        pulse.setDuration(900);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.start();
    }

    private void startClock() {
        Runnable clockRunnable = new Runnable() {
            @Override public void run() {
                updateTimestamp();
                mainHandler.postDelayed(this, 1000);
            }
        };
        mainHandler.post(clockRunnable);
    }

    private void updateTimestamp() {
        if (tvLastRefresh == null) return;
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        tvLastRefresh.setText(String.format(Locale.getDefault(), "Live · %s", time));
    }

    // ═════════════════════════════════════════════════════════════════
    //  SHIMMER
    // ═════════════════════════════════════════════════════════════════

    private void showShimmer() {
        if (shimmerOrders != null) { shimmerOrders.setVisibility(View.VISIBLE); shimmerOrders.startShimmer(); }
        if (recyclerView  != null)   recyclerView.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        if (shimmerOrders != null) { shimmerOrders.stopShimmer(); shimmerOrders.setVisibility(View.GONE); }
        if (recyclerView  != null)   recyclerView.setVisibility(View.VISIBLE);
    }

    // ═════════════════════════════════════════════════════════════════
    //  UTILITIES
    // ═════════════════════════════════════════════════════════════════

    private void showActionToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private String shortId(String id) {
        return id != null && id.length() >= 6 ? id.substring(0, 6).toUpperCase() : "------";
    }

    private long getTodayStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}