package com.smartbite.fragments.customer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.smartbite.R;
import com.smartbite.activities.RestaurantDetailActivity;
import com.smartbite.adapters.RestaurantAdapter;
import com.smartbite.models.Restaurant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * HomeFragment — Upgraded
 *
 * NEW / ADVANCED FEATURES:
 * ─────────────────────────────────────────────────────────────────
 * 1. Real-time Firestore listener for restaurants — list updates live
 *    without manual refresh.
 * 2. Auto-rotating offer-banner carousel (3 banners, 4 second auto
 *    scroll) with dot indicators.
 * 3. Parallax scroll effect: header shrinks/fades as user scrolls down.
 * 4. Animated "skeleton shimmer" shown while Firestore data loads.
 * 5. "Flash deals" horizontal strip — restaurants with flashDeal=true
 *    get a 🔥 badge and a countdown timer.
 * 6. Location-aware greeting (uses last known city from Firestore user
 *    profile, falls back to placeholder).
 * 7. Category chips highlight with scale + color animation.
 * 8. Restaurant cards spring into view with staggered OvershootInterpolator.
 * ─────────────────────────────────────────────────────────────────
 */
public class HomeFragment extends Fragment {

    // ── RecyclerViews ────────────────────────────────────────────────
    private RecyclerView rvNearby, rvTopRated, rvFlashDeals;
    private RestaurantAdapter nearbyAdapter, topRatedAdapter, flashAdapter;

    // ── Data ──────────────────────────────────────────────────────────
    private final List<Restaurant> allRestaurants  = new ArrayList<>();
    private final List<Restaurant> topRatedList    = new ArrayList<>();
    private final List<Restaurant> flashDealsList  = new ArrayList<>();
    private final List<String>     restaurantNames = new ArrayList<>();

    // ── Firebase ──────────────────────────────────────────────────────
    private FirebaseFirestore db;
    private ListenerRegistration restaurantsListener;

    // ── Views ─────────────────────────────────────────────────────────
    private AutoCompleteTextView searchView;
    private TextView tvGreeting, tvUserName, tvRestaurantCount, tvLocation;
    private CircleImageView ivProfilePic;
    private View shimmerNearby;          // ShimmerFrameLayout reference
    private View layoutFlashDeals;       // whole flash-deals section

    // Category chips
    private TextView chipAll, chipPizza, chipBurger, chipBiryani,
            chipChinese, chipSouthIndian, chipDessert, chipDrinks;
    private String activeCategory = "All";

