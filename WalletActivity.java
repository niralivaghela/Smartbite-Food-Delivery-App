package com.smartbite.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.R;
import com.smartbite.databinding.ActivityWalletBinding;
import com.smartbite.utils.Constants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WalletActivity extends AppCompatActivity {

    private ActivityWalletBinding binding;
    private FirebaseFirestore db;
    private String currentUserId;
    private double walletBalance = 0;

    private static final double[] ADD_AMOUNTS = {100, 250, 500, 1000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFirebase();
        setupQuickAdd();
        loadWalletBalance();
        playEntranceAnimation();
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private void setupToolbar() {
        // ViewBinding guarantees toolbar is NonNull — no null check needed
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_wallet);
        }
    }

    // ── Firebase ──────────────────────────────────────────────────────────────

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) currentUserId = user.getUid();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void setupQuickAdd() {
        // Quick-add preset cards — ViewBinding fields are NonNull, no null checks needed
        View[] addCards = {
                binding.card100, binding.card250,
                binding.card500, binding.card1000
        };
        for (int i = 0; i < addCards.length; i++) {
            final double amt = ADD_AMOUNTS[i];
            addCards[i].setOnClickListener(v -> {
                animateCard(v);
                confirmAddMoney(amt);
            });
        }

        // Custom amount — getText() on TextInputEditText can return null, guard it
        binding.btnAddCustom.setOnClickListener(v -> {
            android.text.Editable editable = binding.etCustomAmount.getText();
            String input = editable != null ? editable.toString().trim() : "";
            if (input.isEmpty()) {
                Toast.makeText(this, R.string.msg_min_amount, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double amt = Double.parseDouble(input);
                if (amt < 10) {
                    Toast.makeText(this, R.string.msg_min_amount, Toast.LENGTH_SHORT).show();
                    return;
                }
                confirmAddMoney(amt);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnPayNow.setOnClickListener(v -> {
            animateCard(v);
            Toast.makeText(this, R.string.msg_payment_redirecting, Toast.LENGTH_SHORT).show();
        });
    }

    // ── Add money ─────────────────────────────────────────────────────────────

    private void confirmAddMoney(double amount) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.label_add_money)
                .setMessage(getString(R.string.msg_confirm_add_money,
                        amount, amount / 10))
                .setPositiveButton(R.string.label_add_money,
                        (d, w) -> addMoneyToWallet(amount))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void addMoneyToWallet(double amount) {
        if (currentUserId == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);

        double newBalance = walletBalance + amount;
        Map<String, Object> update = new HashMap<>();
        update.put("walletBalance", newBalance);
        update.put("loyaltyPoints",
                com.google.firebase.firestore.FieldValue.increment((long)(amount / 10)));

        db.collection(Constants.COLLECTION_USERS).document(currentUserId).update(update)
                .addOnSuccessListener(x -> {
                    setLoading(false);
                    walletBalance = newBalance;
                    animateBalanceUpdate(newBalance);
                    Toast.makeText(this,
                            getString(R.string.msg_money_added, amount),
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    // Demo fallback — show locally even if Firestore write fails
                    walletBalance = newBalance;
                    animateBalanceUpdate(newBalance);
                    Toast.makeText(this,
                            getString(R.string.msg_money_added, amount),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Load balance ──────────────────────────────────────────────────────────

    private void loadWalletBalance() {
        if (currentUserId == null) {
            // No user signed in — show zero without animating
            binding.tvWalletBalance.setText(
                    String.format(Locale.getDefault(), "₹%.2f", 0.0));
            return;
        }
        db.collection(Constants.COLLECTION_USERS).document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double bal = doc.getDouble("walletBalance");
                        Long pts   = doc.getLong("loyaltyPoints");
                        walletBalance = bal != null ? bal : 0;
                        animateBalanceUpdate(walletBalance);
                        if (pts != null) {
                            // Use resource placeholder — avoid setText concatenation warning
                            binding.tvLoyaltyPoints.setText(
                                    getString(R.string.achievement_points, pts.intValue()));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        binding.tvWalletBalance.setText(
                                String.format(Locale.getDefault(), "₹%.2f", 0.0)));
    }

    // ── Animations ────────────────────────────────────────────────────────────

    private void animateBalanceUpdate(double newBalance) {
        // Animate counter from (newBalance - 100) up to newBalance
        float from = (float) Math.max(0, newBalance - 100);
        ValueAnimator animator = ValueAnimator.ofFloat(from, (float) newBalance);
        animator.setDuration(800);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(a -> {
            float val = (float) a.getAnimatedValue();
            binding.tvWalletBalance.setText(
                    String.format(Locale.getDefault(), "₹%.2f", val));
        });
        animator.start();

        // Bounce the balance card — fixed: setDuration returns void, cannot chain .start()
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(
                binding.cardBalance, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(
                binding.cardBalance, "scaleY", 1f, 1.05f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(400);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    private void animateCard(View v) {
        v.animate().scaleX(0.93f).scaleY(0.93f).setDuration(80)
                .withEndAction(() -> v.animate()
                        .scaleX(1f).scaleY(1f).setDuration(120)
                        .setInterpolator(new OvershootInterpolator())
                        .start())
                .start();
    }

    private void setLoading(boolean loading) {
        // ViewBinding field is NonNull — no null check needed
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void playEntranceAnimation() {
        // ViewBinding fields are NonNull — no null checks needed
        binding.cardBalance.setAlpha(0f);
        binding.cardBalance.setScaleX(0.8f);
        binding.cardBalance.setScaleY(0.8f);
        binding.cardBalance.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(1.3f))
                .start();

        int delay = 200;
        for (View row : new View[]{binding.layoutQuickAdd, binding.layoutTransactions}) {
            row.setAlpha(0f);
            row.setTranslationY(40f);
            row.animate().alpha(1f).translationY(0f)
                    .setDuration(400).setStartDelay(delay)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            delay += 120;
        }
    }
}