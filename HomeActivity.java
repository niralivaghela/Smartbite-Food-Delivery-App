package com.smartbite.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.R;
import com.smartbite.databinding.ActivityHomeBinding;
import com.smartbite.fragments.customer.HomeFragment;
import com.smartbite.fragments.customer.OrdersFragment;
import com.smartbite.fragments.customer.ProfileFragment;
import com.smartbite.fragments.customer.SearchFragment;
import com.smartbite.utils.Constants;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private FirebaseFirestore db;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupFirebase();
        setupBottomNav();
        loadFragment(new HomeFragment());
        listenForActiveOrders();
        playEntranceAnimation();
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener((MenuItem item) -> {
            Fragment fragment;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_search) {
                fragment = new SearchFragment();
            } else if (id == R.id.nav_orders) {
                fragment = new OrdersFragment();
                // Clear badge when opening orders
                binding.bottomNav.removeBadge(R.id.nav_orders);
            } else {
                fragment = new ProfileFragment();
            }

            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        currentFragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /** Listen for active orders and show a badge on the orders tab */
    private void listenForActiveOrders() {
        com.google.firebase.auth.FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("customerId", user.getUid())
                .whereNotEqualTo("status", "Delivered")
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    int active = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        String status = doc.getString("status");
                        if (status != null && !status.equals("Cancelled")
                                && !status.equals("Delivered")) {
                            active++;
                        }
                    }
                    if (active > 0) {
                        binding.bottomNav.getOrCreateBadge(R.id.nav_orders).setNumber(active);
                    } else {
                        binding.bottomNav.removeBadge(R.id.nav_orders);
                    }
                });
    }

    private void playEntranceAnimation() {
        binding.bottomNav.setTranslationY(120f);
        binding.bottomNav.setAlpha(0f);
        binding.bottomNav.animate()
                .translationY(0f).alpha(1f)
                .setDuration(500).setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }
}