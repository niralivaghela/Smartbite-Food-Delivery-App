package com.smartbite.fragments.restaurant;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.R;
import com.smartbite.adapters.MenuManageAdapter;
import com.smartbite.databinding.FragmentMenuManageBinding;
import com.smartbite.models.MenuItem;
import com.smartbite.utils.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MenuManageFragment extends Fragment {

    private FragmentMenuManageBinding binding;
    private FirebaseFirestore db;
    private String restaurantId;
    private MenuManageAdapter adapter;

    private final List<MenuItem> allItems    = new ArrayList<>();
    private final List<MenuItem> filteredItems = new ArrayList<>();
    private String activeCategory = "All";
    private String searchQuery    = "";

    // FAB expansion state
    private boolean fabExpanded = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuManageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        restaurantId = user.getUid();

        setupAdapter();
        setupRecyclerView();
        setupSwipeToDelete();
        setupSearchBar(view);
        setupExpandableFab();
        setupCategoryChips();
        showShimmer();
        loadMenuItems();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    private void setupAdapter() {
        adapter = new MenuManageAdapter(new ArrayList<>(),
                new MenuManageAdapter.OnMenuActionListener() {
                    @Override public void onEdit(MenuItem i)                          { showAddEditDialog(i); }
                    @Override public void onDelete(MenuItem i)                        { confirmDelete(i); }
                    @Override public void onToggleAvailability(MenuItem i, boolean a) { toggleAvailability(i, a); }
                });
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    /** ItemTouchHelper: swipe right = edit, swipe left = delete */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView rv,
                                            @NonNull RecyclerView.ViewHolder vh,
                                            @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                MenuItem item = adapter.getItemAt(pos);
                if (item == null) { adapter.notifyItemChanged(pos); return; }
                if (dir == ItemTouchHelper.LEFT) {
                    // Swipe left = delete with confirmation
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete \"" + item.getName() + "\"?")
                            .setPositiveButton("Delete", (d, w) -> deleteItem(item))
                            .setNegativeButton("Cancel", (d, w) -> adapter.notifyItemChanged(pos))
                            .setOnCancelListener(d -> adapter.notifyItemChanged(pos))
                            .show();
                } else {
                    // Swipe right = edit
                    adapter.notifyItemChanged(pos);
                    showAddEditDialog(item);
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView);
        // show hint once
        TextView hint = binding.getRoot().findViewById(R.id.tvSwipeHint);
        if (hint != null) hint.setVisibility(View.VISIBLE);
    }

    /** Search bar logic */
    private void setupSearchBar(View v) {
        EditText etSearch = v.findViewById(R.id.etSearchMenu);
        ImageView btnClear = v.findViewById(R.id.btnClearMenuSearch);
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                searchQuery = s.toString().trim().toLowerCase();
                if (btnClear != null)
                    btnClear.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilter();
            }
        });
        if (btnClear != null) btnClear.setOnClickListener(x -> etSearch.setText(""));
    }

    /** Expandable FAB: tap primary FAB to show/hide mini FABs */
    private void setupExpandableFab() {
        FloatingActionButton fabPrimary    = binding.getRoot().findViewById(R.id.fabAddItem);
        FloatingActionButton fabAvailable  = binding.getRoot().findViewById(R.id.fabMarkAllAvailable);
        FloatingActionButton fabSort       = binding.getRoot().findViewById(R.id.fabToggleCategory);

        fabPrimary.setOnClickListener(x -> {
            fabExpanded = !fabExpanded;
            if (fabExpanded) {
                if (fabAvailable != null) { fabAvailable.setVisibility(View.VISIBLE); fabAvailable.show(); }
                if (fabSort != null)      { fabSort.setVisibility(View.VISIBLE);      fabSort.show();      }
                fabPrimary.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                // small bounce
                ObjectAnimator.ofFloat(fabPrimary, "rotation", 0f, 135f).setDuration(200).start();
            } else {
                if (fabAvailable != null) fabAvailable.hide();
                if (fabSort      != null) fabSort.hide();
                fabPrimary.setImageResource(android.R.drawable.ic_input_add);
                ObjectAnimator.ofFloat(fabPrimary, "rotation", 135f, 0f).setDuration(200).start();
                // After hide animation completes
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (fabAvailable != null) fabAvailable.setVisibility(View.GONE);
                    if (fabSort      != null) fabSort.setVisibility(View.GONE);
                }, 300);
                showAddEditDialog(null);
            }
        });
        if (fabAvailable != null) fabAvailable.setOnClickListener(x -> markAllAvailable());
        if (fabSort      != null) fabSort.setOnClickListener(x -> showSortDialog());
    }

    private void setupCategoryChips() {
        ChipGroup cg = binding.getRoot().findViewById(R.id.chipGroupCategory);
        if (cg == null) return;
        String[] cats = {"All", "Starters", "Mains", "Biryani", "Pizza", "Burgers", "Drinks", "Dessert"};
        for (String c : cats) {
            Chip chip = new Chip(requireContext());
            chip.setText(c);
            chip.setCheckable(true);
            chip.setChecked(c.equals("All"));
            chip.setChipBackgroundColorResource(R.color.chip_selector);
            chip.setOnClickListener(v -> { activeCategory = c; applyFilter(); });
            cg.addView(chip);
        }
    }

    private void showShimmer() {
        ShimmerFrameLayout shimmer = binding.getRoot().findViewById(R.id.shimmerMenu);
        if (shimmer != null) { shimmer.setVisibility(View.VISIBLE); shimmer.startShimmer(); }
        binding.recyclerView.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        ShimmerFrameLayout shimmer = binding.getRoot().findViewById(R.id.shimmerMenu);
        if (shimmer != null) { shimmer.stopShimmer(); shimmer.setVisibility(View.GONE); }
        binding.recyclerView.setVisibility(View.VISIBLE);
        // show swipe hint
        View hint = binding.getRoot().findViewById(R.id.tvSwipeHint);
        if (hint != null) hint.setVisibility(View.VISIBLE);
    }

    private void loadMenuItems() {
        db.collection(Constants.COLLECTION_MENU_ITEMS)
                .whereEqualTo("restaurantId", restaurantId)
                .addSnapshotListener((snap, err) -> {
                    if (binding == null) return;
                    if (snap != null) {
                        allItems.clear();
                        allItems.addAll(snap.toObjects(MenuItem.class));
                        hideShimmer();
                        applyFilter();
                        updateItemCountBadge();
                    }
                });
    }

    private void applyFilter() {
        filteredItems.clear();
        for (MenuItem item : allItems) {
            boolean matchCat = activeCategory.equals("All")
                    || (item.getCategory() != null &&
                    item.getCategory().equalsIgnoreCase(activeCategory));
            boolean matchSearch = searchQuery.isEmpty()
                    || item.getName().toLowerCase().contains(searchQuery);
            if (matchCat && matchSearch) filteredItems.add(item);
        }
        adapter.updateList(new ArrayList<>(filteredItems));

        View emptyState = binding.getRoot().findViewById(R.id.layoutEmpty);
        if (emptyState != null)
            emptyState.setVisibility(filteredItems.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateItemCountBadge() {
        TextView badge = binding.getRoot().findViewById(R.id.tvMenuItemCount);
        if (badge != null) badge.setText(String.format(java.util.Locale.getDefault(), "%d items", allItems.size()));
    }

    private void markAllAvailable() {
        for (MenuItem item : allItems) {
            db.collection(Constants.COLLECTION_MENU_ITEMS)
                    .document(item.getItemId())
                    .update("available", true);
        }
        Toast.makeText(requireContext(), "✅ All items marked available", Toast.LENGTH_SHORT).show();
    }

    private void showSortDialog() {
        String[] opts = {"Name A–Z", "Price Low–High", "Price High–Low", "Category"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Sort Menu Items")
                .setItems(opts, (d, which) -> {
                    switch (which) {
                        case 0: allItems.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName())); break;
                        case 1: allItems.sort(Comparator.comparingDouble(MenuItem::getPrice)); break;
                        case 2: allItems.sort(Comparator.comparingDouble(MenuItem::getPrice).reversed()); break;
                        case 3: allItems.sort((a, b) -> {
                            String ca = a.getCategory() != null ? a.getCategory() : "";
                            String cb = b.getCategory() != null ? b.getCategory() : "";
                            return ca.compareToIgnoreCase(cb);
                        }); break;
                    }
                    applyFilter();
                    Toast.makeText(requireContext(), "Sorted!", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void showAddEditDialog(@Nullable MenuItem existing) {
        View dlg = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_menu_item, null);
        EditText etName     = dlg.findViewById(R.id.etItemName);
        EditText etPrice    = dlg.findViewById(R.id.etItemPrice);
        EditText etDesc     = dlg.findViewById(R.id.etItemDesc);
        EditText etCategory = dlg.findViewById(R.id.etItemCategory);
        if (existing != null) {
            etName.setText(existing.getName());
            etPrice.setText(String.valueOf(existing.getPrice()));
            etDesc.setText(existing.getDescription());
            etCategory.setText(existing.getCategory());
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(existing != null ? getString(R.string.title_edit_item) : getString(R.string.title_add_item))
                .setView(dlg)
                .setPositiveButton(getString(R.string.action_save), (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.error_fill_required_fields), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveMenuItem(name, Double.parseDouble(priceStr),
                            etDesc.getText().toString().trim(),
                            etCategory.getText().toString().trim(),
                            existing != null ? existing.getImage() : "", existing);
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void saveMenuItem(String name, double price, String desc, String cat,
                              String img, @Nullable MenuItem existing) {
        String id = existing != null ? existing.getItemId() : UUID.randomUUID().toString();
        MenuItem item = new MenuItem();
        item.setItemId(id); item.setName(name); item.setPrice(price);
        item.setDescription(desc); item.setCategory(cat);
        item.setImage(img); item.setRestaurantId(restaurantId); item.setAvailable(true);
        db.collection(Constants.COLLECTION_MENU_ITEMS).document(id).set(item)
                .addOnSuccessListener(u -> {
                    if (binding == null) return;
                    Toast.makeText(requireContext(),
                            getString(existing != null ? R.string.msg_item_updated : R.string.msg_item_added),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDelete(MenuItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.title_delete_item, item.getName()))
                .setPositiveButton(getString(R.string.action_delete), (d, w) -> deleteItem(item))
                .setNegativeButton(getString(R.string.action_cancel), null).show();
    }

    private void deleteItem(MenuItem item) {
        db.collection(Constants.COLLECTION_MENU_ITEMS).document(item.getItemId()).delete()
                .addOnSuccessListener(u -> {
                    if (binding == null) return;
                    Toast.makeText(requireContext(), getString(R.string.msg_item_deleted), Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleAvailability(MenuItem item, boolean available) {
        db.collection(Constants.COLLECTION_MENU_ITEMS).document(item.getItemId())
                .update("available", available)
                .addOnSuccessListener(u -> {
                    if (binding == null) return;
                    Toast.makeText(requireContext(),
                            getString(available ? R.string.msg_item_available : R.string.msg_item_unavailable),
                            Toast.LENGTH_SHORT).show();
                });
    }
}