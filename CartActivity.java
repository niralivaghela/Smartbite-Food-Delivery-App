package com.smartbite.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.smartbite.R;
import com.smartbite.adapters.CartAdapter;
import com.smartbite.database.CartEntity;
import com.smartbite.databinding.ActivityCartBinding;
import com.smartbite.viewmodels.CartViewModel;

import java.util.ArrayList;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {

    private ActivityCartBinding binding;
    private CartViewModel       cartViewModel;
    private CartAdapter         cartAdapter;

    private static final double DELIVERY_FEE    = 30.0;
    @SuppressWarnings("SpellCheckingInspection")
    private static final String COUPON_FREE_DEL = "FREEDEL";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String PREFS_NAME      = "smartbite_prefs";
    private static final String KEY_COUPON      = "applied_coupon";

    private double discountAmount     = 0.0;
    private String appliedCoupon      = null;
    private double currentTotal       = 0.0;
    private double lastDisplayedTotal = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        cartAdapter = new CartAdapter(this, new ArrayList<>(), this);
        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCart.setAdapter(cartAdapter);

        // ── Swipe-to-remove ───────────────────────────────────────────────────
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                CartEntity removed = cartAdapter.getItemAt(pos);
                cartViewModel.removeItem(removed);
                String removedName = removed.getName() != null ? removed.getName() : "";
                Snackbar.make(binding.getRoot(),
                                getString(R.string.msg_item_removed, removedName),
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_undo, v ->
                                cartViewModel.increaseQuantity(removed))
                        .setActionTextColor(getColor(android.R.color.holo_orange_light))
                        .show();
            }
        }).attachToRecyclerView(binding.rvCart);

        // ── Observe cart ──────────────────────────────────────────────────────
        cartViewModel.getAllCartItems().observe(this, items -> {
            if (items == null || items.isEmpty()) {
                animateVisibility(binding.rvCart, false);
                animateVisibility(binding.layoutEmptyCart, true);
                animateVisibility(binding.cardOrderSummary, false);
            } else {
                animateVisibility(binding.layoutEmptyCart, false);
                animateVisibility(binding.rvCart, true);
                animateVisibility(binding.cardOrderSummary, true);
                cartAdapter.updateList(items);
            }
        });

        cartViewModel.getTotalPrice().observe(this, total -> {
            if (total != null) {
                currentTotal = total;
                animateTotalChange();
            }
        });

        // Restore saved coupon
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String saved = prefs.getString(KEY_COUPON, null);
        if (saved != null) {
            appliedCoupon = saved;
            binding.etPromoCode.setText(saved);
        }

        binding.btnProceedToCheckout.setOnClickListener(v -> {
            animateButtonPulse(v);
            startActivity(new Intent(this, CheckoutActivity.class));
        });

        binding.tvApplyPromo.setOnClickListener(v -> {
            CharSequence text = binding.etPromoCode.getText();
            String code = text != null ? text.toString().trim().toUpperCase(Locale.ROOT) : "";
            applyPromoCode(code);
        });
    }

    // ── Coupon logic ──────────────────────────────────────────────────────────

    private void applyPromoCode(String code) {
        if (code.isEmpty()) {
            Toast.makeText(this, R.string.error_enter_promo, Toast.LENGTH_SHORT).show();
            shakeView(binding.etPromoCode);
            return;
        }
        if (code.equals(appliedCoupon)) {
            Toast.makeText(this,
                    getString(R.string.msg_coupon_already_applied, code),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        switch (code) {
            case "FIRST50":
                discountAmount = Math.min(currentTotal * 0.5, 100.0);
                appliedCoupon  = code;
                break;

            case COUPON_FREE_DEL:
                if (currentTotal >= 199) {
                    discountAmount = DELIVERY_FEE;
                    appliedCoupon  = code;
                } else {
                    int needed = (int) Math.ceil(199 - currentTotal);
                    Toast.makeText(this,
                            getString(R.string.msg_add_more_for_free_delivery, needed),
                            Toast.LENGTH_SHORT).show();
                    shakeView(binding.etPromoCode);
                    return;
                }
                break;

            case "SMART10":
                discountAmount = currentTotal * 0.10;
                appliedCoupon  = code;
                break;

            default:
                Toast.makeText(this, R.string.error_invalid_promo, Toast.LENGTH_SHORT).show();
                shakeView(binding.etPromoCode);
                return;
        }

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit().putString(KEY_COUPON, appliedCoupon).apply();

        Snackbar.make(binding.getRoot(),
                        getString(R.string.msg_coupon_saved, (int) discountAmount),
                        Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(android.R.color.holo_green_dark))
                .show();
        updatePriceSummary();
    }

    private void animateTotalChange() {
        if (lastDisplayedTotal < 0) {
            lastDisplayedTotal = currentTotal;
            updatePriceSummary();
            return;
        }
        ValueAnimator anim = ValueAnimator.ofFloat(
                (float) lastDisplayedTotal, (float) currentTotal);
        anim.setDuration(350);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addUpdateListener(a -> {
            float v        = (float) a.getAnimatedValue();
            double delivery = isFreeDelivery() ? 0 : DELIVERY_FEE;
            double grand    = Math.max(0, v - discountAmount + delivery);
            binding.tvItemTotal.setText(
                    getString(R.string.label_rupee_amount_float,
                            String.format(Locale.getDefault(), "%.0f", v)));
            binding.tvGrandTotal.setText(
                    getString(R.string.label_rupee_amount_float,
                            String.format(Locale.getDefault(), "%.0f", grand)));
        });
        anim.start();
        lastDisplayedTotal = currentTotal;
    }

    private void updatePriceSummary() {
        double delivery = isFreeDelivery() ? 0 : DELIVERY_FEE;
        double grand    = Math.max(0, currentTotal - discountAmount + delivery);
        binding.tvItemTotal.setText(
                getString(R.string.label_rupee_amount_float,
                        String.format(Locale.getDefault(), "%.0f", currentTotal)));
        binding.tvGrandTotal.setText(
                getString(R.string.label_rupee_amount_float,
                        String.format(Locale.getDefault(), "%.0f", grand)));
    }

    private boolean isFreeDelivery() {
        return COUPON_FREE_DEL.equals(appliedCoupon);
    }

    // ── Cart actions ──────────────────────────────────────────────────────────

    @Override
    public void onIncrease(@NonNull CartEntity item) {
        cartViewModel.increaseQuantity(item);
    }

    @Override
    public void onDecrease(@NonNull CartEntity item) {
        cartViewModel.decreaseQuantity(item);
    }

    @Override
    public void onRemove(@NonNull CartEntity item) {
        String name = item.getName() != null ? item.getName() : "";
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_remove_item)
                .setMessage(getString(R.string.msg_confirm_remove, name))
                .setPositiveButton(R.string.action_remove, (d, w) -> {
                    cartViewModel.removeItem(item);
                    Snackbar.make(binding.getRoot(),
                            getString(R.string.msg_item_removed, name),
                            Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // ── Animations ────────────────────────────────────────────────────────────

    private void animateVisibility(View v, boolean show) {
        if (show) {
            v.setVisibility(View.VISIBLE);
            v.setAlpha(0f);
            v.animate().alpha(1f).setDuration(250).start();
        } else {
            v.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> v.setVisibility(View.GONE)).start();
        }
    }

    private void shakeView(View view) {
        ObjectAnimator.ofFloat(view, "translationX",
                        0f, -16f, 16f, -10f, 10f, -5f, 5f, 0f)
                .setDuration(450).start();
    }

    private void animateButtonPulse(View v) {
        v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80)
                .withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                .start();
    }
}