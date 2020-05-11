package com.chocolateam.galileospaceship;

import android.graphics.drawable.AnimationDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.galfins.gnss_compare.Constellations.SatelliteParameters;

import java.util.List;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class SkyViewFragment extends Fragment {

    View mView;
    SatView mSatView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        mView = inflater.inflate(R.layout.sky_view, container, false);

        SatelliteInfoView satinfoview = mView.findViewById(R.id.satinfoview);
        mSatView = mView.findViewById(R.id.satview);
        ImageView clouds = mView.findViewById(R.id.clouds);
        ConstraintLayout earthAndSat = mView.findViewById(R.id.earth_all);

        Animation ViewAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation_slow);
        clouds.startAnimation(ViewAnimation);

        mSatView.setSatInfoView(satinfoview);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ImageView topRightArrow = mView.findViewById(R.id.swipe_arrow_right_top);
        ImageView bottomRightArrow = mView.findViewById(R.id.swipe_arrow_right_bottom);
        ImageView topLeftArrow = mView.findViewById(R.id.swipe_arrow_left_top);
        ImageView bottomLeftArrow = mView.findViewById(R.id.swipe_arrow_left_bottom);
        ImageView shipDisabled = mView.findViewById(R.id.ship_disabled);

        //right arrows animation
        topRightArrow.setBackgroundResource(R.drawable.ship_arrow_anim_right);
        AnimationDrawable topArrow_anim_right = (AnimationDrawable) topRightArrow.getBackground();
        topArrow_anim_right.start();
        bottomRightArrow.setBackgroundResource(R.drawable.ship_arrow_anim_right);
        AnimationDrawable bottomArrow_anim_right = (AnimationDrawable) bottomRightArrow.getBackground();
        bottomArrow_anim_right.start();

        //left arrows animation
        topLeftArrow.setBackgroundResource(R.drawable.ship_arrow_anim_left);
        AnimationDrawable topArrow_anim_left = (AnimationDrawable) topLeftArrow.getBackground();
        topArrow_anim_left.start();
        bottomLeftArrow.setBackgroundResource(R.drawable.ship_arrow_anim_left);
        AnimationDrawable bottomArrow_anim_left = (AnimationDrawable) bottomLeftArrow.getBackground();
        bottomArrow_anim_left.start();

        Bundle bundle = this.getArguments();
        // Hide "Ship disabled" if appropriate
        GraphicsTools.hideShipDisabledWarning(shipDisabled, bundle);

        // Animate arrows and hologram
//        GraphicsTools.pulseAnimate(topRightArrow, 750);
//        GraphicsTools.pulseAnimate(bottomRightArrow, 750);
//        GraphicsTools.pulseAnimate(topLeftArrow, 750);
//        GraphicsTools.pulseAnimate(bottomLeftArrow, 750);
        GraphicsTools.pulseAnimate(shipDisabled, 2000);
    }

    public void updateSatView(List <SatelliteParameters> satellites){
        mSatView.updateSatView(satellites);
    }
}
