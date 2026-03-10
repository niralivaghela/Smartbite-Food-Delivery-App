package com.smartbite.fragments.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.smartbite.R;
import com.smartbite.adapters.AdminOrdersAdapter;
import com.smartbite.models.Order;
import com.smartbite.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdminOrdersFragment extends Fragment {

    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmer;
    private AdminOrdersAdapter adapter;
    private TextView tvOrderCount;
    private View layoutEmpty;
    private ChipGroup chipGroupStatus;

    private final List<Order> allOrders = new ArrayList<>();
    private String activeStatus = "All";
    private String searchQuery  = "";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        recyclerView  = v.findViewById(R.id.recyclerView);
        shimmer       = v.findViewById(R.id.shimmerOrders);
        tvOrderCount  = v.findViewById(R.id.tvOrderCount);
        layoutEmpty   = v.findViewById(R.id.layoutEmpty);
        chipGroupStatus = v.findViewById(R.id.chipGroupStatus);

        adapter = new AdminOrdersAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        setupChips();
        setupSearch(v);
        showShimmer();
        listenToOrders();
        animateCardsIn();
    }

    @Override public void onDestroyView() { super.onDestroyView(); if (listener != null) listener.remove(); }

    private void showShimmer() {
        if (shimmer != null) { shimmer.setVisibility(View.VISIBLE); shimmer.startShimmer(); }
        recyclerView.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        if (shimmer != null) { shimmer.stopShimmer(); shimmer.setVisibility(View.GONE); }
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setupChips() {
        if (chipGroupStatus == null) return;
        chipGroupStatus.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            activeStatus = id == R.id.chipPlaced    ? "Placed"
                    : id == R.id.chipPreparing ? "Preparing"
                    : id == R.id.chipDelivered ? "Delivered"
                    : id == R.id.chipCancelled ? "Cancelled" : "All";
            applyFilter();
        });
    }

    private void setupSearch(View v) {
        EditText et = v.findViewById(R.id.etSearchOrders);
        if (et == null) return;
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                searchQuery = s.toString().trim().toLowerCase(Locale.getDefault());
                applyFilter();
            }
        });
    }

    private void listenToOrders() {
        listener = db.collection(Constants.COLLECTION_ORDERS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (!isAdded()) return;
                    hideShimmer();
                    if (snap != null) {
                        allOrders.clear();
                        allOrders.addAll(snap.toObjects(Order.class));
                        applyFilter();
                    }
                });
    }

    private void applyFilter() {
        List<Order> filtered = allOrders.stream().filter(o -> {
            boolean matchStatus = activeStatus.equals("All") ||
                    activeStatus.equalsIgnoreCase(o.getStatus());
            boolean matchSearch = searchQuery.isEmpty() ||
                    (o.getOrderId() != null && o.getOrderId().toLowerCase(Locale.getDefault()).contains(searchQuery)) ||
                    (o.getRestaurantName() != null && o.getRestaurantName().toLowerCase(Locale.getDefault()).contains(searchQuery));
            return matchStatus && matchSearch;
        }).collect(Collectors.toList());

        adapter.updateList(filtered);
        if (tvOrderCount != null) tvOrderCount.setText(String.format(Locale.getDefault(), "%d orders", filtered.size()));
        if (layoutEmpty != null)
            layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void animateCardsIn() {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View child = recyclerView.getChildAt(i);
                if (child == null) continue;
                child.setTranslationX(150f); child.setAlpha(0f);
                child.animate().translationX(0f).alpha(1f)
                        .setStartDelay(50L * i).setDuration(250).start();
            }
        }, 500);
    }
}