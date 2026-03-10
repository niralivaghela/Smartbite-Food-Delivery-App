package com.smartbite.fragments.customer;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.smartbite.R;
import com.smartbite.adapters.OrdersAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersFragment extends Fragment {

    private static final String TAG = "OrdersFragment";

    private FirebaseFirestore db;
    private String uid;

    // Real-time listener — removed on destroy to prevent memory leak
    private ListenerRegistration ordersListener;

    // ── Active order views ─────────────────────────────────────────────
    private LinearLayout layoutActiveOrder;
    private TextView tvActiveRestaurant, tvActiveStatus, tvActiveItems, tvStatusIcon;
    private View etaPulseDot;
    private LinearProgressIndicator progressOrder;

    // Step circle views
    private View stepPlaced, stepConfirmed, stepPreparing, stepDelivery, stepDelivered;
    // Step connector lines
    private View linePlacedConfirmed, lineConfirmedPreparing, linePreparingDelivery, lineDeliveryDelivered;
    // Step label icons (✓ or step number)
    private TextView stepPlacedIcon, stepConfirmedIcon, stepPreparingIcon, stepDeliveryIcon, stepDeliveredIcon;

    // ── Scratch card ───────────────────────────────────────────────────
    private LinearLayout layoutScratchCard;
    private LinearLayout tvScratchReward;
    private ShimmerFrameLayout shimmerScratch;
    private boolean scratched = false;

    // ── Past orders ────────────────────────────────────────────────────
    private RecyclerView rvPastOrders;
    private OrdersAdapter ordersAdapter;
    // allOrders = full unfiltered list; pastOrders = currently displayed list
    private final List<Map<String, Object>> pastOrders = new ArrayList<>();
    private final List<Map<String, Object>> allOrders  = new ArrayList<>();

    // ── UI state ───────────────────────────────────────────────────────
    private LinearLayout layoutEmpty;
    private ShimmerFrameLayout shimmerPastOrders;
    private TextView tvOrderCount;

    // ETA pulse handler
    private final Handler pulseHandler = new Handler(Looper.getMainLooper());

    // ── Coupon data ────────────────────────────────────────────────────
    private static final String[][] COUPONS = {
            {"FIRST50", "50% off up to \u20b9100 on first order"},
            {"FREEDEL", "Free delivery on orders above \u20b9199"},
            {"SMART10", "10% discount applied!"},
    };
    private String appliedCoupon = null;

    // ── Status step constants ──────────────────────────────────────────
    private static final int STEP_PLACED    = 1;
    private static final int STEP_CONFIRMED = 2;
    private static final int STEP_PREPARING = 3;
    private static final int STEP_DELIVERY  = 4;
    private static final int STEP_DELIVERED = 5;

    // ── Step colours ───────────────────────────────────────────────────
    private static final int COLOR_ACTIVE   = 0xFFFC8019;
    private static final int COLOR_INACTIVE = 0xFFDDDDDD;

    // ══════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db  = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        Log.d(TAG, "UID: " + uid);

        bindViews(v);
        setupScratchCard(v);
        setupCoupons(v);
        setupFilterChips(v);
        setupPastOrders();
        showShimmer();
        loadOrders();
    }

    @Override
    public void onPause() {
        super.onPause();
        pulseHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null && etaPulseDot != null) startEtaPulse();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pulseHandler.removeCallbacksAndMessages(null);
        if (ordersListener != null) {
            ordersListener.remove();
            ordersListener = null;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  BIND VIEWS
    // ══════════════════════════════════════════════════════════════════

    private void bindViews(View v) {
        layoutActiveOrder      = v.findViewById(R.id.layoutActiveOrder);
        tvActiveRestaurant     = v.findViewById(R.id.tvActiveRestaurant);
        tvActiveStatus         = v.findViewById(R.id.tvActiveStatus);
        tvActiveItems          = v.findViewById(R.id.tvActiveItems);
        tvStatusIcon           = v.findViewById(R.id.tvStatusIcon);
        etaPulseDot            = v.findViewById(R.id.etaPulseDot);
        progressOrder          = v.findViewById(R.id.progressOrder);

        stepPlaced             = v.findViewById(R.id.stepPlaced);
        stepConfirmed          = v.findViewById(R.id.stepConfirmed);
        stepPreparing          = v.findViewById(R.id.stepPreparing);
        stepDelivery           = v.findViewById(R.id.stepDelivery);
        stepDelivered          = v.findViewById(R.id.stepDelivered);

        stepPlacedIcon         = v.findViewById(R.id.stepPlacedIcon);
        stepConfirmedIcon      = v.findViewById(R.id.stepConfirmedIcon);
        stepPreparingIcon      = v.findViewById(R.id.stepPreparingIcon);
        stepDeliveryIcon       = v.findViewById(R.id.stepDeliveryIcon);
        stepDeliveredIcon      = v.findViewById(R.id.stepDeliveredIcon);

        linePlacedConfirmed    = v.findViewById(R.id.linePlacedConfirmed);
        lineConfirmedPreparing = v.findViewById(R.id.lineConfirmedPreparing);
        linePreparingDelivery  = v.findViewById(R.id.linePreparingDelivery);
        lineDeliveryDelivered  = v.findViewById(R.id.lineDeliveryDelivered);

        layoutScratchCard  = v.findViewById(R.id.layoutScratchCard);
        tvScratchReward    = v.findViewById(R.id.tvScratchReward);
        shimmerScratch     = v.findViewById(R.id.shimmerScratch);
        rvPastOrders       = v.findViewById(R.id.rvPastOrders);
        layoutEmpty        = v.findViewById(R.id.layoutEmpty);
        shimmerPastOrders  = v.findViewById(R.id.shimmerPastOrders);
        tvOrderCount       = v.findViewById(R.id.tvOrderCount);

        View btnTrackMap = v.findViewById(R.id.btnTrackMap);
        if (btnTrackMap != null)
            btnTrackMap.setOnClickListener(x ->
                    Toast.makeText(requireContext(),
                            getString(R.string.msg_map_coming_soon), Toast.LENGTH_SHORT).show());

        View btnExplore = v.findViewById(R.id.btnExploreNow);
        if (btnExplore != null)
            btnExplore.setOnClickListener(x ->
                    requireActivity().getSupportFragmentManager().popBackStack());
    }

    // ══════════════════════════════════════════════════════════════════
    //  SCRATCH CARD
    // ══════════════════════════════════════════════════════════════════

    private void setupScratchCard(View v) {
        LinearLayout cardScratch = v.findViewById(R.id.cardScratch);
        if (cardScratch == null) return;

        if (shimmerScratch != null) shimmerScratch.startShimmer();

        cardScratch.setOnClickListener(x -> {
            if (scratched) return;
            scratched = true;

            if (shimmerScratch != null) shimmerScratch.stopShimmer();

            cardScratch.animate()
                    .scaleX(0f).scaleY(0f).alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (!isAdded()) return;
                        cardScratch.setVisibility(View.GONE);
                        if (tvScratchReward != null) {
                            tvScratchReward.setVisibility(View.VISIBLE);
                            tvScratchReward.setScaleX(0f);
                            tvScratchReward.setScaleY(0f);
                            tvScratchReward.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(400)
                                    .setInterpolator(new BounceInterpolator())
                                    .start();
                        }
                    }).start();

            Toast.makeText(requireContext(),
                    getString(R.string.msg_scratch_won), Toast.LENGTH_LONG).show();
        });
    }

    // ══════════════════════════════════════════════════════════════════
    //  COUPONS
    // ══════════════════════════════════════════════════════════════════

    private void setupCoupons(View v) {
        View btnFirst50 = v.findViewById(R.id.btnApplyFirst50);
        View btnFreedel = v.findViewById(R.id.btnApplyFreedel);
        if (btnFirst50 != null) btnFirst50.setOnClickListener(x -> applyCoupon("FIRST50", btnFirst50));
        if (btnFreedel != null) btnFreedel.setOnClickListener(x -> applyCoupon("FREEDEL", btnFreedel));
    }

    private void applyCoupon(String code, View btn) {
        if (code.equals(appliedCoupon)) {
            Toast.makeText(requireContext(),
                    getString(R.string.msg_coupon_already_applied, code), Toast.LENGTH_SHORT).show();
            return;
        }
        for (String[] coupon : COUPONS) {
            if (coupon[0].equalsIgnoreCase(code)) {
                appliedCoupon = coupon[0];

                if (btn instanceof TextView) {
                    ((TextView) btn).setText(R.string.label_coupon_applied);
                    btn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100)
                            .withEndAction(() ->
                                    btn.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                            .start();
                }

                Toast.makeText(requireContext(),
                        getString(R.string.msg_coupon_applied, coupon[1]), Toast.LENGTH_LONG).show();
                requireContext()
                        .getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE)
                        .edit()
                        .putString(getString(R.string.pref_key_coupon), appliedCoupon)
                        .apply();
                return;
            }
        }
        Toast.makeText(requireContext(),
                getString(R.string.msg_coupon_invalid), Toast.LENGTH_SHORT).show();
    }

    // ══════════════════════════════════════════════════════════════════
    //  FILTER CHIPS
    // ══════════════════════════════════════════════════════════════════

    private void setupFilterChips(View v) {
        ChipGroup chipGroup = v.findViewById(R.id.chipGroupOrdersFilter);
        if (chipGroup == null) return;

        chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            if      (id == R.id.chipFilterAll)       applyFilter(null);
            else if (id == R.id.chipFilterActive)    applyFilter("active");
            else if (id == R.id.chipFilterDelivered) applyFilter("Delivered");
            else if (id == R.id.chipFilterCancelled) applyFilter("Cancelled");
        });
    }

    private void applyFilter(@Nullable String filter) {
        int oldCount = pastOrders.size();
        pastOrders.clear();

        for (Map<String, Object> order : allOrders) {
            String status = (String) order.get("status");
            if (filter == null) {
                pastOrders.add(order);
            } else if ("active".equals(filter)) {
                if (status != null
                        && !status.equalsIgnoreCase("Delivered")
                        && !status.equalsIgnoreCase("Cancelled")) {
                    pastOrders.add(order);
                }
            } else if (status != null && status.equalsIgnoreCase(filter)) {
                pastOrders.add(order);
            }
        }

        int newCount = pastOrders.size();
        if (oldCount > 0) ordersAdapter.notifyItemRangeRemoved(0, oldCount);
        if (newCount > 0) ordersAdapter.notifyItemRangeInserted(0, newCount);

        updateEmptyState(newCount == 0 && allOrders.isEmpty());
        updateOrderCount();
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAST ORDERS RECYCLER
    // ══════════════════════════════════════════════════════════════════

    private void setupPastOrders() {
        ordersAdapter = new OrdersAdapter(requireContext(), pastOrders,
                order -> Toast.makeText(requireContext(),
                        getString(R.string.msg_added_to_cart), Toast.LENGTH_SHORT).show());
        rvPastOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPastOrders.setAdapter(ordersAdapter);
    }

    // ══════════════════════════════════════════════════════════════════
    //  SHIMMER
    // ══════════════════════════════════════════════════════════════════

    private void showShimmer() {
        if (shimmerPastOrders != null) {
            shimmerPastOrders.setVisibility(View.VISIBLE);
            shimmerPastOrders.startShimmer();
        }
        if (rvPastOrders != null) rvPastOrders.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        if (shimmerPastOrders != null) {
            shimmerPastOrders.stopShimmer();
            shimmerPastOrders.setVisibility(View.GONE);
        }
        if (rvPastOrders != null) rvPastOrders.setVisibility(View.VISIBLE);
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD ORDERS (real-time snapshot listener)
    // ══════════════════════════════════════════════════════════════════

    private void loadOrders() {
        if (uid.isEmpty()) {
            Log.w(TAG, "UID empty — user not logged in");
            hideShimmer();
            updateEmptyState(true);
            return;
        }

        ordersListener = db.collection("orders")
                .whereEqualTo("customerId", uid)
                .addSnapshotListener((snap, error) -> {
                    if (!isAdded()) return;

                    if (error != null) {
                        Log.e(TAG, "Firestore error: " + error.getMessage());
                        loadOrdersFallback();
                        return;
                    }

                    hideShimmer();

                    if (snap == null) {
                        updateEmptyState(true);
                        return;
                    }

                    Log.d(TAG, "Orders received: " + snap.size());
                    processDocuments(snap.getDocuments());
                });
    }

    private void loadOrdersFallback() {
        Log.d(TAG, "Fallback query with 'userId' field...");
        db.collection("orders")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;
                    hideShimmer();
                    Log.d(TAG, "Fallback: " + snap.size() + " orders");
                    processDocuments(snap.getDocuments());
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    hideShimmer();
                    Log.e(TAG, "Fallback failed: " + e.getMessage());
                    updateEmptyState(true);
                    Toast.makeText(requireContext(),
                            getString(R.string.msg_load_orders_failed), Toast.LENGTH_LONG).show();
                });
    }

    /** Shared processing logic for both primary and fallback query results. */
    private void processDocuments(List<DocumentSnapshot> rawDocs) {
        allOrders.clear();
        int prevPastCount = pastOrders.size();
        pastOrders.clear();
        boolean hasActive = false;

        // Sort by timestamp descending — avoids needing a Firestore composite index
        List<DocumentSnapshot> docs = new ArrayList<>(rawDocs);
        docs.sort((a, b) -> {
            Long ta = a.getLong("timestamp");
            Long tb = b.getLong("timestamp");
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return Long.compare(tb, ta);
        });

        for (DocumentSnapshot doc : docs) {
            Map<String, Object> order = doc.getData();
            if (order == null) continue;
            order.put("orderId", doc.getId());

            String status = (String) order.get("status");
            boolean completed = status == null
                    || status.equalsIgnoreCase("Delivered")
                    || status.equalsIgnoreCase("Cancelled");

            if (!completed && !hasActive) {
                hasActive = true;
                showActiveOrder(order);
            } else {
                allOrders.add(order);
                pastOrders.add(order);
            }
        }

        if (!hasActive) {
            layoutActiveOrder.setVisibility(View.GONE);
            pulseHandler.removeCallbacksAndMessages(null);
        }

        // Targeted notifications instead of notifyDataSetChanged
        if (prevPastCount > 0) ordersAdapter.notifyItemRangeRemoved(0, prevPastCount);
        if (!pastOrders.isEmpty()) ordersAdapter.notifyItemRangeInserted(0, pastOrders.size());

        updateEmptyState(!hasActive && pastOrders.isEmpty());
        layoutScratchCard.setVisibility(!pastOrders.isEmpty() ? View.VISIBLE : View.GONE);
        updateOrderCount();
    }

    // ══════════════════════════════════════════════════════════════════
    //  SHOW ACTIVE ORDER CARD
    // ══════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showActiveOrder(Map<String, Object> order) {
        layoutActiveOrder.setVisibility(View.VISIBLE);

        layoutActiveOrder.setAlpha(0f);
        layoutActiveOrder.setTranslationY(-30f);
        layoutActiveOrder.animate()
                .alpha(1f).translationY(0f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        Object nameObj = order.getOrDefault("restaurantName", getString(R.string.placeholder_restaurant));
        tvActiveRestaurant.setText(nameObj != null ? nameObj.toString() : getString(R.string.placeholder_restaurant));

        Object statusObj = order.get("status");
        String status = statusObj instanceof String ? (String) statusObj : "Placed";
        tvActiveStatus.setText(getStatusLabel(status));

        if (tvStatusIcon != null) tvStatusIcon.setText(getStatusEmoji(status));

        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        if (items != null && !items.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int limit = Math.min(items.size(), 3);
            for (int i = 0; i < limit; i++) {
                if (i > 0) sb.append(", ");
                Object name = items.get(i).get("name");
                if (name != null) sb.append(name);
            }
            if (items.size() > 3) {
                sb.append(" +");
                sb.append(items.size() - 3);
                sb.append(" more");
            }
            tvActiveItems.setText(sb.toString());
        }

        updateStepIndicator(status);
        updateProgressBar(status);
        startEtaPulse();
    }

    // ══════════════════════════════════════════════════════════════════
    //  ANIMATED STEP TRACKER
    // ══════════════════════════════════════════════════════════════════

    private void updateStepIndicator(String status) {
        if (stepPlaced == null) return;
        int step = statusToStep(status);

        updateStep(stepPlaced,    stepPlacedIcon,    1, step);
        updateStep(stepConfirmed, stepConfirmedIcon, 2, step);
        updateStep(stepPreparing, stepPreparingIcon, 3, step);
        updateStep(stepDelivery,  stepDeliveryIcon,  4, step);
        updateStep(stepDelivered, stepDeliveredIcon, 5, step);

        animateLine(linePlacedConfirmed,    step >= 2);
        animateLine(lineConfirmedPreparing, step >= 3);
        animateLine(linePreparingDelivery,  step >= 4);
        animateLine(lineDeliveryDelivered,  step >= 5);
    }

    private void updateStep(View stepView, TextView iconView, int stepNum, int currentStep) {
        if (stepView == null) return;
        boolean done   = stepNum < currentStep;
        boolean active = stepNum == currentStep;

        // Single bg_step_circle drawable — tinted at runtime, no need for two separate drawables
        stepView.setBackgroundResource(R.drawable.bg_step_circle);
        stepView.getBackground().setTint(done || active ? COLOR_ACTIVE : COLOR_INACTIVE);

        if (iconView != null) {
            if (done) {
                iconView.setText(R.string.step_check);
                iconView.setTextColor(Color.WHITE);
            } else {
                iconView.setText(String.valueOf(stepNum));
                iconView.setTextColor(active ? Color.WHITE : 0xFF9E9E9E);
            }
        }

        // Pulse active step — two separate ObjectAnimators (AnimatorSet has no setRepeatCount)
        if (active) {
            ObjectAnimator px = ObjectAnimator.ofFloat(stepView, "scaleX", 1f, 1.2f, 1f);
            px.setDuration(700);
            px.setRepeatCount(ValueAnimator.INFINITE);
            px.start();

            ObjectAnimator py = ObjectAnimator.ofFloat(stepView, "scaleY", 1f, 1.2f, 1f);
            py.setDuration(700);
            py.setRepeatCount(ValueAnimator.INFINITE);
            py.start();
        }
    }

    private void animateLine(View line, boolean activate) {
        if (line == null) return;
        ValueAnimator anim = ValueAnimator.ofArgb(COLOR_INACTIVE, activate ? COLOR_ACTIVE : COLOR_INACTIVE);
        anim.setDuration(500);
        anim.addUpdateListener(a -> line.setBackgroundColor((int) a.getAnimatedValue()));
        anim.start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  PROGRESS BAR
    // ══════════════════════════════════════════════════════════════════

    private void updateProgressBar(String status) {
        if (progressOrder == null) return;
        progressOrder.setProgressCompat(statusToStep(status) * 20, true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  ETA PULSE DOT
    // ══════════════════════════════════════════════════════════════════

    private void startEtaPulse() {
        if (etaPulseDot == null) return;
        ObjectAnimator p = ObjectAnimator.ofFloat(etaPulseDot, "alpha", 1f, 0.15f, 1f);
        p.setDuration(900);
        p.setRepeatCount(ValueAnimator.INFINITE);
        p.setInterpolator(new AccelerateDecelerateInterpolator());
        p.start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════

    private int statusToStep(String status) {
        if (status == null) return STEP_PLACED;
        switch (status) {
            case "Confirmed":          return STEP_CONFIRMED;
            case "Preparing":          return STEP_PREPARING;
            case "Picked Up":
            case "Out for Delivery":   return STEP_DELIVERY;
            case "Delivered":          return STEP_DELIVERED;
            default:                   return STEP_PLACED;
        }
    }

    private String getStatusLabel(String status) {
        if (status == null) return getString(R.string.status_placed);
        switch (status) {
            case "Confirmed":        return getString(R.string.status_confirmed);
            case "Preparing":        return getString(R.string.status_preparing);
            case "Picked Up":
            case "Out for Delivery": return getString(R.string.status_out_for_delivery);
            case "Delivered":        return getString(R.string.status_delivered);
            case "Cancelled":        return getString(R.string.status_cancelled);
            default:                 return getString(R.string.status_placed);
        }
    }

    private String getStatusEmoji(String status) {
        if (status == null) return "\uD83D\uDCCB";          // 📋
        switch (status) {
            case "Confirmed":        return "\u2705";        // ✅
            case "Preparing":        return "\uD83D\uDC68\u200D\uD83C\uDF73"; // 👨‍🍳
            case "Picked Up":
            case "Out for Delivery": return "\uD83D\uDEF5"; // 🛵
            case "Delivered":        return "\uD83C\uDF89"; // 🎉
            case "Cancelled":        return "\u274C";       // ❌
            default:                 return "\uD83D\uDCCB"; // 📋
        }
    }

    private void updateEmptyState(boolean show) {
        if (layoutEmpty == null) return;
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            layoutEmpty.setScaleX(0.8f);
            layoutEmpty.setScaleY(0.8f);
            layoutEmpty.setAlpha(0f);
            layoutEmpty.animate()
                    .scaleX(1f).scaleY(1f).alpha(1f)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }

    private void updateOrderCount() {
        if (tvOrderCount == null) return;
        int count = pastOrders.size();
        tvOrderCount.setText(count > 0
                ? getResources().getQuantityString(R.plurals.order_count, count, count)
                : "");
    }
}