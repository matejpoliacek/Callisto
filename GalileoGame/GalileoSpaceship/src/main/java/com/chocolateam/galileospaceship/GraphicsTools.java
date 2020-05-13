package com.chocolateam.galileospaceship;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by Matej on 03/02/2019.
 */

public class GraphicsTools {

    public static void hideShipDisabledWarning(View view,  Bundle bundle) {
        boolean isNavDefault = checkIfDefaultOnly(bundle);

        Log.e("GRAPH-TOOLS", "Hide Spaceship? " + !isNavDefault);

        if (!isNavDefault) {
            view.setVisibility(View.GONE);
        }
    }

    public static boolean checkIfDefaultOnly(Bundle bundle) {
        boolean isNavDefault = true;

        if (bundle != null) {
            isNavDefault = bundle.getBoolean("isNavDefault", true);
        }

        return isNavDefault;
    }

    public static boolean checkIfGPSOnly(Bundle bundle) {
        boolean isGpsOnly = false;

        if (bundle != null) {
            isGpsOnly = bundle.getBoolean("isNavGpsOnly", false);
        }

        return isGpsOnly;
    }

    public static void pulseAnimate(View view, int speed) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.5f);
        fadeOut.setDuration(speed);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1.0f);
        fadeIn.setDuration(speed);

        AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        final AnimatorSet mAnimationSet_final = mAnimationSet;

        mAnimationSet_final.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet_final.start();
            }
        });

        mAnimationSet_final.start();
    }
}
