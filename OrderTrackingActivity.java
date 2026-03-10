package com.smartbite.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.smartbite.R;
import com.smartbite.databinding.ActivityOrderTrackingBinding;

import java.util.Arrays;
import java.util.List;

public class OrderTrackingActivity extends AppCompatActivity {

    private ActivityOrderTrackingBinding binding;
    private FirebaseFirestore db;
    private ListenerRegistration statusListener;

    private View stepPlaced, stepConfirmed, stepPreparing, stepDelivery, stepDelivered;
    private View linePlacedConfirmed, lineConfirmedPreparing, linePreparingDelivery, lineDeliveryDelivered;

    private String currentStatus = "";

    private static final List<String> STAGES = Arrays.asList(
            "placed", "confirmed", "preparing", "out_for_delivery", "delivered");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        db = FirebaseFirestore.getInstance();

        String orderId        = getIntent().getStringExtra("orderId");
        String restaurantName = getIntent().getStringExtra("restaurantName");
        int    totalAmount    = getIntent().getIntExtra("totalAmount", 0);
        String itemsSummary   = getIntent().getStringExtra("itemsSummary");

        stepPlaced             = findViewById(R.id.stepPlaced);
        stepConfirmed          = findViewById(R.id.stepConfirmed);
        stepPreparing          = findViewById(R.id.stepPreparing);
        stepDelivery           = findViewById(R.id.stepDelivery);
        stepDelivered          = findViewById(R.id.stepDelivered);
        linePlacedConfirmed    = findViewById(R.id.linePlacedConfirmed);
        lineConfirmedPreparing = findViewById(R.id.lineConfirmedPreparing);
        linePreparingDelivery  = findViewById(R.id.linePreparingDelivery);
        lineDeliveryDelivered  = findViewById(R.id.lineDeliveryDelivered);

        TextView tvRestaurant = findViewById(R.id.tvRestaurantName);
        TextView tvOrderId    = findViewById(R.id.tvOrderId);
        TextView tvTotal      = findViewById(R.id.tvOrderTotal);
        TextView tvItems      = findViewById(R.id.tvOrderItems);

        if (tvRestaurant != null)
            tvRestaurant.setText(restaurantName != null ? restaurantName : getString(R.string.label_restaurant));

        if (tvOrderId != null && orderId != null)
            tvOrderId.setText(getString(R.string.label_order_id_prefix,
                    orderId.substring(0, Math.min(orderId.length(), 8)).toUpperCase()));

        if (tvTotal != null)
            tvTotal.setText(getString(R.string.label_rupee_amount, totalAmount));

        if (tvItems != null && itemsSummary != null)
            tvItems.setText(itemsSummary);

