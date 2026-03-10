package com.smartbite.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smartbite.R;
import com.smartbite.adapters.RestaurantAdapter;
import com.smartbite.databinding.ActivitySearchBinding;
import com.smartbite.models.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private RestaurantAdapter adapter;
    private final List<Restaurant> allRestaurants = new ArrayList<>();
    private final List<Restaurant> filtered = new ArrayList<>();
    private String activeFilter = "All";

    private static final String[][] RESTAURANT_DATA = {
            {"r1", "Spice Garden",  "North Indian • Biryani",    "4.5", "30", "25",
                    "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=400"},
            {"r2", "Pizza Palace",  "Pizza • Italian",           "4.3",  "0", "20",
                    "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400"},
            {"r3", "Burger Barn",   "Burger • Fast Food",        "4.1", "20", "15",
                    "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400"},
            {"r4", "Dragon Wok",    "Chinese • Noodles",         "4.4", "25", "30",
                    "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400"},
            {"r5", "South Tadka",   "South Indian • Dosa",       "4.6",  "0", "20",
                    "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=400"},
            {"r6", "Sweet Tooth",   "Dessert • Ice Cream",       "4.2", "15", "25",
                    "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=400"},
    };

    private final ActivityResultLauncher<Intent> voiceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    List<String> spoken = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (spoken != null && !spoken.isEmpty() && binding.etSearch != null) {
                        binding.etSearch.setText(spoken.get(0));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        loadRestaurants();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupVoiceSearch();
        playEntranceAnimation();
    }

    private void setupToolbar() {
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            binding.toolbar.setNavigationOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed());
        }
    }

    private void loadRestaurants() {
        allRestaurants.clear();
        for (String[] d : RESTAURANT_DATA) {
            Restaurant r = new Restaurant(
                    d[0], d[1], d[2],
                    Float.parseFloat(d[3]),
                    Integer.parseInt(d[4]),
                    Integer.parseInt(d[5]),
                    true, true, d[6]);
            allRestaurants.add(r);
        }
        filtered.addAll(allRestaurants);
    }

    private void setupRecyclerView() {
        adapter = new RestaurantAdapter(this, new ArrayList<>(allRestaurants),
                this::openRestaurant, RestaurantAdapter.TYPE_FULL);
        if (binding.rvResults != null) {
            binding.rvResults.setLayoutManager(new LinearLayoutManager(this));
            binding.rvResults.setAdapter(adapter);
        }
        updateResultCount(allRestaurants.size());
    }

    private void setupSearch() {
        if (binding.etSearch == null) return;
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterRestaurants(s.toString().trim());
            }
        });

        // Auto-focus search
        binding.etSearch.requestFocus();
    }

    private void setupFilterChips() {
        String[] filterIds = {"All", "Pizza", "Burger", "Biryani", "Chinese", "South Indian", "Dessert"};

        // Map chip views to filter values
        View[] chips = {
                binding.chipAll, binding.chipPizza, binding.chipBurger,
                binding.chipBiryani, binding.chipChinese, binding.chipSouthIndian,
                binding.chipDessert
        };

        for (int i = 0; i < chips.length; i++) {
            if (chips[i] == null) continue;
            final String filter = filterIds[i];
            chips[i].setOnClickListener(v -> {
                activeFilter = filter;
                filterRestaurants(binding.etSearch != null
                        ? binding.etSearch.getText().toString().trim() : "");
                updateChipSelection(chips, v);
            });
        }
    }

    private void updateChipSelection(View[] chips, View selected) {
        for (View chip : chips) {
            if (chip == null) continue;
            chip.setSelected(chip == selected);
        }
    }

    private void setupVoiceSearch() {
        if (binding.btnVoice == null) return;
        binding.btnVoice.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a restaurant or food name...");
            try {
                voiceLauncher.launch(intent);
                // Animate mic button
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                        .start();
            } catch (Exception e) {
                Toast.makeText(this, "Voice search not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRestaurants(String query) {
        filtered.clear();
        String q = query.toLowerCase(Locale.ROOT);

        for (Restaurant r : allRestaurants) {
            boolean matchesQuery = q.isEmpty()
                    || r.getName().toLowerCase().contains(q)
                    || (r.getCuisine() != null && r.getCuisine().toLowerCase().contains(q));

            boolean matchesFilter = activeFilter.equals("All")
                    || (r.getCuisine() != null
                    && r.getCuisine().toLowerCase().contains(activeFilter.toLowerCase()));

            if (matchesQuery && matchesFilter) filtered.add(r);
        }

        adapter.updateList(new ArrayList<>(filtered));
        updateResultCount(filtered.size());

        // Show/hide empty state
        if (binding.layoutEmpty != null)
            binding.layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        if (binding.rvResults != null)
            binding.rvResults.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateResultCount(int count) {
        if (binding.tvResultCount != null)
            binding.tvResultCount.setText(count + " restaurant" + (count == 1 ? "" : "s") + " found");
    }

    private void openRestaurant(Restaurant r) {
        Intent intent = new Intent(this, RestaurantDetailActivity.class);
        intent.putExtra("restaurantId", r.getId());
        intent.putExtra("restaurantName", r.getName());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void playEntranceAnimation() {
        if (binding.cardSearch != null) {
            binding.cardSearch.setTranslationY(-30f);
            binding.cardSearch.setAlpha(0f);
            binding.cardSearch.animate().translationY(0f).alpha(1f)
                    .setDuration(400).setInterpolator(new OvershootInterpolator(1.2f)).start();
        }
        if (binding.rvResults != null) {
            binding.rvResults.setAlpha(0f);
            binding.rvResults.animate().alpha(1f)
                    .setDuration(400).setStartDelay(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator()).start();
        }
    }
}