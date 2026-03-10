package com.smartbite.utils;

public class Constants {

    // ── Firestore collections ─────────────────────────────────────────────────
    public static final String COLLECTION_USERS         = "users";
    public static final String COLLECTION_RESTAURANTS   = "restaurants";
    public static final String COLLECTION_MENU_ITEMS    = "menuItems";
    public static final String COLLECTION_ORDERS        = "orders";
    public static final String COLLECTION_AGENTS        = "deliveryAgents";
    public static final String COLLECTION_PROMOS        = "promos";
    public static final String COLLECTION_REVIEWS       = "reviews";

    // ── User roles ────────────────────────────────────────────────────────────
    public static final String ROLE_CUSTOMER    = "customer";
    public static final String ROLE_RESTAURANT  = "restaurant";
    public static final String ROLE_AGENT       = "agent";
    public static final String ROLE_ADMIN       = "admin";

    // ── Order statuses ────────────────────────────────────────────────────────
    public static final String STATUS_PLACED            = "Placed";
    public static final String STATUS_CONFIRMED         = "Confirmed";
    public static final String STATUS_PREPARING         = "Preparing";
    public static final String STATUS_PICKED_UP         = "Picked Up";
    public static final String STATUS_OUT_FOR_DELIVERY  = "Out for Delivery";
    public static final String STATUS_DELIVERED         = "Delivered";
    public static final String STATUS_CANCELLED         = "Cancelled";

    // ── Payment methods ───────────────────────────────────────────────────────
    public static final String PAYMENT_COD     = "Cash on Delivery";
    public static final String PAYMENT_ONLINE  = "Online";
    public static final String PAYMENT_WALLET  = "Wallet";

    // ── SharedPreferences ─────────────────────────────────────────────────────
    public static final String PREF_NAME        = "SmartBitePrefs";
    public static final String PREF_USER_ID     = "userId";
    public static final String PREF_USER_ROLE   = "userRole";
    public static final String PREF_IS_LOGGED_IN = "isLoggedIn";

    // ── Intent keys ───────────────────────────────────────────────────────────
    public static final String KEY_RESTAURANT_ID = "restaurantId";
    public static final String KEY_ORDER_ID      = "orderId";
    public static final String KEY_USER_ID       = "userId";

    // ── API Keys ──────────────────────────────────────────────────────────────

    // ⚠️  REPLACE with your real test key from dashboard.razorpay.com
    //     Settings → API Keys → Generate Test Key
    //     Test keys look like:  rzp_test_aBcDeFgHiJkLmN
    //     Live keys look like:  rzp_live_aBcDeFgHiJkLmN  (only after account activation)
    public static final String RAZORPAY_KEY = "rzp_test_XXXXXXXXXXXXXXXXXX";

    // ⚠️  REPLACE with your real key from console.cloud.google.com
    public static final String MAPS_API_KEY = "YOUR_MAPS_API_KEY";

    // ── Request codes ─────────────────────────────────────────────────────────
    public static final int REQUEST_IMAGE_PICK          = 100;
    public static final int REQUEST_LOCATION_PERMISSION = 101;
    public static final int REQUEST_CAMERA_PERMISSION   = 102;
    public static final int VOICE_REQUEST_CODE          = 999;
}