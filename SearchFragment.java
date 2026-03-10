package com.smartbite.fragments.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.smartbite.R;
import com.smartbite.activities.RestaurantDetailActivity;
import com.smartbite.adapters.RestaurantAdapter;
import com.smartbite.models.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchFragment extends Fragment {

    private AutoCompleteTextView etSearch;
    private RecyclerView         rvResults;
    private ChipGroup            chipGroup;
    private View                 layoutEmpty;
    private RestaurantAdapter    adapter;

    private final List<Restaurant> allRestaurants = new ArrayList<>();
    private final List<Restaurant> filtered       = new ArrayList<>();
    private String activeCategory = "All";

    // {cardId, imgId, label, emoji, imageUrl}
    private static final Object[][] CATEGORIES = {
            {R.id.cardAll,         R.id.imgAll,         "All",          "🍽", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=300"},
            {R.id.cardPizza,       R.id.imgPizza,       "Pizza",        "🍕", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=300"},
            {R.id.cardBurger,      R.id.imgBurger,      "Burger",       "🍔", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300"},
            {R.id.cardBiryani,     R.id.imgBiryani,     "Biryani",      "🍛", "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=300"},
            {R.id.cardChinese,     R.id.imgChinese,     "Chinese",      "🍜", "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=300"},
            {R.id.cardSouthIndian, R.id.imgSouthIndian, "South Indian", "🥘", "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=300"},
            {R.id.cardNorthIndian, R.id.imgNorthIndian, "North Indian", "🍲", "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=300"},
            {R.id.cardNoodles,     R.id.imgNoodles,     "Noodles",      "🍝", "https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=300"},
            {R.id.cardThali,       R.id.imgThali,       "Thali",        "🍱", "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300"},
            {R.id.cardSandwich,    R.id.imgSandwich,    "Sandwich",     "🥪", "https://images.unsplash.com/photo-1553909489-cd47e0907980?w=300"},
            {R.id.cardPasta,       R.id.imgPasta,       "Pasta",        "🍝", "https://images.unsplash.com/photo-1555949258-eb67b1ef0ceb?w=300"},
            {R.id.cardRolls,       R.id.imgRolls,       "Rolls",        "🌯", "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=300"},
            {R.id.cardMomos,       R.id.imgMomos,       "Momos",        "🥟", "https://images.unsplash.com/photo-1534482421-64566f976cfa?w=300"},
            {R.id.cardShake,       R.id.imgShake,       "Shake",        "🥤", "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=300"},
            {R.id.cardStreetFood,  R.id.imgStreetFood,  "Street Food",  "🌮", "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=300"},
            {R.id.cardDessert,     R.id.imgDessert,     "Dessert",      "🍦", "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=300"},
            {R.id.cardDrinks,      R.id.imgDrinks,      "Drinks",       "☕", "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=300"},
            {R.id.cardHealthy,     R.id.imgHealthy,     "Healthy",      "🥗", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=300"},
            {R.id.cardBakery,      R.id.imgBakery,      "Bakery",       "🥐", "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=300"},
            {R.id.cardSeafood,     R.id.imgSeafood,     "Seafood",      "🦞", "https://images.unsplash.com/photo-1615141982883-c7ad0e69fd62?w=300"},
            {R.id.cardBBQ,         R.id.imgBBQ,         "BBQ",          "🔥", "https://images.unsplash.com/photo-1558030006-450675393462?w=300"},
    };

    // IMPORTANT: tags must match CATEGORY labels exactly (case-sensitive)
    // Format: id, name, "Tag1|Tag2|Tag3", rating, deliveryFee, prepTime, open, coverUrl, foodNames[], foodUrls[]
    private static final Object[][] RESTAURANT_DATA = {
            {"r1","Spice Garden",  "North Indian|Biryani|Mughlai",  4.5f,30,25,true,
                    "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=400",
                    new String[]{"Butter Chicken","Paneer Tikka","Dal Makhani"},
                    new String[]{"https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=200",
                            "https://images.unsplash.com/photo-1567188040759-fb8a883dc6d8?w=200",
                            "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=200"}},

            {"r2","Pizza Palace",  "Pizza|Pasta|Italian",           4.3f, 0,20,true,
                    "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400",
                    new String[]{"Margherita Pizza","Pasta Arrabbiata","Garlic Bread"},
                    new String[]{"https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200",
                            "https://images.unsplash.com/photo-1555949258-eb67b1ef0ceb?w=200",
                            "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=200"}},

            {"r3","Burger Barn",   "Burger|Sandwich|Rolls",         4.1f,20,15,true,
                    "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400",
                    new String[]{"Classic Burger","Chicken Sandwich","Veg Roll"},
                    new String[]{"https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200",
                            "https://images.unsplash.com/photo-1553909489-cd47e0907980?w=200",
                            "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=200"}},

            {"r4","Dragon Wok",    "Chinese|Noodles|Momos",         4.4f,25,30,true,
                    "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400",
                    new String[]{"Hakka Noodles","Manchurian","Veg Momos"},
                    new String[]{"https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=200",
                            "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=200",
                            "https://images.unsplash.com/photo-1534482421-64566f976cfa?w=200"}},

            {"r5","South Tadka",   "South Indian|Thali",            4.6f, 0,20,true,
                    "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=400",
                    new String[]{"Masala Dosa","Idli Sambhar","Veg Thali"},
                    new String[]{"https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=200",
                            "https://images.unsplash.com/photo-1630383249896-424e482df921?w=200",
                            "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=200"}},

            {"r6","Sweet Tooth",   "Dessert|Bakery|Shake",          4.2f,15,25,true,
                    "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=400",
                    new String[]{"Gulab Jamun","Croissant","Mango Shake"},
                    new String[]{"https://images.unsplash.com/photo-1551024601-bec78aea704b?w=200",
                            "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=200",
                            "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=200"}},

            {"r7","Street Bites",  "Street Food|Rolls|Momos",       4.0f,10,20,true,
                    "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=400",
                    new String[]{"Pav Bhaji","Kathi Roll","Veg Momos"},
                    new String[]{"https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=200",
                            "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=200",
                            "https://images.unsplash.com/photo-1534482421-64566f976cfa?w=200"}},

            {"r8","Ocean Platter", "Seafood|BBQ",                   4.3f,35,40,true,
                    "https://images.unsplash.com/photo-1615141982883-c7ad0e69fd62?w=400",
                    new String[]{"Grilled Fish","Prawn BBQ","Seafood Platter"},
                    new String[]{"https://images.unsplash.com/photo-1615141982883-c7ad0e69fd62?w=200",
                            "https://images.unsplash.com/photo-1558030006-450675393462?w=200",
                            "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=200"}},

            {"r9","Green Bowl",    "Healthy|Drinks|Shake",          4.5f, 0,15,true,
                    "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400",
                    new String[]{"Buddha Bowl","Green Smoothie","Fruit Salad"},
                    new String[]{"https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=200",
                            "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=200",
                            "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=200"}},
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etSearch    = v.findViewById(R.id.etSearch);
        rvResults   = v.findViewById(R.id.rvSearchResults);
        chipGroup   = v.findViewById(R.id.chipGroupCategories);
        layoutEmpty = v.findViewById(R.id.layoutEmpty);

        adapter = new RestaurantAdapter(requireContext(), filtered, r -> {
            Intent i = new Intent(requireContext(), RestaurantDetailActivity.class);
            i.putExtra("restaurantId",   r.getId());
            i.putExtra("restaurantName", r.getName());
            i.putExtra("cuisine",        r.getCuisine());
            i.putExtra("imageUrl",       r.getImage());
            i.putExtra("rating",         r.getRating());
            i.putExtra("prepTime",       r.getPrepTime());
            i.putExtra("deliveryFee",    (double) r.getDeliveryFee());
            startActivity(i);
        }, RestaurantAdapter.TYPE_FULL);

        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResults.setAdapter(adapter);

        loadCategoryImages(v);
        setupCategoryCardClicks(v);
        loadLocalRestaurants();
        setupChips();
        setupSearch();
    }

    private void loadCategoryImages(View v) {
        for (Object[] cat : CATEGORIES) {
            ImageView img = v.findViewById((int) cat[1]);
            if (img != null)
                Glide.with(this).load((String) cat[4]).centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade()).into(img);
        }
    }

    private void setupCategoryCardClicks(View v) {
        for (Object[] cat : CATEGORIES) {
            View card = v.findViewById((int) cat[0]);
            if (card == null) continue;
            final String label = (String) cat[2];
            card.setOnClickListener(x -> selectCategory(label));
        }
    }

    private void selectCategory(String label) {
        activeCategory = label;
        // sync chip UI
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setChecked(label.equals(chip.getTag()));
        }
        applyFilter(etSearch.getText().toString().trim());
    }

    private void loadLocalRestaurants() {
        allRestaurants.clear();
        for (Object[] d : RESTAURANT_DATA) {
            // Store tags with | separator — we split on | when filtering
            Restaurant r = new Restaurant(
                    (String)  d[0], (String) d[1], (String) d[2],
                    (float)   d[3], (int)    d[4], (int)    d[5],
                    (boolean) d[6], true,           (String) d[7]);
            r.setFoodNames(Arrays.asList((String[]) d[8]));
            r.setFoodPhotos(Arrays.asList((String[]) d[9]));
            allRestaurants.add(r);
        }
        List<String> names = new ArrayList<>();
        for (Restaurant r : allRestaurants) names.add(r.getName());
        etSearch.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, names));
        applyFilter("");
    }

    private void setupChips() {
        chipGroup.removeAllViews();
        for (Object[] cat : CATEGORIES) {
            String label = (String) cat[2];
            String emoji = (String) cat[3];
            Chip chip = new Chip(requireContext());
            chip.setText(emoji + " " + label);
            chip.setTag(label);  // ← label stored as tag for matching
            chip.setCheckable(true);
            chip.setChecked(label.equals("All"));
            chip.setChipBackgroundColorResource(R.color.chip_selector);
            chip.setTextColor(getResources().getColorStateList(
                    R.color.chip_text_selector, requireContext().getTheme()));
            chip.setOnClickListener(x -> selectCategory(label));
            chipGroup.addView(chip);
        }
    }

    // ── THE FIX: split on | and match each tag exactly ───────────────────────
    private void applyFilter(String query) {
        filtered.clear();
        for (Restaurant r : allRestaurants) {
            // Category match
            boolean matchCat = activeCategory.equals("All");
            if (!matchCat && r.getCuisine() != null) {
                // cuisine stored as "Pizza|Pasta|Italian"
                String[] tags = r.getCuisine().split("\\|");
                for (String tag : tags) {
                    if (tag.trim().equalsIgnoreCase(activeCategory.trim())) {
                        matchCat = true;
                        break;
                    }
                }
            }

            // Search query match
            boolean matchQuery = query.isEmpty();
            if (!matchQuery) {
                String q = query.toLowerCase();
                matchQuery = r.getName().toLowerCase().contains(q)
                        || (r.getCuisine() != null
                        && r.getCuisine().toLowerCase().contains(q));
            }

            if (matchCat && matchQuery) filtered.add(r);
        }

        adapter.updateList(new ArrayList<>(filtered));
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvResults.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                applyFilter(s.toString().trim());
            }
        });
    }
}