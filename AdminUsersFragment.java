package com.smartbite.fragments.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.smartbite.R;
import com.smartbite.adapters.AdminUsersAdapter;
import com.smartbite.models.User;
import com.smartbite.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminUsersFragment extends Fragment {

    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmer;
    private AdminUsersAdapter adapter;
    private TextView tvUserCount;
    private View layoutEmpty;
    private ChipGroup chipGroupRole;

    private final List<User> allUsers = new ArrayList<>();
    private String activeRole   = "All";
    private String searchQuery  = "";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        recyclerView  = v.findViewById(R.id.recyclerView);
        shimmer       = v.findViewById(R.id.shimmerUsers);
        tvUserCount   = v.findViewById(R.id.tvUserCount);
        layoutEmpty   = v.findViewById(R.id.layoutEmpty);
        chipGroupRole = v.findViewById(R.id.chipGroupRole);

        adapter = new AdminUsersAdapter(requireContext(), new ArrayList<>(), this::showUserActions);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        setupChips();
        setupSearch(v);
        showShimmer();
        listenToUsers();
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
        if (chipGroupRole == null) return;
        chipGroupRole.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            if      (id == R.id.chipCustomers)   activeRole = "customer";
            else if (id == R.id.chipRestaurants) activeRole = "restaurant";
            else if (id == R.id.chipBanned)      activeRole = "banned";
            else                                  activeRole = "All";
            applyFilter();
        });
    }

    private void setupSearch(View v) {
        EditText et = v.findViewById(R.id.etSearchUsers);
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

    private void listenToUsers() {
        listener = db.collection("users")
                .addSnapshotListener((snap, err) -> {
                    if (!isAdded()) return;
                    hideShimmer();
                    if (snap != null) {
                        allUsers.clear();
                        allUsers.addAll(snap.toObjects(User.class));
                        applyFilter();
                    }
                });
    }

    private void applyFilter() {
        List<User> filtered = allUsers.stream().filter(u -> {
            boolean matchRole = activeRole.equals("All")
                    || (u.getRole() != null && u.getRole().equalsIgnoreCase(activeRole))
                    || (activeRole.equals("banned") && Boolean.TRUE.equals(u.getBanned()));
            boolean matchSearch = searchQuery.isEmpty()
                    || (u.getName() != null && u.getName().toLowerCase(Locale.getDefault()).contains(searchQuery))
                    || (u.getEmail() != null && u.getEmail().toLowerCase(Locale.getDefault()).contains(searchQuery));
            return matchRole && matchSearch;
        }).collect(Collectors.toList());

        adapter.updateList(filtered);
        if (tvUserCount != null) tvUserCount.setText(String.format(Locale.getDefault(), "%d users", filtered.size()));
        if (layoutEmpty != null) layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showUserActions(User user) {
        boolean isBanned = Boolean.TRUE.equals(user.getBanned());
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(user.getName() != null ? user.getName() : "User Actions")
                .setItems(new String[]{
                        isBanned ? "🔓 Unban User" : "🚫 Ban User",
                        "📧 Send Email",
                        "👁 View Orders"
                }, (d, i) -> {
                    if (i == 0) toggleBan(user, isBanned);
                    else if (i == 1) Toast.makeText(requireContext(), "Email — Coming Soon!", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(requireContext(), "User Orders — Coming Soon!", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void toggleBan(User user, boolean currentlyBanned) {
        db.collection("users").document(user.getUid())
                .update("banned", !currentlyBanned)
                .addOnSuccessListener(u -> Toast.makeText(requireContext(),
                        currentlyBanned ? "✅ User unbanned" : "🚫 User banned",
                        Toast.LENGTH_SHORT).show());
    }
}