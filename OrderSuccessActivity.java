package com.smartbite.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smartbite.R;
import com.smartbite.databinding.ActivityOrderSuccessBinding;

import java.util.Locale;

public class OrderSuccessActivity extends AppCompatActivity {

    private ActivityOrderSuccessBinding binding;
    private CountDownTimer estimatedTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // toolbar is NonNull from ViewBinding — no null check needed
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> goHome());

        String orderId        = getIntent().getStringExtra("orderId");
        String restaurantName = getIntent().getStringExtra("restaurantName");
        int    totalAmount    = getIntent().getIntExtra("totalAmount", 0);
        String itemsSummary   = getIntent().getStringExtra("itemsSummary");
        int    prepTime       = getIntent().getIntExtra("prepTime", 30);

        TextView       tvRestaurant = findViewById(R.id.tvRestaurantName);
        TextView       tvItems      = findViewById(R.id.tvItemsSummary);
        TextView       tvTotal      = findViewById(R.id.tvTotal);
        TextView       tvEstTime    = findViewById(R.id.tvEstTime);
        MaterialButton btnTrack     = findViewById(R.id.btnTrackOrder);
        TextView       btnHome      = findViewById(R.id.btnBackHome);

        if (tvRestaurant != null)
            tvRestaurant.setText(getString(R.string.label_order_from,
                    restaurantName != null ? restaurantName : getString(R.string.label_restaurant)));

        if (tvItems != null)
            tvItems.setText(itemsSummary != null ? itemsSummary : "-");

        if (tvTotal != null)
            tvTotal.setText(getString(R.string.label_rupee_amount, totalAmount));

        if (tvEstTime != null)
            startLiveCountdown(tvEstTime, prepTime * 60 * 1000L);

        if (btnTrack != null) {
            btnTrack.setOnClickListener(v -> {
                animateButton(v);
                Intent intent = new Intent(this, OrderTrackingActivity.class);
                intent.putExtra("orderId",        orderId);
                intent.putExtra("restaurantName", restaurantName);
                intent.putExtra("totalAmount",    totalAmount);
                intent.putExtra("itemsSummary",   itemsSummary);
                startActivity(intent);
            });
        }

        if (btnHome != null)
            btnHome.setOnClickListener(v -> goHome());

        playSuccessAnimation();
    }

    // ── Animated countdown ────────────────────────────────────────────────────

    private void startLiveCountdown(TextView tv, long millis) {
        estimatedTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long ms) {
                long minutes = ms / 60000;
                long seconds = (ms % 60000) / 1000;
                tv.setText(String.format(Locale.getDefault(), "%dm %02ds", minutes, seconds));
            }
            @Override
            public void onFinish() {
                tv.setText(R.string.msg_almost_there);
            }
        }.start();
    }

    // ── Entrance animations ───────────────────────────────────────────────────

    private void playSuccessAnimation() {
        View checkCard = binding.getRoot().findViewWithTag("checkCard");
        if (checkCard == null) {
            animateContentFadeIn();
            return;
        }

        checkCard.setScaleX(0f);
        checkCard.setScaleY(0f);

        ObjectAnimator sx = ObjectAnimator.ofFloat(checkCard, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(checkCard, "scaleY", 0f, 1.2f, 1f);
        sx.setDuration(700);
        sy.setDuration(700);
        sx.setInterpolator(new OvershootInterpolator(2f));
        sy.setInterpolator(new OvershootInterpolator(2f));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(sx, sy);
        set.start();
    }

    private void animateContentFadeIn() {
        View content = binding.getRoot().findViewWithTag("contentLayout");
        if (content == null) return;
        content.setAlpha(0f);
        content.setTranslationY(50f);
        content.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateButton(View v) {
        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                .withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                .start();
    }

    private void goHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (estimatedTimer != null) estimatedTimer.cancel();
    }
}