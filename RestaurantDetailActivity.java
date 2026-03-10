package com.smartbite.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import com.smartbite.R;
import com.smartbite.database.CartEntity;
import com.smartbite.databinding.ActivityRestaurantDetailBinding;
import com.smartbite.viewmodels.CartViewModel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class RestaurantDetailActivity extends AppCompatActivity {

    private ActivityRestaurantDetailBinding binding;
    private CartViewModel cartViewModel;

    // item name → {price, quantity}
    private final Map<String, int[]> cart = new LinkedHashMap<>();

    // SpellCheckingInspection not needed — no misspelled words remain
    private static final HashMap<String, Object[][]> RESTAURANT_MENUS = new HashMap<>();

    static {
        RESTAURANT_MENUS.put("r1", new Object[][]{
                {"Butter Chicken",   249, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=300"},
                {"Paneer Tikka",     199, "https://images.unsplash.com/photo-1567188040759-fb8a883dc6d8?w=300"},
                {"Dal Makhani",      159, "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300"},
        });
        RESTAURANT_MENUS.put("r2", new Object[][]{
                {"Margherita Pizza", 299, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=300"},
                {"Pasta Arrabbiata", 199, "https://images.unsplash.com/photo-1555949258-eb67b1ef0ceb?w=300"},
                {"Garlic Bread",      99, "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=300"},
        });
        RESTAURANT_MENUS.put("r3", new Object[][]{
                {"Classic Burger",   179, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300"},
                {"Chicken Sandwich", 149, "https://images.unsplash.com/photo-1553909489-cd47e0907980?w=300"},
                {"Veg Roll",         119, "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=300"},
        });
        RESTAURANT_MENUS.put("r4", new Object[][]{
                {"Hakka Noodles",    179, "https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=300"},
                {"Manchurian",       149, "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=300"},
                {"Veg Dumplings",    129, "https://images.unsplash.com/photo-1534482421-64566f976cfa?w=300"},
        });
        RESTAURANT_MENUS.put("r5", new Object[][]{
                {"Masala Dosa",      129, "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=300"},
                {"Idli Sambhar",      99, "https://images.unsplash.com/photo-1630383249896-424e482df921?w=300"},
                {"Veg Thali",        199, "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300"},
        });
        RESTAURANT_MENUS.put("r6", new Object[][]{
                {"Gulab Jamun",       89, "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=300"},
                {"Mango Shake",      129, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=300"},
                {"Croissant",        119, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=300"},
        });
        RESTAURANT_MENUS.put("r7", new Object[][]{
                {"Pav Bhaji",        129, "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=300"},
                {"Kathi Roll",       119, "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=300"},
                {"Veg Bao",          109, "https://images.unsplash.com/photo-1534482421-64566f976cfa?w=300"},
        });
        RESTAURANT_MENUS.put("r8", new Object[][]{
                {"Grilled Fish",     349, "https://images.unsplash.com/photo-1615141982883-c7ad0e69fd62?w=300"},
                {"Prawn BBQ",        399, "https://images.unsplash.com/photo-1558030006-450675393462?w=300"},
                {"Seafood Platter",  499, "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=300"},
        });
        RESTAURANT_MENUS.put("r9", new Object[][]{
                {"Buddha Bowl",      249, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=300"},
                {"Green Smoothie",   149, "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=300"},
                {"Fruit Salad",      129, "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=300"},
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        String restaurantId   = getIntent().getStringExtra("restaurantId");
        String restaurantName = getIntent().getStringExtra("restaurantName");
        String cuisine        = getIntent().getStringExtra("cuisine");
        String imageUrl       = getIntent().getStringExtra("imageUrl");
        float  rating         = getIntent().getFloatExtra("rating", 4.0f);
        int    prepTime       = getIntent().getIntExtra("prepTime", 30);
        double deliveryFee    = getIntent().getDoubleExtra("deliveryFee", 0.0);

        String resolvedName = restaurantName != null
                ? restaurantName : getString(R.string.label_restaurant);

        binding.tvRestaurantName.setText(resolvedName);
        binding.tvCuisine.setText(cuisine != null ? cuisine.replace(",", " • ") : "");
        binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f ★", rating));
        binding.tvPrepTime.setText(getString(R.string.label_prep_time_min, prepTime));
        binding.tvDeliveryFee.setText(deliveryFee == 0
                ? getString(R.string.label_free_delivery)
                : getString(R.string.label_delivery_fee_amount, (int) deliveryFee));
        binding.collapsingToolbar.setTitle(resolvedName);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivRestaurantBanner);
        }

        Object[][] menu = RESTAURANT_MENUS.getOrDefault(restaurantId, new Object[][]{
                {"Special Dish 1", 199, "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=300"},
                {"Special Dish 2", 149, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=300"},
                {"Special Dish 3", 129, "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300"},
        });

        loadMenuItem(binding.ivMenuItem1, binding.tvMenuName1, binding.btnAdd1,
                menu, 0, restaurantId, resolvedName);
        loadMenuItem(binding.ivMenuItem2, binding.tvMenuName2, binding.btnAdd2,
                menu, 1, restaurantId, resolvedName);
        loadMenuItem(binding.ivMenuItem3, binding.tvMenuName3, binding.btnAdd3,
                menu, 2, restaurantId, resolvedName);

        binding.btnPlaceOrder.setOnClickListener(v -> {
            if (cart.isEmpty()) {
                Toast.makeText(this, R.string.error_add_items_first, Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, CartActivity.class));
        });
    }

    private void loadMenuItem(ImageView iv, TextView tvName, MaterialButton btnAdd,
                              Object[][] menu, int index,
                              String restaurantId, String restaurantName) {
        if (menu == null || index >= menu.length) return;

        String name   = (String) menu[index][0];
        int    price  = (int)    menu[index][1];
        String imgUrl = (String) menu[index][2];

        tvName.setText(name);

        ViewGroup parent = (ViewGroup) tvName.getParent();
        if (parent != null && parent.getChildCount() >= 3) {
            View priceView = parent.getChildAt(2);
            if (priceView instanceof TextView)
                ((TextView) priceView).setText(getString(R.string.label_rupee_amount, price));
        }

        Glide.with(this).load(imgUrl).centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv);

        btnAdd.setOnClickListener(v -> {
            int[] entry = cart.get(name);
            if (entry == null) {
                entry = new int[]{price, 0};
            }
            entry[1]++;
            cart.put(name, entry);

            // ✅ FIXED: use addToCart() — the correct CartViewModel method name.
            // CartViewModel.addToCart() checks if item exists and increments qty,
            // or inserts fresh — no separate addItem() needed.
            //
            // NOTE: If CartEntity uses an all-args constructor, replace the
            // setter calls below with your constructor, e.g.:
            //   new CartEntity(itemId, name, price, 1, restaurantId, restaurantName, imgUrl)
            // Upload CartEntity.java to get the exact constructor signature fixed.
            CartEntity entity = new CartEntity(
                    restaurantId + "_" + name.replace(" ", "_"), // itemId
                    name,                                         // name
                    imgUrl,                                       // image
                    restaurantId,                                 // restaurantId
                    restaurantName,                               // restaurantName
                    price,                                        // price
                    1                                             // quantity
            );
            cartViewModel.addToCart(entity);

            btnAdd.setText(getString(R.string.label_added_qty, entry[1]));
            btnAdd.setBackgroundColor(0xFF4CAF50);
            animateAddButton(btnAdd);
            updateOrderBar();
            Toast.makeText(this,
                    getString(R.string.msg_item_added_to_cart, name),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void animateAddButton(View btn) {
        btn.animate().scaleX(1.15f).scaleY(1.15f).setDuration(100)
                .withEndAction(() -> btn.animate().scaleX(1f).scaleY(1f)
                        .setDuration(150)
                        .setInterpolator(new OvershootInterpolator())
                        .start())
                .start();
    }

    private void updateOrderBar() {
        if (cart.isEmpty()) {
            binding.bottomOrderBar.animate().translationY(200f).alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> binding.bottomOrderBar.setVisibility(View.GONE))
                    .start();
            return;
        }
        if (binding.bottomOrderBar.getVisibility() == View.GONE) {
            binding.bottomOrderBar.setVisibility(View.VISIBLE);
            binding.bottomOrderBar.setTranslationY(200f);
            binding.bottomOrderBar.setAlpha(0f);
            binding.bottomOrderBar.animate().translationY(0f).alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }

        int totalItems = 0, totalPrice = 0;
        for (int[] entry : cart.values()) {
            totalItems += entry[1];
            totalPrice += entry[0] * entry[1];
        }

        binding.tvItemCount.setText(getResources().getQuantityString(
                R.plurals.label_item_count, totalItems, totalItems));
        binding.tvTotalPrice.setText(getString(R.string.label_rupee_amount, totalPrice));
    }
}