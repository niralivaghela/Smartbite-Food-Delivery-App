package com.smartbite.utils;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
public class AnimationHelper {
    public static void bounceClick(View view){
        ObjectAnimator sx=ObjectAnimator.ofFloat(view,"scaleX",1f,0.9f,1f);
        ObjectAnimator sy=ObjectAnimator.ofFloat(view,"scaleY",1f,0.9f,1f);
        sx.setDuration(300); sy.setDuration(300);
        sx.setInterpolator(new OvershootInterpolator());
        sy.setInterpolator(new OvershootInterpolator());
        AnimatorSet set=new AnimatorSet();
        set.playTogether(sx,sy); set.start();
    }
    public static void slideInFromBottom(View view){
        view.setTranslationY(300f); view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        ObjectAnimator t=ObjectAnimator.ofFloat(view,"translationY",300f,0f);
        ObjectAnimator f=ObjectAnimator.ofFloat(view,"alpha",0f,1f);
        t.setDuration(400); f.setDuration(400);
        t.setInterpolator(new DecelerateInterpolator());
        AnimatorSet set=new AnimatorSet();
        set.playTogether(t,f); set.start();
    }
    public static void fadeIn(View view,int duration){
        view.setAlpha(0f); view.setVisibility(View.VISIBLE);
        ObjectAnimator f=ObjectAnimator.ofFloat(view,"alpha",0f,1f);
        f.setDuration(duration); f.start();
    }
    public static void pulse(View view){
        ObjectAnimator sx=ObjectAnimator.ofFloat(view,"scaleX",1f,1.2f,1f);
        ObjectAnimator sy=ObjectAnimator.ofFloat(view,"scaleY",1f,1.2f,1f);
        sx.setDuration(600); sy.setDuration(600);
        sx.setRepeatCount(ValueAnimator.INFINITE);
        sy.setRepeatCount(ValueAnimator.INFINITE);
        AnimatorSet set=new AnimatorSet();
        set.playTogether(sx,sy); set.start();
    }
    public static void shake(View view){
        ObjectAnimator shake=ObjectAnimator.ofFloat(view,"translationX",
            0f,25f,-25f,25f,-25f,15f,-15f,6f,-6f,0f);
        shake.setDuration(600); shake.start();
    }
    public static void staggeredEntrance(android.view.ViewGroup container){
        for(int i=0;i<container.getChildCount();i++){
            View child=container.getChildAt(i);
            child.setAlpha(0f); child.setTranslationY(50f);
            ObjectAnimator fi=ObjectAnimator.ofFloat(child,"alpha",0f,1f);
            ObjectAnimator su=ObjectAnimator.ofFloat(child,"translationY",50f,0f);
            fi.setDuration(300); su.setDuration(300);
            fi.setStartDelay(i*80L); su.setStartDelay(i*80L);
            AnimatorSet set=new AnimatorSet();
            set.playTogether(fi,su); set.start();
        }
    }
}