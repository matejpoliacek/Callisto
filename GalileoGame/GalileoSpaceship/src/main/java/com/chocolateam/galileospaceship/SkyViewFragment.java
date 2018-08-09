package com.chocolateam.galileospaceship;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.chocolateam.galileospaceship.R;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class SkyViewFragment extends Fragment {

    View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.sky_view, container, false);

//        mView = getView().findViewById(R.id.main_layout);

        SatelliteInfoView satinfoview = mView.findViewById(R.id.satinfoview);
        SatView satview = mView.findViewById(R.id.satview);
        ImageView clouds = mView.findViewById(R.id.clouds);
        FrameLayout earthAndSat = mView.findViewById(R.id.earth_all);

        Animation ViewAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation_slow);
        clouds.startAnimation(ViewAnimation);

//        Animation EarthPoisitionAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.earth_sat_initial_position);
//        earthAndSat.startAnimation(EarthPoisitionAnimation);

        satview.setSatInfoView(satinfoview);

        return mView;
    }
}
