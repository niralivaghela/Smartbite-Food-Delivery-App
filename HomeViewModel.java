package com.smartbite.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.smartbite.models.LocalRestaurantImages;   // ← NEW
import com.smartbite.models.Restaurant;
import com.smartbite.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();
    private final MutableLiveData<List<Restaurant>> topRated    = new MutableLiveData<>();
    private final MutableLiveData<String>           error       = new MutableLiveData<>();
    private final MutableLiveData<Boolean>          loading     = new MutableLiveData<>();

    public LiveData<List<Restaurant>> getRestaurants() { return restaurants; }
    public LiveData<List<Restaurant>> getTopRated()    { return topRated; }
    public LiveData<String>           getError()       { return error; }
    public LiveData<Boolean>          getLoading()     { return loading; }

    // ── Load all restaurants ──────────────────────────────────────────────────

    public void loadRestaurants() {
        loading.setValue(true);

        db.collection(Constants.COLLECTION_RESTAURANTS)
                .whereEqualTo("approved", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Restaurant> list = snapshot.toObjects(Restaurant.class);

                    if (list != null && !list.isEmpty()) {
                        // ✅ Assign local pizza images as fallback
                        LocalRestaurantImages.assignLocalImages(list);
                        restaurants.setValue(list);
                        loading.setValue(false);
                    } else {
                        loadAllRestaurantsFallback();
                    }
                })
                .addOnFailureListener(e -> loadAllRestaurantsFallback());
    }

    private void loadAllRestaurantsFallback() {
        db.collection(Constants.COLLECTION_RESTAURANTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Restaurant> list = snapshot.toObjects(Restaurant.class);
                    if (list == null) list = new ArrayList<>();

                    // ✅ Assign local pizza images as fallback
                    LocalRestaurantImages.assignLocalImages(list);
                    restaurants.setValue(list);
                    loading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    error.setValue(e.getMessage());
                    loading.setValue(false);
                    restaurants.setValue(new ArrayList<>());
                });
    }

    // ── Load top rated ────────────────────────────────────────────────────────

    public void loadTopRated() {
        db.collection(Constants.COLLECTION_RESTAURANTS)
                .whereEqualTo("approved", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Restaurant> list = snapshot.toObjects(Restaurant.class);

                    if (list != null && !list.isEmpty()) {
                        // ✅ Assign local pizza images as fallback
                        LocalRestaurantImages.assignLocalImages(list);
                        topRated.setValue(list);
                    } else {
                        // Fallback without approved filter
                        db.collection(Constants.COLLECTION_RESTAURANTS)
                                .orderBy("rating", Query.Direction.DESCENDING)
                                .limit(10)
                                .get()
                                .addOnSuccessListener(s -> {
                                    List<Restaurant> fallback = s.toObjects(Restaurant.class);
                                    if (fallback != null) {
                                        LocalRestaurantImages.assignLocalImages(fallback);
                                    }
                                    topRated.setValue(fallback != null ? fallback : new ArrayList<>());
                                });
                    }
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    // ── Search ────────────────────────────────────────────────────────────────

    public void searchRestaurants(String query) {
        loading.setValue(true);
        db.collection(Constants.COLLECTION_RESTAURANTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Restaurant> all = snapshot.toObjects(Restaurant.class);
                    List<Restaurant> filtered = new ArrayList<>();

                    for (Restaurant r : all) {
                        if ((r.getName() != null
                                && r.getName().toLowerCase().contains(query.toLowerCase()))
                                || (r.getCuisine() != null
                                && r.getCuisine().toLowerCase().contains(query.toLowerCase()))) {
                            filtered.add(r);
                        }
                    }

                    // ✅ Assign local pizza images as fallback
                    LocalRestaurantImages.assignLocalImages(filtered);
                    restaurants.setValue(filtered);
                    loading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    error.setValue(e.getMessage());
                    loading.setValue(false);
                });
    }
}