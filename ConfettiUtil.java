package com.smartbite.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ConfettiUtil – drop colourful emoji confetti over any ViewGroup.
 *
 * Usage:
 *   ConfettiUtil.burst(rootLayout, 30);
 *   ConfettiUtil.celebrate(rootLayout); // shows 🎉 + bursts confetti
 */
public class ConfettiUtil {

    private static final String[] EMOJIS  = {"🎉","🎊","⭐","✨","🌟","🍕","🎈","🏆","💫","🥳"};
    private static final int[]    COLORS  = {
            Color.parseColor("#FC8019"), Color.parseColor("#FF6B6B"),
            Color.parseColor("#FFD93D"), Color.parseColor("#6BCB77"),
            Color.parseColor("#4D96FF"), Color.parseColor("#FF6BDF"),
    };
    private static final Random RNG = new Random();

    /** Drop {@code count} emoji particles from the top of {@code parent}. */
    public static void burst(ViewGroup parent, int count) {
        int w = parent.getWidth();
        if (w == 0) { parent.post(() -> burst(parent, count)); return; }

        for (int i = 0; i < count; i++) {
            final TextView particle = new TextView(parent.getContext());
            particle.setText(EMOJIS[RNG.nextInt(EMOJIS.length)]);
            particle.setTextSize(18 + RNG.nextInt(14));

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            parent.addView(particle, lp);
            particle.setX(RNG.nextInt(w));
            particle.setY(-80f);

            float endX  = particle.getX() + RNG.nextInt(160) - 80;
            float endY  = parent.getHeight() + 80f;
            long delay   = RNG.nextInt(500);
            long duration = 1800 + RNG.nextInt(800);

            ObjectAnimator tx = ObjectAnimator.ofFloat(particle, "translationX", 0f,
                    RNG.nextInt(100) - 50f, RNG.nextInt(60) - 30f);
            ObjectAnimator ty = ObjectAnimator.ofFloat(particle, "y", particle.getY(), endY);
            ObjectAnimator rot = ObjectAnimator.ofFloat(particle, "rotation",
                    0f, RNG.nextInt(720) - 360f);
            ObjectAnimator fade = ObjectAnimator.ofFloat(particle, "alpha", 1f, 0f);
            fade.setStartDelay(duration / 2);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(tx, ty, rot, fade);
            set.setDuration(duration);
            set.setStartDelay(delay);
            set.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator a) { parent.removeView(particle); }
            });
            set.start();
        }
    }

    /** Scale-bounce a target view (great for the "Order Placed!" card). */
    public static void celebrate(View target, ViewGroup confettiParent) {
        // 1. Bounce the target
        ObjectAnimator sx = ObjectAnimator.ofFloat(target, "scaleX", 1f, 1.12f, 0.96f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(target, "scaleY", 1f, 1.12f, 0.96f, 1f);
        sx.setDuration(450); sy.setDuration(450);
        sx.start(); sy.start();
        // 2. Confetti burst
        target.postDelayed(() -> burst(confettiParent, 35), 100);
    }

    /** Show a floating success toast-style emoji over a view. */
    public static void floatEmoji(String emoji, View anchorView, ViewGroup parent) {
        Context ctx = parent.getContext();
        TextView tv = new TextView(ctx);
        tv.setText(emoji);
        tv.setTextSize(36);
        parent.addView(tv);

        int[] loc = new int[2];
        anchorView.getLocationInWindow(loc);
        int[] pLoc = new int[2];
        parent.getLocationInWindow(pLoc);
        tv.setX(loc[0] - pLoc[0] + anchorView.getWidth() / 2f - 30);
        tv.setY(loc[1] - pLoc[1] + anchorView.getHeight() / 2f - 30);
        tv.setAlpha(1f);

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator rise = ObjectAnimator.ofFloat(tv, "translationY", 0f, -180f);
        ObjectAnimator fade = ObjectAnimator.ofFloat(tv, "alpha", 1f, 0f);
        set.playTogether(rise, fade);
        set.setDuration(900);
        set.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator a) { parent.removeView(tv); }
        });
        set.start();
    }
}