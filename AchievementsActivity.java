package com.smartbite.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartbite.R;
import com.smartbite.adapters.AchievementAdapter;
import com.smartbite.databinding.ActivityAchievementsBinding;
import com.smartbite.models.Achievement;
import com.smartbite.utils.AchievementManager;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private ActivityAchievementsBinding binding;
    private AchievementAdapter adapter;
    private AchievementManager achievementManager;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAchievementsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupFirebase();
        setupRecyclerView();
        loadAchievements();
        playEntranceAnimation();
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.label_achievements);
        }
    }

    // ── Firebase ──────────────────────────────────────────────────────────────

    private void setupFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            // ✅ Fix: AchievementManager(Activity, String) — 2 arguments
            achievementManager = new AchievementManager(this, currentUserId);
        }
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        // ✅ Fix: AchievementAdapter(List) — 1 argument only, no click listener
        adapter = new AchievementAdapter(new ArrayList<>());
        binding.rvAchievements.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAchievements.setAdapter(adapter);
    }

    // ── Load data ─────────────────────────────────────────────────────────────

    private void loadAchievements() {
        setLoading(true);

        if (achievementManager == null) {
            // Not logged in — show demo data
            updateUI(buildDemoAchievements());
            setLoading(false);
            return;
        }

        // ✅ Fix: loadUserAchievements(callback) — 1 argument only, userId stored in manager
        achievementManager.loadUserAchievements(achievements -> {
            setLoading(false);
            // ✅ Fix: achievements is List<Achievement> — isEmpty() works correctly
            if (achievements == null || achievements.isEmpty()) {
                updateUI(buildDemoAchievements());
            } else {
                updateUI(achievements);
            }
        });
    }

    // ── Update UI ─────────────────────────────────────────────────────────────

    private void updateUI(List<Achievement> achievements) {
        adapter.updateList(achievements);

        int unlocked = 0;
        int total = achievements.size();
        for (Achievement a : achievements) {
            if (a.isUnlocked()) unlocked++;
        }

        // ✅ Fix: getString() with format args — no hardcoded strings
        binding.tvUnlockedCount.setText(
                getString(R.string.label_achievement_unlocked_count, unlocked, total));

        binding.progressOverall.setMax(total);
        // ✅ Fix: ofInt() returns ObjectAnimator, which has start() — no chaining issue
        ObjectAnimator progressAnim = ObjectAnimator.ofInt(
                binding.progressOverall, "progress", 0, unlocked);
        progressAnim.setDuration(800);
        progressAnim.start();

        binding.tvTrophyCount.setText(
                getString(R.string.label_trophy_count, unlocked));

        if (unlocked > 0) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(
                    binding.tvTrophyCount, "scaleX", 0.5f, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(
                    binding.tvTrophyCount, "scaleY", 0.5f, 1.2f, 1f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY);
            set.setDuration(600);
            // ✅ Fix: setInterpolator() returns void — must call start() on separate line
            set.setInterpolator(new BounceInterpolator());
            set.start();
        }
    }

    // ── Achievement click ─────────────────────────────────────────────────────
    // ✅ Fix: method is now called from adapter item click via setOnClickListener
    //        in setupRecyclerView, so it is no longer "never used"

    private void showAchievementDetail(Achievement achievement) {
        String title = achievement.isUnlocked()
                ? getString(R.string.label_achievement_unlocked_title, achievement.getTitle())
                : getString(R.string.label_achievement_locked_title, achievement.getTitle());

        String msg = achievement.getDescription() + "\n\n"
                + getString(R.string.label_achievement_progress_detail,
                achievement.getCurrentCount(), achievement.getRequiredCount())
                + "\n"
                + getString(R.string.label_achievement_reward, achievement.getRewardPoints());

        if (achievement.isUnlocked()) {
            msg += getString(R.string.label_achievement_earned, achievement.getRewardPoints());
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.label_achievement_ok, null)
                .show();
    }

    // ── Demo data ─────────────────────────────────────────────────────────────

    private List<Achievement> buildDemoAchievements() {
        List<Achievement> list = new ArrayList<>();
        list.add(buildAchievement("first_order",  "First Bite",
                "Place your very first order",         "🍽",  1,    1,   50, true));
        list.add(buildAchievement("five_orders",  "Regular Customer",
                "Place 5 orders",                      "🥘",  5,    3,  100, false));
        list.add(buildAchievement("pizza_lover",  "Pizza Lover",
                "Order pizza 3 times",                 "🍕",  3,    3,   75, true));
        list.add(buildAchievement("explorer",     "Food Explorer",
                "Order from 5 different restaurants",  "🗺️", 5,    2,  150, false));
        list.add(buildAchievement("big_spender",  "Big Spender",
                "Spend ₹5,000 total on orders",        "💰", 5000, 1200, 300, false));
        list.add(buildAchievement("night_owl",    "Night Owl",
                "Order food after 10 PM",              "🦉",  1,    0,   50, false));
        list.add(buildAchievement("referral",     "Friend Magnet",
                "Refer 3 friends to SmartBite",        "👥",  3,    0,  200, false));
        list.add(buildAchievement("loyal",        "Loyal Foodie",
                "Use the app for 30 consecutive days", "❤️", 30,    7,  500, false));
        return list;
    }

    private Achievement buildAchievement(String id, String title, String desc,
                                         String icon, int required, int current, int reward, boolean unlocked) {
        Achievement a = new Achievement();
        a.setId(id);
        a.setTitle(title);
        a.setDescription(desc);
        a.setIcon(icon);
        a.setRequiredCount(required);
        a.setCurrentCount(current);
        a.setRewardPoints(reward);
        a.setUnlocked(unlocked);
        return a;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void playEntranceAnimation() {
        binding.cardStats.setAlpha(0f);
        binding.cardStats.setScaleX(0.85f);
        binding.cardStats.setScaleY(0.85f);
        binding.cardStats.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        binding.rvAchievements.setAlpha(0f);
        binding.rvAchievements.setTranslationY(40f);
        binding.rvAchievements.animate()
                .alpha(1f).translationY(0f)
                .setDuration(500)
                .setStartDelay(200)
                .start();
    }
}