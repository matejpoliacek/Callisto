package com.chocolateam.galileospaceship;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.galfins.gnss_compare.Constellations.SatelliteParameters;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        mView = inflater.inflate(R.layout.radar_view, container, false);
        mconstellationPannel = mView.findViewById(R.id.constellation_panel);
        ImageButton constellationPannelButton = mView.findViewById(R.id.constellation_panel_button);
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

        created = true;
    }

    public void updateSatellites(List<SatelliteParameters> satellites){
        mRadar.updateSatellites(satellites);
    }

    public void setSatCounts(String constellationType, int numberOfSat) {
        mMeasurementsInfo.setSatCounts(constellationType, numberOfSat);
    }

    public void setTimeUTC(){
        mMeasurementsInfo.setTimeUTC();
    }

    public void setclock(Date initialtime){
        mMeasurementsInfo.setTimeClock(initialtime);
    }
}