        TextView btnCall = findViewById(R.id.btnCallAgent);
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                Intent call = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:+919876543210"));
                startActivity(call);
            });
        }

        MaterialButton btnHome = findViewById(R.id.btnBackHome);
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> goHome());
        }

        if (orderId != null && !orderId.isEmpty()) {
            attachRealtimeListener(orderId);
        } else {
            updateUI("preparing");
        }

        playEntranceAnim();
    }

    private void goHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private void attachRealtimeListener(String orderId) {
        statusListener = db.collection("orders").document(orderId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists()) {
                        updateUI("preparing");
                        return;
                    }
                    String status = doc.getString("status");
                    String newStatus = status != null ? status : "preparing";
                    if (!newStatus.equals(currentStatus)) {
                        currentStatus = newStatus;
                        updateUI(currentStatus);
                    }
                });
    }

    private void updateUI(String status) {
        TextView tvLabel = findViewById(R.id.tvStatusLabel);
        TextView tvSub   = findViewById(R.id.tvStatusSub);
        TextView tvEst   = findViewById(R.id.tvEstTime);

        if (tvLabel != null) {
            tvLabel.animate().alpha(0f).setDuration(150).withEndAction(() -> {
                tvLabel.setText(getStatusLabel(status));
                tvLabel.animate().alpha(1f).setDuration(200).start();
            }).start();
        }
        if (tvSub != null) tvSub.setText(getStatusSub(status));
        if (tvEst != null) tvEst.setText(getEstTime(status));

        updateStepIndicator(status);
    }

    private void updateStepIndicator(String status) {
        if (stepPlaced == null) return;

        int grey   = 0xFFDDDDDD;
        int orange = 0xFFFC8019;

        stepPlaced.setBackgroundColor(orange);

        boolean confirmed = isAtLeast(status, "confirmed");
        boolean preparing = isAtLeast(status, "preparing");
        boolean delivery  = isAtLeast(status, "out_for_delivery");
        boolean delivered = "delivered".equals(status);

        setStepColor(stepConfirmed, confirmed ? orange : grey);
        setStepColor(stepPreparing, preparing ? orange : grey);
        setStepColor(stepDelivery,  delivery  ? orange : grey);
        setStepColor(stepDelivered, delivered ? orange : grey);

        animateLine(linePlacedConfirmed,    confirmed ? orange : grey);
        animateLine(lineConfirmedPreparing, preparing ? orange : grey);
        animateLine(linePreparingDelivery,  delivery  ? orange : grey);
        animateLine(lineDeliveryDelivered,  delivered ? orange : grey);

        View activeStep = delivered ? stepDelivered
                : delivery  ? stepDelivery
                : preparing ? stepPreparing
                : confirmed ? stepConfirmed
                : stepPlaced;
        pulseView(activeStep);
    }

    private boolean isAtLeast(String status, String target) {
        int si = STAGES.indexOf(status);
        int ti = STAGES.indexOf(target);
        return si >= ti && ti >= 0;
    }

    private void setStepColor(View view, int color) {
        if (view == null) return;
        Object tag = view.getTag();
        int fromColor = (tag instanceof Integer) ? (Integer) tag : 0xFFDDDDDD;
        ValueAnimator anim = ValueAnimator.ofArgb(fromColor, color);
        anim.setDuration(400);
        anim.addUpdateListener(a -> view.setBackgroundColor((int) a.getAnimatedValue()));
        anim.start();
        view.setTag(color);
    }

    private void animateLine(View line, int color) {
        if (line != null) line.setBackgroundColor(color);
    }

    private void pulseView(View v) {
        if (v == null) return;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.25f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.25f, 1f);
        scaleX.setDuration(600); scaleX.setRepeatCount(2);
        scaleY.setDuration(600); scaleY.setRepeatCount(2);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start(); scaleY.start();
    }

    private void playEntranceAnim() {
        View root = binding.getRoot();
        root.setAlpha(0f);
        root.setTranslationY(30f);
        root.animate().alpha(1f).translationY(0f).setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private String getStatusLabel(String s) {
        switch (s) {
            case "confirmed":        return getString(R.string.status_confirmed);
            case "preparing":        return getString(R.string.status_preparing);
            case "out_for_delivery": return getString(R.string.status_out_for_delivery);
            case "delivered":        return getString(R.string.status_delivered);
            default:                 return getString(R.string.status_placed);
        }
    }

    private String getStatusSub(String s) {
        switch (s) {
            case "confirmed":        return getString(R.string.status_sub_confirmed);
            case "preparing":        return getString(R.string.status_sub_preparing);
            case "out_for_delivery": return getString(R.string.status_sub_out_for_delivery);
            case "delivered":        return getString(R.string.status_sub_delivered);
            default:                 return getString(R.string.status_sub_placed);
        }
    }

    private String getEstTime(String s) {
        switch (s) {
            case "confirmed":        return getString(R.string.est_time_confirmed);
            case "preparing":        return getString(R.string.est_time_preparing);
            case "out_for_delivery": return getString(R.string.est_time_out_for_delivery);
            case "delivered":        return getString(R.string.est_time_delivered);
            default:                 return getString(R.string.est_time_placed);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) statusListener.remove();
    }
}