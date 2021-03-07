package com.chocolateam.galileospaceship;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.galfins.gnss_compare.Constellations.SatelliteParameters;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lionel Garcia on 26/01/2018.
 */

public class RadarViewFragment extends Fragment {

    View mView;
    Boolean created = false;
    RadarView mRadar;
    MeasurementsInfo mMeasurementsInfo;
    GConstellationPanel mconstellationPannel;
    ImageButton constellationPannelButton;
    List<SatelliteParameters> satList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        mView = inflater.inflate(R.layout.radar_view, container, false);

        mconstellationPannel = mView.findViewById(R.id.constellation_panel_radar);
        constellationPannelButton = mView.findViewById(R.id.constellation_panel_button_radar);

        constellationPannelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mconstellationPannel.deploy();
            }
        });

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mRadar = mView.findViewById(R.id.radarview);
        mMeasurementsInfo = mView.findViewById(R.id.measurements);

        ImageView topArrow = mView.findViewById(R.id.swipe_arrow_radar_top);
        ImageView bottomArrow = mView.findViewById(R.id.swipe_arrow_radar_bottom);
        ImageView shipDisabled = mView.findViewById(R.id.ship_disabled);

        topArrow.setBackgroundResource(R.drawable.ship_arrow_anim_left);
        AnimationDrawable topArrow_anim = (AnimationDrawable) topArrow.getBackground();
        topArrow_anim.start();
        bottomArrow.setBackgroundResource(R.drawable.ship_arrow_anim_left);
        AnimationDrawable bottomArrow_anim = (AnimationDrawable) bottomArrow.getBackground();
        bottomArrow_anim.start();

        // Animate hologram
        GraphicsTools.pulseAnimate(shipDisabled, 2000);

        Bundle bundle = this.getArguments();
        // Hide "Ship disabled" if appropriate
        GraphicsTools.hideShipDisabledWarning(shipDisabled, bundle);

        mconstellationPannel.checkConstellationBundle(bundle);

        created = true;
    }

    public void addSatellites(List<SatelliteParameters> satellites) {
        satList.addAll(satellites);
    }

    public void updateSatellites() {
        mRadar.updateSatellites(satList);
    }

    public void resetSatellites() {
        satList = new ArrayList<>();
    }

    public void setSatCounts() {
        mMeasurementsInfo.setSatCounts(satList);
    }

    public void setTimeUTC(){
        mMeasurementsInfo.setTimeUTC();
    }

    public void setclock(Date initialtime){
        mMeasurementsInfo.setTimeClock(initialtime);
    }

    public void setLatLngXYZ(double lat , double lng, double ECEF_X, double ECEF_Y, double ECEF_Z) {
        mRadar.setLatLngXYZ(lat, lng, ECEF_X, ECEF_Y, ECEF_Z);
    }

    public boolean isCreated() { return created; }
}

