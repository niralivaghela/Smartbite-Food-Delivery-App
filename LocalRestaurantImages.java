package com.smartbite.models;

import com.smartbite.R;

import java.util.Arrays;
import java.util.List;

/**
 * Maps local restaurant cover images (r1–r6) and food preview images (pizza1–pizza9)
 * to restaurants fetched from Firestore.
 * Call assignLocalImages(list) after Firestore fetch to assign fallback images
 * when a restaurant's remote image URL is missing or empty.
 */
public class LocalRestaurantImages {

    // ── Cover images → your restaurant photos r1–r6 ───────────────────────────
    private static final int[] COVER_IMAGES = {
            R.drawable.r1,
            R.drawable.r2,
            R.drawable.r3,
            R.drawable.r4,
            R.drawable.r5,
            R.drawable.r6
    };

    // ── Food preview images (3 per restaurant card, pizza1–pizza9) ───────────
    private static final int[][] FOOD_PREVIEW_IMAGES = {
            { R.drawable.pizza1, R.drawable.pizza2, R.drawable.pizza3 },
            { R.drawable.pizza4, R.drawable.pizza5, R.drawable.pizza6 },
            { R.drawable.pizza7, R.drawable.pizza8, R.drawable.pizza9 },
            { R.drawable.pizza2, R.drawable.pizza5, R.drawable.pizza8 },
            { R.drawable.pizza3, R.drawable.pizza6, R.drawable.pizza9 },
            { R.drawable.pizza1, R.drawable.pizza4, R.drawable.pizza7 }
    };

    /**
     * Assigns local drawable images to each restaurant.
     * - Always sets imageResId as a fallback.
     * - Only sets foodResIds if the restaurant has no remote foodPhotos.
     *
     * @param list list of restaurants from Firestore
     */
    public static void assignLocalImages(List<Restaurant> list) {
        for (int i = 0; i < list.size(); i++) {
            Restaurant r = list.get(i);
            int slot = i % COVER_IMAGES.length;

            // Always assign local cover image as fallback
            r.setImageResId(COVER_IMAGES[slot]);

            // Assign local food preview images only if Firestore has none
            if (r.getFoodPhotos() == null || r.getFoodPhotos().isEmpty()) {
                int[] foodSlot = FOOD_PREVIEW_IMAGES[slot];
                List<Integer> foodIds = Arrays.asList(
                        foodSlot[0], foodSlot[1], foodSlot[2]
                );
                r.setFoodResIds(foodIds);
            }
        }
    }

    /**
     * Returns the cover drawable res ID for a given index (useful for single items).
     */
    public static int getCoverResId(int index) {
        return COVER_IMAGES[index % COVER_IMAGES.length];
    }
}