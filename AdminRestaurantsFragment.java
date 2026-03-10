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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.smartbite.R;
import com.smartbite.adapters.AdminRestaurantAdapter;
import com.smartbite.models.Restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdminRestaurantsFragment extends Fragment {

    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmer;
    private AdminRestaurantAdapter adapter;
    private TextView tvCount;
    private View layoutEmpty;
    private ChipGroup chipGroupStatus;

    private final List<Restaurant> allRestaurants = new ArrayList<>();
    private String activeStatus = "All";
    private String searchQuery  = "";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_restaurants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        recyclerView   = v.findViewById(R.id.recyclerView);
        shimmer        = v.findViewById(R.id.shimmerRestaurants);
        tvCount        = v.findViewById(R.id.tvRestaurantCount);
        layoutEmpty    = v.findViewById(R.id.layoutEmpty);
        chipGroupStatus = v.findViewById(R.id.chipGroupStatus);

        adapter = new AdminRestaurantAdapter(new ArrayList<>(),
                new AdminRestaurantAdapter.OnAdminRestaurantAction() {
                    @Override public void onApprove(Restaurant r) { approveRestaurant(r); }
                    @Override public void onSuspend(Restaurant r) { suspendRestaurant(r); }
                    @Override public void onDelete(Restaurant r)  { showApproveRejectDialog(r); }
                });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        setupChips();
        setupSearch(v);
        setupFab(v);
        showShimmer();
        listenToRestaurants();
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
            if      (id == R.id.chipOpen)    activeStatus = "Open";
            else if (id == R.id.chipClosed)  activeStatus = "Closed";
            else if (id == R.id.chipPending) activeStatus = "Pending";
            else                             activeStatus = "All";
            applyFilter();
        });
    }

    private void setupSearch(View v) {
        EditText et = v.findViewById(R.id.etSearchRestaurants);
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

    private void setupFab(View v) {
        FloatingActionButton fab = v.findViewById(R.id.fabAddRestaurant);
        if (fab != null) fab.setOnClickListener(x ->
                Toast.makeText(requireContext(), "➕ Add Restaurant — Coming Soon!", Toast.LENGTH_SHORT).show());
    }

    private void listenToRestaurants() {
        listener = db.collection("restaurants")
                .addSnapshotListener((snap, err) -> {
                    if (!isAdded()) return;
                    hideShimmer();
                    if (snap != null) {
                        allRestaurants.clear();
                        allRestaurants.addAll(snap.toObjects(Restaurant.class));
                        applyFilter();
                    }
                });
    }

    private void applyFilter() {
        List<Restaurant> filtered = allRestaurants.stream().filter(r -> {
            boolean matchStatus;
            switch (activeStatus) {
                case "Open":    matchStatus = r.isOpen();      break;
                case "Closed":  matchStatus = !r.isOpen();     break;
                case "Pending": matchStatus = !r.isApproved(); break;
                default:        matchStatus = true;            break;
            }
            boolean matchSearch = searchQuery.isEmpty()
                    || r.getName().toLowerCase(Locale.getDefault()).contains(searchQuery);
            return matchStatus && matchSearch;
        }).collect(Collectors.toList());

        adapter.updateList(filtered);
        if (tvCount    != null) tvCount.setText(String.format(Locale.getDefault(), "%d restaurants", filtered.size()));
        if (layoutEmpty != null) layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showApproveRejectDialog(Restaurant r) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Manage: " + r.getName())
                .setItems(new String[]{"✅ Approve", "🚫 Suspend", "👁 View Details"}, (d, i) -> {
                    if (i == 0) approveRestaurant(r);
                    else if (i == 1) suspendRestaurant(r);
                    else Toast.makeText(requireContext(), "Details — Coming Soon!", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void approveRestaurant(Restaurant r) {
        db.collection("restaurants").document(r.getId())
                .update("approved", true)
                .addOnSuccessListener(u -> Toast.makeText(requireContext(),
                        "✅ " + r.getName() + " approved!", Toast.LENGTH_SHORT).show());
    }

    private void suspendRestaurant(Restaurant r) {
        db.collection("restaurants").document(r.getId())
                .update("isOpen", false)
                .addOnSuccessListener(u -> Toast.makeText(requireContext(),
                        "🚫 " + r.getName() + " suspended!", Toast.LENGTH_SHORT).show());
    }
}