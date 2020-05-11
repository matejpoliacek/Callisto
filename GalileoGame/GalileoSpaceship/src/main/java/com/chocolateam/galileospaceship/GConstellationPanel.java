package com.chocolateam.galileospaceship;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.galfins.gnss_compare.GNSSCoreServiceActivity;

/**
 * Created by lgr on 21/01/2018.
 */

public class GConstellationPanel extends RelativeLayout {

    private Animation mViewAnimation;
    private Context mContext;
    private View mView;

    private RadioButton mGalileoSwitch;
    private RadioButton  mGpsSwitch;
    private RadioButton mGalGPSSwitch;

    private ImageButton mOKButton;
    private ImageButton mCANCELButton;

    private String selectedConst = "none";

    private boolean bGPSOnly = false;
    private boolean bDefaultOnly = false;

    public GConstellationPanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.constellation_pannel, this, true);

        mView = this.findViewById(R.id.main_layout);
        mOKButton = this.findViewById(R.id.ok_button);
        mCANCELButton = this.findViewById(R.id.cancel_button);

        mGalileoSwitch = this.findViewById(R.id.galileo_switch);
        mGpsSwitch = this.findViewById(R.id.gps_switch);
        mGalGPSSwitch = this.findViewById(R.id.all_switch);

        mViewAnimation = AnimationUtils.loadAnimation(mContext, R.anim.constellation_panel_initial_position);
        mView.startAnimation(mViewAnimation);

        mOKButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectedConst = chooseConstellation();
                retract();
            }
        });

        mCANCELButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                retract();
            }
        });

        setActive(false);

        // By default, use Gal+GPS constellation
        selectedConst = GNSSCoreServiceActivity.GalGPSConstName;

    }

    public GConstellationPanel(Context context) {
        this(context, null);
    }

    public void deploy(){
        mViewAnimation = AnimationUtils.loadAnimation(mContext, R.anim.constellation_panel_slide_down);

        // This is to hide the constellations panel buttons only if slide down animation is done
        // This way we can still see the buttons while the panel is sliding up
        // INFO : if setActive(false) is not called, buttons are still clickable even if the
        //        panel is retracted
        mViewAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // If we're using GPS only, disable buttons, otherwise, enable them on panel deployment
                setActive(true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mView.startAnimation(mViewAnimation);
        mView.setOnTouchListener(new OnSwipeTouchListener(mContext) {
            public void onSwipeTop() {
                selectedConst = chooseConstellation();
                retract();
            }
        });
    }

    public void retract(){
        mViewAnimation = AnimationUtils.loadAnimation(mContext, R.anim.constellation_panel_slide_up);

        // This is to show the constellations panel buttons when slide up animation start
        mViewAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                setActive(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mView.startAnimation(mViewAnimation);

        mView.setOnTouchListener(new OnSwipeTouchListener(mContext) {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
    }

    public void setActive(Boolean active){

        boolean status = active && !bGPSOnly && !bDefaultOnly;

        mGalileoSwitch.setEnabled(status);
        mGalGPSSwitch.setEnabled(status);
        mGpsSwitch.setEnabled(status);

        if (bDefaultOnly) {
            mGalileoSwitch.setChecked(false);
            mGpsSwitch.setChecked(false);
            mGalGPSSwitch.setChecked(false);
        } else {
            if (selectedConst.equals(GNSSCoreServiceActivity.GalConstName)) {
                mGalileoSwitch.setChecked(true);
            } else if (selectedConst.equals(GNSSCoreServiceActivity.GPSConstName)) {
                mGpsSwitch.setChecked(true);
            } else if (selectedConst.equals(GNSSCoreServiceActivity.GalGPSConstName)) {
                mGalGPSSwitch.setChecked(true);
            }
        }

        if(active) {
            mCANCELButton.setVisibility(VISIBLE);
            // Only make OK button visible, if we're using more than GPS only
            if (status) {
                mOKButton.setVisibility(VISIBLE);
            }
        }
        else {
            mCANCELButton.setVisibility(INVISIBLE);
            mOKButton.setVisibility(INVISIBLE);
        }
    }

    public String getSelectedConst(){
       return selectedConst;
    }

    private String chooseConstellation() {
        if (mGalileoSwitch.isChecked()){
            return GNSSCoreServiceActivity.GalConstName;
        }
        else if (mGpsSwitch.isChecked()){
            return GNSSCoreServiceActivity.GPSConstName;
        }
        else {
            return GNSSCoreServiceActivity.GalGPSConstName;
        }
    }

    public void hideGpsOnlyWarning() {
        mView.findViewById(R.id.gps_only).setVisibility(GONE);
    }

    public void setGPSOnly(boolean gpsOnly) {
        this.bGPSOnly = gpsOnly;
        selectedConst = GNSSCoreServiceActivity.GPSConstName;
    }

    public void setDefaultOnly(boolean defaultOnly) {
        this.bDefaultOnly = defaultOnly;
    }

    public void checkConstellationBundle(Bundle bundle) {
        if (GraphicsTools.checkIfGPSOnly(bundle)) {
            bGPSOnly = true;
        } else if (GraphicsTools.checkIfDefaultOnly(bundle)) {
            bDefaultOnly = true;
        }

        if (!bGPSOnly) {
            hideGpsOnlyWarning();
        }
    }
}
