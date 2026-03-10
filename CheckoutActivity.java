package com.smartbite.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.R;
import com.smartbite.database.CartEntity;
import com.smartbite.databinding.ActivityCheckoutBinding;
import com.smartbite.models.CartItem;
import com.smartbite.models.Order;
import com.smartbite.utils.Constants;
import com.smartbite.viewmodels.CartViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private CartViewModel cartViewModel;
    private FirebaseFirestore db;
    private String currentUserId;
    private List<CartEntity> cartItems = new ArrayList<>();
    private double itemTotal = 0, grandTotal = 0, walletBalance = 0;
    private double discountAmount = 0;
    private String appliedCoupon  = null;
    private String deliveryAddress = "";
    private static final double DELIVERY_FEE = 30.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = user.getUid();

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        // Restore coupon applied in CartActivity
        SharedPreferences prefs = getSharedPreferences("smartbite_prefs", MODE_PRIVATE);
        appliedCoupon = prefs.getString("applied_coupon", null);

        cartViewModel.getAllCartItems().observe(this, items -> cartItems = items);

        cartViewModel.getTotalPrice().observe(this, total -> {
            if (total != null) {
                itemTotal = total;
                recalculateGrandTotal();
                animateBillIn();
            }
        });

        loadUserData();

        binding.btnPlaceOrder.setOnClickListener(v -> {
            if (deliveryAddress.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_add_delivery_address),
                        Toast.LENGTH_SHORT).show();
                shakeView(binding.tvDeliveryAddress);
                return;
            }
            animatePlaceOrderButton();
        });

        binding.tvChangeAddress.setOnClickListener(v -> showAddressDialog());

        // Coupon apply in checkout too
        if (binding.tvApplyPromo != null) {
            binding.tvApplyPromo.setOnClickListener(v -> {
                String code = binding.etPromoCode.getText().toString().trim().toUpperCase();
                applyPromo(code);
            });
        }

        // Slide cards in
        playEntranceAnimation();
    }

    // ── Animations ────────────────────────────────────────────────────────────

    private void playEntranceAnimation() {
        View[] cards = { binding.getRoot().getChildAt(1) }; // NestedScrollView
        for (View v : cards) {
            if (v == null) continue;
            v.setTranslationY(60f);
            v.setAlpha(0f);
            v.animate().translationY(0).alpha(1f).setDuration(450)
                    .setInterpolator(new AccelerateDecelerateInterpolator()).start();
        }
    }

    private void animateBillIn() {
        binding.tvGrandTotal.animate().scaleX(1.08f).scaleY(1.08f).setDuration(150)
                .withEndAction(() -> binding.tvGrandTotal.animate()
                        .scaleX(1f).scaleY(1f).setDuration(150).start())
                .start();
    }

    private void animatePlaceOrderButton() {
        binding.btnPlaceOrder.animate().scaleX(0.94f).scaleY(0.94f).setDuration(100)
                .withEndAction(() -> {
                    binding.btnPlaceOrder.animate().scaleX(1f).scaleY(1f).setDuration(100)
                            .withEndAction(this::processPayment).start();
                }).start();
    }

    // ── Coupon ────────────────────────────────────────────────────────────────

    private void applyPromo(String code) {
        if (code.isEmpty()) { shakeView(binding.etPromoCode); return; }
        switch (code) {
            case "FIRST50":
                discountAmount = Math.min(itemTotal * 0.5, 100.0);
                appliedCoupon  = code;
                break;
            case "FREEDEL":
                if (itemTotal >= 199) { discountAmount = DELIVERY_FEE; appliedCoupon = code; }
                else { Toast.makeText(this, "Add ₹" + (int)(199-itemTotal) + " more", Toast.LENGTH_SHORT).show(); return; }
                break;
            case "SMART10":
                discountAmount = itemTotal * 0.10;
                appliedCoupon  = code;
                break;
            default:
                Toast.makeText(this, "❌ Invalid code", Toast.LENGTH_SHORT).show();
                shakeView(binding.etPromoCode);
                return;
        }
        getSharedPreferences("smartbite_prefs", MODE_PRIVATE)
                .edit().putString("applied_coupon", appliedCoupon).apply();
        Toast.makeText(this, "🎉 Saved ₹" + (int) discountAmount + "!", Toast.LENGTH_SHORT).show();
        recalculateGrandTotal();
    }

    private void recalculateGrandTotal() {
        double delivery = (appliedCoupon != null && appliedCoupon.equals("FREEDEL")) ? 0 : DELIVERY_FEE;
        grandTotal = Math.max(0, itemTotal - discountAmount + delivery);
        updateBillSummary();
    }

    // ── Data / UI ─────────────────────────────────────────────────────────────

    private void loadUserData() {
        db.collection(Constants.COLLECTION_USERS).document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    Double wb = doc.getDouble("walletBalance");
                    walletBalance = wb != null ? wb : 0;
                    binding.tvWalletBalance.setText(
                            String.format(Locale.getDefault(), "₹%.0f", walletBalance));

                    String addressValue = doc.getString("address");
                    deliveryAddress = addressValue != null ? addressValue : "";
                    binding.tvDeliveryAddress.setText(
                            deliveryAddress.isEmpty()
                                    ? getString(R.string.hint_tap_to_add_address)
                                    : deliveryAddress);
                });
    }

    private void updateBillSummary() {
        double delivery = (appliedCoupon != null && appliedCoupon.equals("FREEDEL")) ? 0 : DELIVERY_FEE;
        binding.tvBillItemTotal.setText(String.format(Locale.getDefault(), "₹%.0f", itemTotal));
        binding.tvBillDeliveryFee.setText(String.format(Locale.getDefault(), "₹%.0f", delivery));
        binding.tvGrandTotal.setText(String.format(Locale.getDefault(), "₹%.0f", grandTotal));
        // Highlight discount
        if (discountAmount > 0 && binding.tvBillDeliveryFee != null) {
            binding.tvBillDeliveryFee.setTextColor(Color.parseColor("#2E7D32"));
        }
    }

    // ── Payment ───────────────────────────────────────────────────────────────

    private void processPayment() {
        if (binding.rbWallet.isChecked())      processWalletPayment();
        else if (binding.rbCOD.isChecked())    placeOrder(Constants.PAYMENT_COD, "PENDING");
        else {
            // Online payment not available — guide user to COD or Wallet
            Toast.makeText(this,
                    "Please select Cash on Delivery or Wallet",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void processWalletPayment() {
        if (walletBalance < grandTotal) {
            Toast.makeText(this, getString(R.string.error_insufficient_wallet), Toast.LENGTH_LONG).show();
            shakeView(binding.rbWallet);
            return;
        }
        db.collection(Constants.COLLECTION_USERS).document(currentUserId)
                .update("walletBalance", walletBalance - grandTotal)
                .addOnSuccessListener(u -> placeOrder(Constants.PAYMENT_WALLET, "WALLET_SUCCESS"));
    }

    private void placeOrder(String payMethod, String payId) {
        binding.btnPlaceOrder.setEnabled(false);
        binding.btnPlaceOrder.setText(getString(R.string.label_placing_order));

        String orderId       = UUID.randomUUID().toString();
        String restaurantId  = !cartItems.isEmpty() ? cartItems.get(0).getRestaurantId() : "";
        String restaurantName= !cartItems.isEmpty() ? cartItems.get(0).getRestaurantName() : "";

        List<CartItem> orderItems = new ArrayList<>();
        for (CartEntity e : cartItems)
            orderItems.add(new CartItem(e.getItemId(), e.getName(), e.getImage(),
                    e.getRestaurantId(), e.getPrice(), e.getQuantity()));

        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerId(currentUserId);
        order.setRestaurantId(restaurantId);
        order.setRestaurantName(restaurantName);
        order.setItems(orderItems);
        order.setTotalAmount(grandTotal);
        order.setDeliveryFee(DELIVERY_FEE);
        order.setStatus(Constants.STATUS_PLACED);
        order.setPaymentMethod(payMethod);
        order.setPaymentStatus(payId);
        order.setDeliveryAddress(deliveryAddress);
        order.setTimestamp(System.currentTimeMillis());
        order.setOtp(String.valueOf((int)(Math.random() * 9000) + 1000));

        db.collection(Constants.COLLECTION_ORDERS).document(orderId).set(order)
                .addOnSuccessListener(u -> {
                    cartViewModel.clearCart();
                    // Clear applied coupon
                    getSharedPreferences("smartbite_prefs", MODE_PRIVATE)
                            .edit().remove("applied_coupon").apply();
                    addLoyaltyPoints((int)(grandTotal / 10));
                    Intent intent = new Intent(this, OrderSuccessActivity.class);
                    intent.putExtra(Constants.KEY_ORDER_ID, orderId);
                    intent.putExtra("restaurantName", restaurantName);
                    intent.putExtra("totalAmount",    (int) grandTotal);
                    intent.putExtra("itemsSummary",   orderItems.isEmpty() ? "" : orderItems.get(0).getName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnPlaceOrder.setEnabled(true);
                    binding.btnPlaceOrder.setText(getString(R.string.label_place_order));
                    Toast.makeText(this, getString(R.string.error_order_failed, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void addLoyaltyPoints(int pts) {
        db.collection(Constants.COLLECTION_USERS).document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    Long cur = doc.getLong("loyaltyPoints");
                    int current = cur != null ? cur.intValue() : 0;
                    db.collection(Constants.COLLECTION_USERS).document(currentUserId)
                            .update("loyaltyPoints", current + pts);
                });
    }

    private void showAddressDialog() {
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
        b.setTitle(getString(R.string.title_delivery_address));
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(getString(R.string.hint_enter_delivery_address));
        input.setText(deliveryAddress);
        input.setPadding(48, 24, 48, 24);
        b.setView(input);
        b.setPositiveButton(getString(R.string.action_save), (d, w) -> {
            deliveryAddress = input.getText().toString().trim();
            binding.tvDeliveryAddress.setText(deliveryAddress);
            db.collection(Constants.COLLECTION_USERS).document(currentUserId)
                    .update("address", deliveryAddress);
        });
        b.setNegativeButton(getString(R.string.action_cancel), null);
        b.show();
    }

    private void shakeView(View view) {
        ObjectAnimator.ofFloat(view, "translationX",
                0f, -16f, 16f, -10f, 10f, -5f, 5f, 0f).setDuration(450).start();
    }
}