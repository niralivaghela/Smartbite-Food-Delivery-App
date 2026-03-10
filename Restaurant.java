package com.smartbite.models;

import java.util.List;
import java.util.Objects;

public class Restaurant {

    private String       id;
    private String       restaurantId;
    private String       name;
    private String       cuisine;
    private float        rating;
    private double       deliveryFee;
    private int          prepTime;
    private boolean      open;
    private boolean      approved;
    private String       image;           // cover photo URL (Firestore)
    private List<String> foodPhotos;      // list of food image URLs (Firestore)
    private List<String> foodNames;       // matching food item names

    // ── Local drawable fallback (NOT stored in Firestore) ─────────────────────
    // Used when image URL is empty/null – set manually after fetching from Firestore
    private transient int imageResId;           // local cover drawable e.g. R.drawable.pizza1
    private transient List<Integer> foodResIds; // local food drawables e.g. R.drawable.pizza2

    // ── Constructors ─────────────────────────────────────────────────────────

    public Restaurant() {}

    public Restaurant(String id, String name, String cuisine,
                      float rating, double deliveryFee,
                      int prepTime, boolean open, boolean approved, String image) {
        this.id           = id;
        this.restaurantId = id;
        this.name         = name;
        this.cuisine      = cuisine;
        this.rating       = rating;
        this.deliveryFee  = deliveryFee;
        this.prepTime     = prepTime;
        this.open         = open;
        this.approved     = approved;
        this.image        = image;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String       getId()           { return id; }
    public String       getRestaurantId() { return restaurantId != null ? restaurantId : id; }
    public String       getName()         { return name; }
    public String       getCuisine()      { return cuisine; }
    public float        getRating()       { return rating; }
    public double       getDeliveryFee()  { return deliveryFee; }
    public int          getPrepTime()     { return prepTime; }
    public boolean      isOpen()          { return open; }
    public boolean      isApproved()      { return approved; }
    public String       getImage()        { return image; }
    public List<String> getFoodPhotos()   { return foodPhotos; }
    public List<String> getFoodNames()    { return foodNames; }

    // Local drawable getters
    public int          getImageResId()   { return imageResId; }
    public List<Integer> getFoodResIds()  { return foodResIds; }

    /** Returns true if this restaurant has a valid remote image URL */
    public boolean hasRemoteImage() {
        return image != null && !image.isEmpty();
    }

    /** Returns true if this restaurant has a valid local drawable fallback */
    public boolean hasLocalImage() {
        return imageResId != 0;
    }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setId(String id)                       { this.id = id; }
    public void setRestaurantId(String rid)            { this.restaurantId = rid; }
    public void setName(String name)                   { this.name = name; }
    public void setCuisine(String cuisine)             { this.cuisine = cuisine; }
    public void setRating(float rating)                { this.rating = rating; }
    public void setDeliveryFee(double deliveryFee)     { this.deliveryFee = deliveryFee; }
    public void setPrepTime(int prepTime)              { this.prepTime = prepTime; }
    public void setOpen(boolean open)                  { this.open = open; }
    public void setApproved(boolean approved)          { this.approved = approved; }
    public void setImage(String image)                 { this.image = image; }
    public void setFoodPhotos(List<String> p)          { this.foodPhotos = p; }
    public void setFoodNames(List<String> n)           { this.foodNames = n; }
    public void setImageResId(int imageResId)          { this.imageResId = imageResId; }
    public void setFoodResIds(List<Integer> foodResIds){ this.foodResIds = foodResIds; }

    // ── DiffUtil ─────────────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;
        Restaurant that = (Restaurant) o;
        return Float.compare(that.rating, rating) == 0
                && Double.compare(that.deliveryFee, deliveryFee) == 0
                && prepTime == that.prepTime
                && open     == that.open
                && approved == that.approved
                && Objects.equals(id,         that.id)
                && Objects.equals(name,       that.name)
                && Objects.equals(cuisine,    that.cuisine)
                && Objects.equals(image,      that.image)
                && Objects.equals(foodPhotos, that.foodPhotos)
                && Objects.equals(foodNames,  that.foodNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cuisine, rating, deliveryFee,
                prepTime, open, approved, image, foodPhotos, foodNames);
    }
}