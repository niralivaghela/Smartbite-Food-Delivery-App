package com.smartbite.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.List;
import java.util.Objects;

public class Order {

    // ── Fields ───────────────────────────────────────────────────────────────
    // IMPORTANT: Firestore serializes every public getter as a field.
    // We use @Exclude on computed/alias getters to prevent duplicate/wrong fields
    // being saved. Only real fields should be read/written by Firestore.

    private String           orderId;
    private String           customerId;   // PRIMARY user id field — always use this
    private String           restaurantId;
    private String           restaurantName;
    private String           status;
    private double           totalAmount;
    private double           deliveryFee;
    private long             timestamp;
    private String           deliveryAddress;
    private String           paymentMethod;
    private String           paymentStatus;
    private String           otp;
    private List<CartItem>   items;

    // ── Constructor ──────────────────────────────────────────────────────────

    public Order() {} // Required for Firestore deserialization

    // ── Getters (read by Firestore when SAVING) ──────────────────────────────

    public String         getOrderId()         { return orderId; }
    public String         getCustomerId()      { return customerId; }  // ✅ Firestore saves this as "customerId"
    public String         getRestaurantId()    { return restaurantId; }
    public String         getRestaurantName()  { return restaurantName; }
    public String         getStatus()          { return status; }
    public double         getTotalAmount()     { return totalAmount; }
    public double         getDeliveryFee()     { return deliveryFee; }
    public long           getTimestamp()       { return timestamp; }   // ✅ Single timestamp field
    public String         getDeliveryAddress() { return deliveryAddress; }
    public String         getPaymentMethod()   { return paymentMethod; }
    public String         getPaymentStatus()   { return paymentStatus; }
    public String         getOtp()             { return otp; }
    public List<CartItem> getItems()           { return items; }

    // ── @Exclude getters — NOT saved to Firestore ────────────────────────────

    @Exclude
    public String getUserId() {
        // Alias for backwards compat — use customerId as source of truth
        return customerId;
    }

    @Exclude
    public long getCreatedAt() {
        // Alias — timestamp is the single source of truth
        return timestamp;
    }

    // ── Setters (called by Firestore when LOADING) ───────────────────────────

    public void setOrderId(String orderId)                 { this.orderId = orderId; }

    public void setCustomerId(String customerId)           { this.customerId = customerId; }

    // FIX: setUserId maps into customerId so old documents still deserialize correctly
    public void setUserId(String userId)                   { this.customerId = userId; }

    public void setRestaurantId(String restaurantId)       { this.restaurantId = restaurantId; }
    public void setRestaurantName(String restaurantName)   { this.restaurantName = restaurantName; }
    public void setStatus(String status)                   { this.status = status; }
    public void setTotalAmount(double totalAmount)         { this.totalAmount = totalAmount; }
    public void setDeliveryFee(double deliveryFee)         { this.deliveryFee = deliveryFee; }

    public void setTimestamp(long timestamp)               { this.timestamp = timestamp; }

    // FIX: setCreatedAt also maps into timestamp for old documents
    public void setCreatedAt(long createdAt)               { if (this.timestamp == 0) this.timestamp = createdAt; }

    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setPaymentMethod(String paymentMethod)     { this.paymentMethod = paymentMethod; }
    public void setPaymentStatus(String paymentStatus)     { this.paymentStatus = paymentStatus; }
    public void setOtp(String otp)                         { this.otp = otp; }
    public void setItems(List<CartItem> items)             { this.items = items; }

    // ── DiffUtil support ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order that = (Order) o;
        return Double.compare(that.totalAmount, totalAmount) == 0
                && Double.compare(that.deliveryFee, deliveryFee) == 0
                && timestamp == that.timestamp
                && Objects.equals(orderId,         that.orderId)
                && Objects.equals(customerId,      that.customerId)
                && Objects.equals(restaurantId,    that.restaurantId)
                && Objects.equals(status,          that.status)
                && Objects.equals(paymentMethod,   that.paymentMethod)
                && Objects.equals(paymentStatus,   that.paymentStatus)
                && Objects.equals(deliveryAddress, that.deliveryAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, customerId, restaurantId, status,
                totalAmount, deliveryFee, timestamp,
                paymentMethod, paymentStatus, deliveryAddress);
    }
}