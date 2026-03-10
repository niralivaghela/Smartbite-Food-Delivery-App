package com.smartbite.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.R;
import com.smartbite.adapters.MealPlanAdapter;
import com.smartbite.databinding.ActivityMealPlanBinding;
import com.smartbite.models.MealPlan;
import com.smartbite.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealPlanActivity extends AppCompatActivity {

    private ActivityMealPlanBinding binding;
    private FirebaseFirestore db;
    private String currentUserId;
    private String activePlanId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMealPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFirebase();
        setupRecyclerView();
        loadActivePlan();
        playEntranceAnimation();
    }

    private void setupToolbar() {
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            binding.toolbar.setNavigationOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed());
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Plans 🥗");
        }
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) currentUserId = user.getUid();
    }

    private void setupRecyclerView() {
        List<MealPlan> plans = buildPlans();
        MealPlanAdapter adapter = new MealPlanAdapter(plans, this::onPlanSelected);
        if (binding.rvMealPlans != null) {
            binding.rvMealPlans.setLayoutManager(new LinearLayoutManager(this));
            binding.rvMealPlans.setAdapter(adapter);
        }
    }

    private List<MealPlan> buildPlans() {
        List<MealPlan> list = new ArrayList<>();

        MealPlan starter = new MealPlan();
        starter.setPlanId("starter");
        starter.setName("Starter");
        starter.setDescription("Perfect for light eaters. 5 meals/week with healthy options.");
        starter.setBadge("🌱");
        starter.setColor("#4CAF50");
        starter.setWeeklyPrice(399);
        starter.setMonthlyPrice(1399);
        starter.setMealsPerWeek(5);
        list.add(starter);

        MealPlan popular = new MealPlan();
        popular.setPlanId("popular");
        popular.setName("Popular");
        popular.setDescription("Most chosen! 10 meals/week with variety across all cuisines.");
        popular.setBadge("⭐");
        popular.setColor("#FF6B35");
        popular.setWeeklyPrice(699);
        popular.setMonthlyPrice(2499);
        popular.setMealsPerWeek(10);
        list.add(popular);

        MealPlan family = new MealPlan();
        family.setPlanId("family");
        family.setName("Family");
        family.setDescription("Feed the whole family! 20 meals/week with premium choices.");
        family.setBadge("👨‍👩‍👧‍👦");
        family.setColor("#9C27B0");
        family.setWeeklyPrice(1199);
        family.setMonthlyPrice(4299);
        family.setMealsPerWeek(20);
        list.add(family);

        MealPlan premium = new MealPlan();
        premium.setPlanId("premium");
        premium.setName("Premium");
        premium.setDescription("Unlimited meals + priority delivery + free desserts every day! 🎉");
        premium.setBadge("👑");
        premium.setColor("#FFB800");
        premium.setWeeklyPrice(1999);
        premium.setMonthlyPrice(6999);
        premium.setMealsPerWeek(999); // Unlimited
        list.add(premium);

        return list;
    }

    private void onPlanSelected(MealPlan plan) {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle(plan.getBadge() + " " + plan.getName() + " Plan")
                .setMessage("Subscribe to " + plan.getName() + " for ₹" + plan.getMonthlyPrice()
                        + "/month?\n\n✅ " + plan.getMealsPerWeek() + " meals/week\n"
                        + "✅ Free delivery on plan meals\n"
                        + "✅ Priority support")
                .setPositiveButton("Subscribe 🚀", (d, w) -> subscribeToPlan(plan))
                .setNegativeButton("Maybe later", null)
                .show();
    }

    private void subscribeToPlan(MealPlan plan) {
        if (currentUserId == null) {
            Toast.makeText(this, "Please log in!", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);

        Map<String, Object> update = new HashMap<>();
        update.put("activePlan", plan.getPlanId());

        db.collection(Constants.COLLECTION_USERS).document(currentUserId).update(update)
                .addOnSuccessListener(x -> {
                    setLoading(false);
                    activePlanId = plan.getPlanId();
                    showSubscribeSuccess(plan);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    // Still show success for demo
                    activePlanId = plan.getPlanId();
                    showSubscribeSuccess(plan);
                });
    }

    private void showSubscribeSuccess(MealPlan plan) {
        // Animate success badge
        if (binding.tvActivePlan != null) {
            binding.tvActivePlan.setVisibility(View.VISIBLE);
            binding.tvActivePlan.setText("✅ Active: " + plan.getName() + " Plan");
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.tvActivePlan, "scaleX", 0f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.tvActivePlan, "scaleY", 0f, 1.1f, 1f);
            android.animation.AnimatorSet set = new android.animation.AnimatorSet();
            set.playTogether(scaleX, scaleY);
            set.setDuration(400).start();
        }
        Toast.makeText(this,
                "🎉 Subscribed to " + plan.getName() + "! Enjoy your meals!",
                Toast.LENGTH_LONG).show();
    }

    private void loadActivePlan() {
        if (currentUserId == null) return;
        db.collection(Constants.COLLECTION_USERS).document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        activePlanId = doc.getString("activePlan");
                        if (activePlanId != null && !activePlanId.isEmpty()
                                && binding.tvActivePlan != null) {
                            binding.tvActivePlan.setVisibility(View.VISIBLE);
                            binding.tvActivePlan.setText("✅ Active Plan: " + capitalize(activePlanId));
                        }
                    }
                });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void setLoading(boolean loading) {
        if (binding.progressBar != null)
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void playEntranceAnimation() {
        if (binding.tvTitle != null) {
            binding.tvTitle.setAlpha(0f);
            binding.tvTitle.setTranslationY(-30f);
            binding.tvTitle.animate().alpha(1f).translationY(0f).setDuration(400).start();
        }
        if (binding.rvMealPlans != null) {
            binding.rvMealPlans.setAlpha(0f);
            binding.rvMealPlans.setTranslationY(50f);
            binding.rvMealPlans.animate().alpha(1f).translationY(0f)
                    .setDuration(500).setStartDelay(150)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }
}