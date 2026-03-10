package com.smartbite.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.databinding.ActivitySplashBinding;
import com.smartbite.utils.Constants;

@SuppressWarnings("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Full-screen immersive
        getWindow().setStatusBarColor(Color.parseColor("#FC8019"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startAnimations();
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, 3800);
    }

    private void startAnimations() {
        // Logo: scale + bounce
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.ivLogo, "scaleX", 0f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.ivLogo, "scaleY", 0f, 1.15f, 1f);
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(binding.ivLogo, "alpha", 0f, 1f);
        scaleX.setDuration(900);
        scaleY.setDuration(900);
        logoAlpha.setDuration(600);
        scaleX.setInterpolator(new OvershootInterpolator(1.2f));
        scaleY.setInterpolator(new OvershootInterpolator(1.2f));

        // App name: slide up + fade
        ObjectAnimator slideUp   = ObjectAnimator.ofFloat(binding.tvAppName, "translationY", 80f, 0f);
        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 0f, 1f);
        slideUp.setStartDelay(700);
        nameAlpha.setStartDelay(700);
        slideUp.setDuration(700);
        nameAlpha.setDuration(700);
        slideUp.setInterpolator(new AccelerateDecelerateInterpolator());

        // Tagline: slide up + fade (delayed more)
        ObjectAnimator tagSlide = ObjectAnimator.ofFloat(binding.tvTagline, "translationY", 50f, 0f);
        ObjectAnimator tagAlpha = ObjectAnimator.ofFloat(binding.tvTagline, "alpha", 0f, 1f);
        tagSlide.setStartDelay(1100);
        tagAlpha.setStartDelay(1100);
        tagSlide.setDuration(600);
        tagAlpha.setDuration(600);

        // Progress bar pulse
        if (binding.progressBar != null) {
            binding.progressBar.setAlpha(0f);
            ObjectAnimator pbFade = ObjectAnimator.ofFloat(binding.progressBar, "alpha", 0f, 1f);
            pbFade.setStartDelay(1600);
            pbFade.setDuration(400);
            pbFade.start();
        }

        // Continuous subtle logo breathing
        ValueAnimator breathe = ValueAnimator.ofFloat(1f, 1.06f, 1f);
        breathe.setDuration(2000);
        breathe.setRepeatCount(ValueAnimator.INFINITE);
        breathe.setInterpolator(new AccelerateDecelerateInterpolator());
        breathe.setStartDelay(1600);
        breathe.addUpdateListener(a -> {
            float v = (float) a.getAnimatedValue();
            binding.ivLogo.setScaleX(v);
            binding.ivLogo.setScaleY(v);
        });
        breathe.start();

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, logoAlpha, slideUp, nameAlpha, tagSlide, tagAlpha);
        set.start();
    }

    private void navigateNext() {
        // Fade out before navigating
        binding.getRoot().animate()
                .alpha(0f)
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        doNavigate();
                    }
                }).start();
    }

    private void doNavigate() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            checkUserRoleAndRedirect(auth.getCurrentUser().getUid());
        } else {
            navigate(LoginActivity.class);
        }
    }

    private void checkUserRoleAndRedirect(String uid) {
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(doc -> {
                    String role = doc.exists() ? doc.getString("role") : Constants.ROLE_CUSTOMER;
                    Class<?> dest;
                    if (Constants.ROLE_RESTAURANT.equals(role))  dest = RestaurantPanelActivity.class;
                    else if (Constants.ROLE_AGENT.equals(role))  dest = DeliveryAgentActivity.class;
                    else if (Constants.ROLE_ADMIN.equals(role))  dest = AdminActivity.class;
                    else                                          dest = HomeActivity.class;
                    navigate(dest);
                })
                .addOnFailureListener(e -> navigate(HomeActivity.class));
    }

    private void navigate(Class<?> dest) {
        startActivity(new Intent(this, dest));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}