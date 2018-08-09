package com.chocolateam.galileospaceship;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.chocolateam.galileospaceship.R;

/**
 * Created by Lionel Garcia on 18/01/2018.
 */

public class SatelliteInfoView extends LinearLayout{

    private View mView;
    private Animation mViewAnimation;
    private Context mContext;

    public SatelliteInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.satellite_info_view, this, true);

        mView = this.findViewById(R.id.main_layout);

        mViewAnimation = AnimationUtils.loadAnimation(mContext, R.anim.blink);
        mView.startAnimation(mViewAnimation);

        mView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mView.clearAnimation();
                mView.setVisibility(INVISIBLE);
            }
        });
    }

    public SatelliteInfoView(Context context) {
        this(context, null);
    }

    public void SweepOut(){

        Animation sweepOutanimation = AnimationUtils.loadAnimation(mContext, R.anim.sweep_out);
        mView.startAnimation(sweepOutanimation);
        mView.startAnimation(mViewAnimation);


    }

}
