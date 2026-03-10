package com.smartbite.fragments.customer;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.R;
import com.smartbite.activities.LoginActivity;
import com.smartbite.activities.OrderHistoryActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;

    // ══════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        bindAndPopulateUser(v);
        setupAvatarRingAnimation(v);
        setupOnlineDotPulse(v);
        setupMenuListeners(v);
        setupQuickToggles(v);
        setupReferralBanner(v);
        setupLogout(v);
        animateCardEntrance(v);
    }

    // ══════════════════════════════════════════════════════════════════
    //  USER DATA
    // ══════════════════════════════════════════════════════════════════

    private void bindAndPopulateUser(View v) {
        CircleImageView ivAvatar       = v.findViewById(R.id.ivAvatar);
        TextView tvName                = v.findViewById(R.id.tvProfileName);
        TextView tvEmail               = v.findViewById(R.id.tvProfileEmail);
        TextView tvWallet              = v.findViewById(R.id.tvWalletBalance);
        TextView tvPoints              = v.findViewById(R.id.tvPoints);
        TextView tvTier                = v.findViewById(R.id.tvMemberTier);
        TextView tvLevelProgress       = v.findViewById(R.id.tvLevelProgress);
        LinearProgressIndicator progressLevel = v.findViewById(R.id.progressLevel);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String displayName = user.getDisplayName();
        tvName.setText(displayName != null && !displayName.isEmpty()
                ? displayName
                : getString(R.string.placeholder_user_name));
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        if (user.getPhotoUrl() != null && ivAvatar != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        }

        View btnEditAvatar = v.findViewById(R.id.btnEditAvatar);
        if (btnEditAvatar != null)
            btnEditAvatar.setOnClickListener(x ->
                    Toast.makeText(requireContext(),
                            getString(R.string.msg_change_photo_soon), Toast.LENGTH_SHORT).show());

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || doc == null) return;

                    Double walletVal = doc.getDouble("walletBalance");
                    Long   ptsVal    = doc.getLong("points");
                    int    wallet    = walletVal != null ? walletVal.intValue() : 0;
                    int    pts       = ptsVal    != null ? (int) (long) ptsVal  : 0;

                    if (tvWallet != null) animateWalletCounter(tvWallet, wallet);
                    if (tvPoints != null) animatePointsCounter(tvPoints, pts);

                    if (tvTier != null) tvTier.setText(resolveTierLabel(pts));

                    int nextThreshold = pts >= 5000 ? 5000 : pts >= 2000 ? 5000 : pts >= 500 ? 2000 : 500;
                    int prevThreshold = pts >= 2000 ? 2000 : pts >= 500  ? 500  : 0;
                    int progressPct   = nextThreshold == prevThreshold ? 100
                            : (int) (((float)(pts - prevThreshold) / (nextThreshold - prevThreshold)) * 100);
                    String nextTier   = pts >= 5000 ? getString(R.string.tier_max)
                            : pts >= 2000 ? getString(R.string.tier_diamond)
                            : pts >= 500  ? getString(R.string.tier_platinum)
                            : getString(R.string.tier_gold);

                    if (tvLevelProgress != null)
                        tvLevelProgress.setText(getString(R.string.label_level_progress,
                                pts, nextThreshold, nextTier));
                    if (progressLevel != null)
                        progressLevel.setProgressCompat(Math.min(progressPct, 100), true);
                });
    }

    private String resolveTierLabel(int pts) {
        if (pts >= 5000) return getString(R.string.tier_diamond_label);
        if (pts >= 2000) return getString(R.string.tier_platinum_label);
        if (pts >= 500)  return getString(R.string.tier_gold_label);
        return getString(R.string.tier_silver_label);
    }

    // ══════════════════════════════════════════════════════════════════
    //  ANIMATED COUNTER — wallet (₹ prefix)
    // ══════════════════════════════════════════════════════════════════

    private void animateWalletCounter(TextView tv, int target) {
        ValueAnimator va = ValueAnimator.ofInt(0, target);
        va.setDuration(1200);
        va.setInterpolator(new AccelerateDecelerateInterpolator());
        va.addUpdateListener(a ->  {
            if (!isAdded()) return;
            tv.setText(getString(R.string.label_wallet_amount, (int) a.getAnimatedValue()));
        });
        va.start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  ANIMATED COUNTER — points (pts suffix)
    // ══════════════════════════════════════════════════════════════════

    private void animatePointsCounter(TextView tv, int target) {
        ValueAnimator va = ValueAnimator.ofInt(0, target);
        va.setDuration(1200);
        va.setInterpolator(new AccelerateDecelerateInterpolator());
        va.addUpdateListener(a -> {
            if (!isAdded()) return;
            tv.setText(getString(R.string.label_points_amount, (int) a.getAnimatedValue()));
        });
        va.start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  AVATAR RING ANIMATION  (rotating glow + breathing scale)
    // ══════════════════════════════════════════════════════════════════

    private void setupAvatarRingAnimation(View v) {
        View ring = v.findViewById(R.id.ivAvatarRing);
        if (ring == null) return;

        ObjectAnimator rotate = ObjectAnimator.ofFloat(ring, "rotation", 0f, 360f);
        rotate.setDuration(6000);
        rotate.setRepeatCount(ValueAnimator.INFINITE);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());
        rotate.start();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ring, "scaleX", 1f, 1.06f, 1f);
        scaleX.setDuration(2500);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.start();

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ring, "scaleY", 1f, 1.06f, 1f);
        scaleY.setDuration(2500);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  ONLINE DOT  (pulsing alpha)
    // ══════════════════════════════════════════════════════════════════

    private void setupOnlineDotPulse(View v) {
        View dot = v.findViewById(R.id.ivOnlineDot);
        if (dot == null) return;

        ObjectAnimator pulse = ObjectAnimator.ofFloat(dot, "alpha", 1f, 0.2f, 1f);
        pulse.setDuration(1400);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  CARD ENTRANCE ANIMATIONS
    // ══════════════════════════════════════════════════════════════════

    private void animateCardEntrance(View v) {
        int[] cardIds = { R.id.cardWallet, R.id.cardPoints };
        for (int i = 0; i < cardIds.length; i++) {
            View card = v.findViewById(cardIds[i]);
            if (card == null) continue;
            card.setAlpha(0f);
            card.setTranslationY(40f);
            card.animate()
                    .alpha(1f).translationY(0f)
                    .setStartDelay(100L * (i + 1))
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  QUICK SETTINGS TOGGLES
    // ══════════════════════════════════════════════════════════════════

    private void setupQuickToggles(View v) {
        SwitchMaterial switchNotif    = v.findViewById(R.id.switchNotifications);
        SwitchMaterial switchDarkMode = v.findViewById(R.id.switchDarkMode);

        if (switchNotif != null) {
            switchNotif.setOnCheckedChangeListener((btn, checked) ->
                    Toast.makeText(requireContext(),
                            getString(checked ? R.string.msg_notif_on : R.string.msg_notif_off),
                            Toast.LENGTH_SHORT).show());
        }

        if (switchDarkMode != null) {
            boolean isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            switchDarkMode.setChecked(isDark);
            switchDarkMode.setOnCheckedChangeListener((btn, checked) -> {
                AppCompatDelegate.setDefaultNightMode(
                        checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(requireContext(),
                        getString(checked ? R.string.msg_dark_mode_on : R.string.msg_dark_mode_off),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  REFERRAL BANNER
    // ══════════════════════════════════════════════════════════════════

    private void setupReferralBanner(View v) {
        View btnReferNow = v.findViewById(R.id.btnReferNow);
        if (btnReferNow == null) return;

        btnReferNow.setOnClickListener(x -> {
            FirebaseUser user = mAuth.getCurrentUser();
            String code = user != null
                    ? "SMART" + user.getUid().substring(0, 6).toUpperCase()
                    : getString(R.string.referral_code_default);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_referral_share, code));
            startActivity(Intent.createChooser(share, getString(R.string.label_invite_friend)));
        });
    }

    // ══════════════════════════════════════════════════════════════════
    //  MENU LISTENERS
    // ══════════════════════════════════════════════════════════════════

    private void setupMenuListeners(View v) {
        v.findViewById(R.id.menuOrders).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), OrderHistoryActivity.class)));

        v.findViewById(R.id.menuAddresses).setOnClickListener(x ->
                Toast.makeText(requireContext(),
                        getString(R.string.msg_addresses_soon), Toast.LENGTH_SHORT).show());

        v.findViewById(R.id.menuOffers).setOnClickListener(x ->
                Toast.makeText(requireContext(),
                        getString(R.string.msg_offers_soon), Toast.LENGTH_SHORT).show());

        v.findViewById(R.id.menuSupport).setOnClickListener(x -> {
            Intent email = new Intent(Intent.ACTION_SENDTO,
                    Uri.parse("mailto:" + getString(R.string.support_email)));
            email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject));
            try {
                startActivity(Intent.createChooser(email, getString(R.string.label_contact_support)));
            } catch (Exception e) {
                Toast.makeText(requireContext(),
                        getString(R.string.msg_no_email_app), Toast.LENGTH_SHORT).show();
            }
        });

        v.findViewById(R.id.menuSettings).setOnClickListener(x ->
                Toast.makeText(requireContext(),
                        getString(R.string.msg_settings_soon), Toast.LENGTH_SHORT).show());

        View btnAddMoney = v.findViewById(R.id.btnAddMoney);
        if (btnAddMoney != null)
            btnAddMoney.setOnClickListener(x ->
                    Toast.makeText(requireContext(),
                            getString(R.string.msg_add_money_soon), Toast.LENGTH_SHORT).show());

        View btnRedeem = v.findViewById(R.id.btnRedeemPoints);
        if (btnRedeem != null)
            btnRedeem.setOnClickListener(x ->
                    Toast.makeText(requireContext(),
                            getString(R.string.msg_redeem_soon), Toast.LENGTH_SHORT).show());
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOGOUT  (confirmation dialog)
    // ══════════════════════════════════════════════════════════════════

    private void setupLogout(View v) {
        v.findViewById(R.id.btnLogout).setOnClickListener(x ->
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.title_logout)
                        .setMessage(R.string.msg_logout_confirm)
                        .setPositiveButton(R.string.action_logout, (d, i) -> {
                            mAuth.signOut();
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton(R.string.action_cancel, null)
                        .show());
    }
}