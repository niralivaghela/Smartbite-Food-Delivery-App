package com.smartbite.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.R;
import com.smartbite.databinding.ActivityRestaurantPanelBinding;
import com.smartbite.fragments.restaurant.MenuManageFragment;
import com.smartbite.fragments.restaurant.OrderManageFragment;
import com.smartbite.fragments.restaurant.RevenueFragment;
import com.smartbite.utils.Constants;

public class RestaurantPanelActivity extends AppCompatActivity {

    private ActivityRestaurantPanelBinding binding;
    private FirebaseFirestore db;
    private String restaurantId;
    private int pendingOrderCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFirebase();
        setupBottomNav();
        loadFragment(new OrderManageFragment());
        loadPendingOrderCount();
        playEntranceAnimation();
    }

    private void setupToolbar() {
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            // No back button — this is root panel
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Restaurant Panel 🏪");
        }

        // Logout
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

        // Get restaurantId linked to this owner
        db.collection(Constants.COLLECTION_RESTAURANTS)
                .whereEqualTo("ownerId", user.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        restaurantId = snap.getDocuments().get(0).getId();
                        if (binding.tvRestaurantName != null) {
                            String name = snap.getDocuments().get(0).getString("name");
                            if (name != null) binding.tvRestaurantName.setText(name);
                        }
                    }
                });
    }

    private void setupBottomNav() {
        if (binding.bottomNav == null) return;
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment;
            if (id == R.id.nav_orders) {
                fragment = new OrderManageFragment();
            } else if (id == R.id.nav_menu) {
                fragment = new MenuManageFragment();
            } else {
                fragment = new RevenueFragment();
            }
            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void loadPendingOrderCount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Listen for pending orders in real-time
        db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("status", "Placed")
                .addSnapshotListener((snap, e) -> {
                    if (snap != null) {
                        pendingOrderCount = snap.size();
                        updateOrderBadge(pendingOrderCount);
                    }
                });
    }

    private void updateOrderBadge(int count) {
        if (binding.bottomNav == null) return;
        if (count > 0) {
            binding.bottomNav.getOrCreateBadge(R.id.nav_orders).setNumber(count);
        } else {
            binding.bottomNav.removeBadge(R.id.nav_orders);
        }
    }

    private void playEntranceAnimation() {
        if (binding.toolbar != null) {
            binding.toolbar.setAlpha(0f);
            binding.toolbar.setTranslationY(-20f);
            binding.toolbar.animate().alpha(1f).translationY(0f)
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }
}