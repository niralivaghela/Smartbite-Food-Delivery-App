package com.smartbite.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.smartbite.R;
import com.smartbite.databinding.ActivityDeliveryAgentBinding;
import com.smartbite.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryAgentActivity extends AppCompatActivity {

    private ActivityDeliveryAgentBinding binding;
    private FirebaseFirestore db;
    private String agentId;
    private boolean isAvailable = false;
    private ListenerRegistration ordersListener;

    private int totalDeliveries = 0;
    private double totalEarnings = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryAgentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFirebase();
        setupAvailabilityToggle();
        loadStats();
        listenForOrders();
        playEntranceAnimation();
    }

    private void setupToolbar() {
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Delivery Panel 🛵");
        }
        if (binding.btnLogout != null) {
            binding.btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            });
        }
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        agentId = user.getUid();

        if (binding.tvAgentName != null && user.getDisplayName() != null) {
            binding.tvAgentName.setText(user.getDisplayName());
        }
    }

    private void setupAvailabilityToggle() {
        if (binding.switchAvailable == null) return;
        binding.switchAvailable.setOnCheckedChangeListener((btn, checked) -> {
            isAvailable = checked;
            updateAvailabilityInFirestore(checked);
            updateAvailabilityUI(checked);
        });
    }

    private void updateAvailabilityUI(boolean available) {
        if (binding.tvStatus != null) {
            binding.tvStatus.setText(available ? "🟢 Available" : "🔴 Offline");
            binding.tvStatus.setTextColor(getColor(available ? R.color.success : R.color.error));
        }
        // Pulse animation when going online
        if (available && binding.cardStatus != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.cardStatus, "scaleX", 1f, 1.05f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.cardStatus, "scaleY", 1f, 1.05f, 1f);
            android.animation.AnimatorSet set = new android.animation.AnimatorSet();
            set.playTogether(scaleX, scaleY);
            set.setDuration(400).start();
        }
    }

    private void updateAvailabilityInFirestore(boolean available) {
        if (agentId == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("available", available);
        data.put("lastSeen", com.google.firebase.firestore.FieldValue.serverTimestamp());
        db.collection(Constants.COLLECTION_AGENTS).document(agentId).set(data,
                com.google.firebase.firestore.SetOptions.merge());
    }

    private void loadStats() {
        if (agentId == null) {
            showStats(5, 750.0); // Demo values
            return;
        }
        db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("agentId", agentId)
                .whereEqualTo("status", "Delivered")
                .get()
                .addOnSuccessListener(snap -> {
                    totalDeliveries = snap.size();
                    totalEarnings = totalDeliveries * 25.0; // ₹25 per delivery
                    showStats(totalDeliveries, totalEarnings);
                })
                .addOnFailureListener(e -> showStats(5, 750.0));
    }

    private void showStats(int deliveries, double earnings) {
        if (binding.tvTotalDeliveries != null)
            binding.tvTotalDeliveries.setText(String.valueOf(deliveries));
        if (binding.tvTotalEarnings != null)
            binding.tvTotalEarnings.setText(String.format("₹%.0f", earnings));
        if (binding.tvTodayCount != null)
            binding.tvTodayCount.setText(String.valueOf(Math.min(deliveries, 3)));
    }

    private void listenForOrders() {
        ordersListener = db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("status", "Picked Up")
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    List<Object[]> activeOrders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        activeOrders.add(new Object[]{
                                doc.getId(),
                                doc.getString("restaurantName"),
                                doc.getString("deliveryAddress"),
                                doc.getDouble("totalAmount")
                        });
                    }
                    showActiveOrders(activeOrders);
                });
    }

    private void showActiveOrders(List<Object[]> orders) {
        if (binding.tvActiveOrderCount != null)
            binding.tvActiveOrderCount.setText(orders.size() + " active");

        if (binding.layoutNoOrders != null)
            binding.layoutNoOrders.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);

        // Build order cards dynamically or use adapter — basic demo
        if (!orders.isEmpty() && binding.tvCurrentDelivery != null) {
            Object[] first = orders.get(0);
            binding.tvCurrentDelivery.setText(
                    "Delivering to: " + (first[2] != null ? first[2] : "—"));
        }
    }

    private void openNavigation(String address) {
        if (address == null || address.isEmpty()) return;
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://maps.google.com/?q=" + Uri.encode(address))));
        }
    }

    private void markDelivered(String orderId) {
        if (orderId == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Mark as Delivered? ✅")
                .setMessage("Confirm delivery of order #" + orderId.substring(0, 8).toUpperCase())
                .setPositiveButton("Mark Delivered", (d, w) -> {
                    db.collection(Constants.COLLECTION_ORDERS).document(orderId)
                            .update("status", "Delivered")
                            .addOnSuccessListener(x -> {
                                totalDeliveries++;
                                totalEarnings += 25;
                                showStats(totalDeliveries, totalEarnings);
                                Toast.makeText(this, "🎉 Delivery completed! ₹25 earned", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void playEntranceAnimation() {
        View[] views = {binding.cardStatus, binding.cardStats, binding.cardCurrentOrder};
        int delay = 0;
        for (View v : views) {
            if (v == null) continue;
            v.setAlpha(0f);
            v.setTranslationY(50f);
            v.animate().alpha(1f).translationY(0f)
                    .setDuration(450).setStartDelay(delay)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();
            delay += 120;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersListener != null) ordersListener.remove();
        // Go offline when leaving
        updateAvailabilityInFirestore(false);
    }
}