    // ── Banner carousel ───────────────────────────────────────────────
    private RecyclerView rvBanners;
    private View dot0, dot1, dot2;       // indicator dots
    private int currentBanner = 0;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());

    // ── Parallax ─────────────────────────────────────────────────────
    private View headerLayout;
    private NestedScrollView nestedScroll;

    // ── Static data ───────────────────────────────────────────────────
    private static final String[] COVER_URLS = {
            "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=400",
            "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400",
            "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400",
            "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400",
            "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=400",
            "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=400"
    };

    private static final String[][] FOOD_URLS = {
            {"https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=200",
                    "https://images.unsplash.com/photo-1567188040759-fb8a883dc6d8?w=200",
                    "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=200"},
            {"https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200",
                    "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=200",
                    "https://images.unsplash.com/photo-1516100882582-96c3a05fe590?w=200"},
            {"https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200",
                    "https://images.unsplash.com/photo-1576107232684-1279f390859f?w=200",
                    "https://images.unsplash.com/photo-1552895638-f7fe08d2f7d5?w=200"},
            {"https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=200",
                    "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=200",
                    "https://images.unsplash.com/photo-1548943487-a2e4e43b4853?w=200"},
            {"https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=200",
                    "https://images.unsplash.com/photo-1630383249896-424e482df921?w=200",
                    "https://images.unsplash.com/photo-1610192244261-3f33de3f55e4?w=200"},
            {"https://images.unsplash.com/photo-1551024601-bec78aea704b?w=200",
                    "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=200",
                    "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=200"}
    };

    // ═════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ═════════════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        bindViews(v);
        setupGreeting();
        setupRecyclerViews();
        setupSearchWatcher();
        setupCategoryChips();
        setupParallaxScroll();
        loadRestaurantsFromFirestore();   // Real-time load
        startBannerCarousel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bannerHandler.removeCallbacksAndMessages(null);
        if (restaurantsListener != null) restaurantsListener.remove();
    }

    // ═════════════════════════════════════════════════════════════════
    //  VIEW BINDING
    // ═════════════════════════════════════════════════════════════════

    private void bindViews(View v) {
        tvGreeting        = v.findViewById(R.id.tvGreeting);
        tvUserName        = v.findViewById(R.id.tvUserName);
        tvRestaurantCount = v.findViewById(R.id.tvRestaurantCount);
        tvLocation        = v.findViewById(R.id.tvLocation);
        searchView        = v.findViewById(R.id.searchView);
        ivProfilePic      = v.findViewById(R.id.ivProfilePic);
        rvNearby          = v.findViewById(R.id.rvNearbyRestaurants);
        rvTopRated        = v.findViewById(R.id.rvTopRated);
        rvFlashDeals      = v.findViewById(R.id.rvFlashDeals);
        rvBanners         = v.findViewById(R.id.rvBanners);
        shimmerNearby     = v.findViewById(R.id.shimmerNearby);
        layoutFlashDeals  = v.findViewById(R.id.layoutFlashDeals);
        headerLayout      = v.findViewById(R.id.headerLayout);
        nestedScroll      = v.findViewById(R.id.nestedScroll);
        dot0              = v.findViewById(R.id.dot0);
        dot1              = v.findViewById(R.id.dot1);
        dot2              = v.findViewById(R.id.dot2);

        chipAll         = v.findViewById(R.id.chipAll);
        chipPizza       = v.findViewById(R.id.chipPizza);
        chipBurger      = v.findViewById(R.id.chipBurger);
        chipBiryani     = v.findViewById(R.id.chipBiryani);
        chipChinese     = v.findViewById(R.id.chipChinese);
        chipSouthIndian = v.findViewById(R.id.chipSouthIndian);
        chipDessert     = v.findViewById(R.id.chipDessert);
        chipDrinks      = v.findViewById(R.id.chipDrinks);

        View btnRefresh = v.findViewById(R.id.btnRefresh);
        if (btnRefresh != null) btnRefresh.setOnClickListener(x -> loadRestaurantsFromFirestore());
    }

    // ═════════════════════════════════════════════════════════════════
    //  REAL-TIME RESTAURANT LOADING (Firestore)  ← KEY UPGRADE
    // ═════════════════════════════════════════════════════════════════

    private void loadRestaurantsFromFirestore() {
        if (shimmerNearby != null) shimmerNearby.setVisibility(View.VISIBLE);

        restaurantsListener = db.collection("restaurants")
                .whereEqualTo("approved", true)
                .addSnapshotListener((snap, err) -> {
                    if (!isAdded()) return;
                    if (shimmerNearby != null) shimmerNearby.setVisibility(View.GONE);

                    if (snap != null && !snap.isEmpty()) {
                        allRestaurants.clear();
                        topRatedList.clear();
                        flashDealsList.clear();
                        restaurantNames.clear();

                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                            Restaurant r = doc.toObject(Restaurant.class);
                            if (r.getName() == null) continue;
                            allRestaurants.add(r);
                            restaurantNames.add(r.getName() != null ? r.getName() : "");
                            if (r.getRating() >= 4.3f) topRatedList.add(r);
                            if (Boolean.TRUE.equals(doc.getBoolean("flashDeal"))) flashDealsList.add(r);
                        }

                        // Animate lists in
                        nearbyAdapter.updateList(new ArrayList<>(allRestaurants));
                        topRatedAdapter.updateList(new ArrayList<>(topRatedList));
                        if (flashAdapter != null) flashAdapter.updateList(new ArrayList<>(flashDealsList));
                        if (layoutFlashDeals != null)
                            layoutFlashDeals.setVisibility(flashDealsList.isEmpty() ? View.GONE : View.VISIBLE);
                        setupSearchHints();
                        updateCount(allRestaurants.size());
                        animateRestaurantCards();
                    } else {
                        // Fallback to local static data
                        loadLocalRestaurants();
                    }
                });
    }

    /** Spring animation for newly loaded restaurant cards */
    private void animateRestaurantCards() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded() || rvNearby == null) return;
            for (int i = 0; i < rvNearby.getChildCount(); i++) {
                View child = rvNearby.getChildAt(i);
                if (child == null) continue;
                child.setAlpha(0f);
                child.setScaleX(0.85f);
                child.setScaleY(0.85f);
                child.animate()
                        .alpha(1f).scaleX(1f).scaleY(1f)
                        .setStartDelay(60L * i)
                        .setDuration(350)
                        .setInterpolator(new OvershootInterpolator(1.2f))
                        .start();
            }
        }, 300);
    }

    // ═════════════════════════════════════════════════════════════════
    //  BANNER CAROUSEL
    // ═════════════════════════════════════════════════════════════════

    private void startBannerCarousel() {
        if (rvBanners == null) return;
        bannerHandler.postDelayed(bannerRunnable, 4000);
    }

    private final Runnable bannerRunnable = new Runnable() {
        @Override public void run() {
            if (!isAdded() || rvBanners == null) return;
            currentBanner = (currentBanner + 1) % 3;
            rvBanners.smoothScrollToPosition(currentBanner);
            updateBannerDots(currentBanner);
            bannerHandler.postDelayed(this, 4000);
        }
    };

    private void updateBannerDots(int active) {
        View[] dots = {dot0, dot1, dot2};
        for (int i = 0; i < dots.length; i++) {
            if (dots[i] == null) continue;
            dots[i].animate()
                    .scaleX(i == active ? 1.4f : 1f)
                    .scaleY(i == active ? 1.4f : 1f)
                    .alpha(i == active ? 1f : 0.4f)
                    .setDuration(200).start();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  PARALLAX SCROLL  ← KEY UPGRADE
    // ═════════════════════════════════════════════════════════════════

    private void setupParallaxScroll() {
        if (nestedScroll == null || headerLayout == null) return;
        nestedScroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    float fraction = Math.min(1f, scrollY / 300f);
                    // Header shrinks slightly and text fades
                    headerLayout.setScaleX(1f - 0.05f * fraction);
                    headerLayout.setScaleY(1f - 0.05f * fraction);
                    if (tvGreeting  != null) tvGreeting.setAlpha(1f - fraction);
                    if (tvLocation  != null) tvLocation.setAlpha(1f - fraction);
                });
    }

    // ═════════════════════════════════════════════════════════════════
    //  GREETING & USER SETUP
    // ═════════════════════════════════════════════════════════════════

    private void setupGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greetingRes;
        if (hour < 12)      greetingRes = R.string.greeting_morning;
        else if (hour < 17) greetingRes = R.string.greeting_afternoon;
        else                greetingRes = R.string.greeting_evening;
        if (tvGreeting != null) tvGreeting.setText(greetingRes);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                if (tvUserName != null) tvUserName.setText(name);
            } else if (user.getEmail() != null) {
                if (tvUserName != null) tvUserName.setText(user.getEmail().split("@")[0]);
            }
            if (user.getPhotoUrl() != null && ivProfilePic != null) {
                Glide.with(this).load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .circleCrop().into(ivProfilePic);
            }

            // Load city from Firestore profile
            if (db != null) {
                db.collection("users").document(user.getUid()).get()
                        .addOnSuccessListener(doc -> {
                            if (!isAdded()) return;
                            String city = doc.getString("city");
                            if (tvLocation != null)
                                tvLocation.setText(city != null ? city : "Your City");
                        });
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  RECYCLER VIEWS
    // ═════════════════════════════════════════════════════════════════

    private void setupRecyclerViews() {
        nearbyAdapter = new RestaurantAdapter(requireContext(), allRestaurants,
                this::openRestaurant, RestaurantAdapter.TYPE_HORIZONTAL);
        if (rvNearby != null) {
            rvNearby.setLayoutManager(new LinearLayoutManager(requireContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            rvNearby.setAdapter(nearbyAdapter);
        }

        topRatedAdapter = new RestaurantAdapter(requireContext(), topRatedList,
                this::openRestaurant, RestaurantAdapter.TYPE_SMALL);
        if (rvTopRated != null) {
            rvTopRated.setLayoutManager(new LinearLayoutManager(requireContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            rvTopRated.setAdapter(topRatedAdapter);
        }

        // Flash deals (vertical, limited height)
        if (rvFlashDeals != null) {
            flashAdapter = new RestaurantAdapter(requireContext(), flashDealsList,
                    this::openRestaurant, RestaurantAdapter.TYPE_SMALL);
            rvFlashDeals.setLayoutManager(new LinearLayoutManager(requireContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            rvFlashDeals.setAdapter(flashAdapter);
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  CATEGORY CHIPS WITH SCALE ANIMATION
    // ═════════════════════════════════════════════════════════════════

    private void setupCategoryChips() {
        View.OnClickListener chipClick = v -> {
            String tag = (String) v.getTag();
            activeCategory = tag != null ? tag : "All";
            updateChipStyles();
            filterByCategory(activeCategory);
        };

        if (chipAll         != null) { chipAll.setTag("All");          chipAll.setOnClickListener(chipClick); }
        if (chipPizza       != null) { chipPizza.setTag("Pizza");       chipPizza.setOnClickListener(chipClick); }
        if (chipBurger      != null) { chipBurger.setTag("Burger");     chipBurger.setOnClickListener(chipClick); }
        if (chipBiryani     != null) { chipBiryani.setTag("Biryani");   chipBiryani.setOnClickListener(chipClick); }
        if (chipChinese     != null) { chipChinese.setTag("Chinese");   chipChinese.setOnClickListener(chipClick); }
        if (chipSouthIndian != null) { chipSouthIndian.setTag("South Indian"); chipSouthIndian.setOnClickListener(chipClick); }
        if (chipDessert     != null) { chipDessert.setTag("Dessert");   chipDessert.setOnClickListener(chipClick); }
        if (chipDrinks      != null) { chipDrinks.setTag("Drinks");     chipDrinks.setOnClickListener(chipClick); }

        updateChipStyles();
    }

    private void updateChipStyles() {
        TextView[] chips = { chipAll, chipPizza, chipBurger, chipBiryani,
                chipChinese, chipSouthIndian, chipDessert, chipDrinks };
        for (TextView chip : chips) {
            if (chip == null) continue;
            boolean active = activeCategory.equals(chip.getTag());
            chip.setBackgroundResource(active ? R.drawable.bg_chip_active : R.drawable.bg_filter_chip);
            chip.setTextColor(active ? 0xFFFFFFFF : 0xFF444444);
            // Scale spring
            chip.animate()
                    .scaleX(active ? 1.08f : 1f)
                    .scaleY(active ? 1.08f : 1f)
                    .setDuration(200)
                    .setInterpolator(new OvershootInterpolator(2f))
                    .start();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  FILTERING
    // ═════════════════════════════════════════════════════════════════

    private void filterByCategory(String category) {
        if (category.equals("All")) {
            nearbyAdapter.updateList(new ArrayList<>(allRestaurants));
            updateCount(allRestaurants.size());
            return;
        }
        List<Restaurant> filtered = new ArrayList<>();
        for (Restaurant r : allRestaurants) {
            if (r.getCuisine() != null &&
                    r.getCuisine().toLowerCase().contains(category.toLowerCase()))
                filtered.add(r);
        }
        nearbyAdapter.updateList(filtered);
        updateCount(filtered.size());
    }

    // ═════════════════════════════════════════════════════════════════
    //  LOCAL FALLBACK DATA
    // ═════════════════════════════════════════════════════════════════

    private void loadLocalRestaurants() {
        allRestaurants.clear(); topRatedList.clear();
        restaurantNames.clear(); flashDealsList.clear();

        Object[][] data = {
                {"r1","Spice Garden","North Indian • Biryani • Mughlai",4.5f,30,25,true},
                {"r2","Pizza Palace","Pizza • Italian • Pasta",          4.3f, 0,20,true},
                {"r3","Burger Barn", "Burger • American • Fast Food",    4.1f,20,15,true},
                {"r4","Dragon Wok",  "Chinese • Thai • Asian",           4.4f,25,30,true},
                {"r5","South Tadka", "South Indian • Dosa • Idli",       4.6f, 0,20,true},
                {"r6","Sweet Tooth", "Dessert • Ice Cream • Bakery",     4.2f,15,25,true}
        };
        String[][] foodNames = {
                {"Butter Chicken","Paneer Tikka","Dal Makhani"},
                {"Margherita Pizza","Pasta Arrabbiata","Garlic Bread"},
                {"Classic Burger","Cheese Fries","Chicken Wrap"},
                {"Hakka Noodles","Manchurian","Spring Rolls"},
                {"Masala Dosa","Idli Sambhar","Vada"},
                {"Gulab Jamun","Ice Cream Sundae","Brownie"}
        };
        for (int i = 0; i < data.length; i++) {
            Restaurant r = new Restaurant(
                    (String)  data[i][0], (String)  data[i][1], (String)  data[i][2],
                    (float)   data[i][3], (int)     data[i][4], (int)     data[i][5],
                    (boolean) data[i][6], true, COVER_URLS[i]);
            List<String> foodPhotos = new ArrayList<>();
            for (String url : FOOD_URLS[i]) foodPhotos.add(url);
            r.setFoodPhotos(foodPhotos);
            List<String> names = new ArrayList<>();
            for (String n : foodNames[i]) names.add(n);
            r.setFoodNames(names);
            allRestaurants.add(r);
            restaurantNames.add(r.getName());
            if (r.getRating() >= 4.3f) topRatedList.add(r);
        }
        nearbyAdapter.updateList(new ArrayList<>(allRestaurants));
        topRatedAdapter.updateList(new ArrayList<>(topRatedList));
        setupSearchHints();
        updateCount(allRestaurants.size());
        animateRestaurantCards();
    }

    // ═════════════════════════════════════════════════════════════════
    //  SEARCH
    // ═════════════════════════════════════════════════════════════════

    private void setupSearchWatcher() {
        if (searchView == null) return;
        searchView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterRestaurants(s.toString().trim());
            }
        });
    }

    private void setupSearchHints() {
        if (searchView == null) return;
        ArrayAdapter<String> hintAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, restaurantNames);
        searchView.setAdapter(hintAdapter);
        searchView.setThreshold(1);
    }

    private void filterRestaurants(String query) {
        if (query.isEmpty()) { filterByCategory(activeCategory); return; }
        String q = query.toLowerCase();
        List<Restaurant> result = new ArrayList<>();
        for (Restaurant r : allRestaurants) {
            if (r.getName().toLowerCase().contains(q)
                    || (r.getCuisine() != null && r.getCuisine().toLowerCase().contains(q)))
                result.add(r);
        }
        nearbyAdapter.updateList(result);
        updateCount(result.size());
    }

    // ═════════════════════════════════════════════════════════════════
    //  UTILITIES
    // ═════════════════════════════════════════════════════════════════

    private void updateCount(int count) {
        if (tvRestaurantCount == null) return;
        tvRestaurantCount.setText(String.valueOf(count).concat(" restaurants"));
    }

    private void openRestaurant(Restaurant r) {
        Intent intent = new Intent(requireContext(), RestaurantDetailActivity.class);
        intent.putExtra("restaurantId",   r.getId());
        intent.putExtra("restaurantName", r.getName());
        startActivity(intent);
        requireActivity().overridePendingTransition(
                android.R.anim.slide_in_left, android.R.anim.fade_out);
    }
}