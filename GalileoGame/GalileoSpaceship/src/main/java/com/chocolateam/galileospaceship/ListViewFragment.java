package com.chocolateam.galileospaceship;

import android.graphics.drawable.AnimationDrawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.galfins.gnss_compare.Constellations.SatelliteParameters;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class ListViewFragment extends Fragment/* implements Runnable*/ {

    private final String TAG = this.getClass().getSimpleName();

    private View mView;
    private RecyclerView mrecyclerView;
    private GConstellationPanel mconstellationPannel;
    private ImageButton  mconstellationPannelButton;
    private List<SatelliteParameters> msatList = new ArrayList<>();
    private SatelliteItemAdapter mAdapter;
    private LocInfo mLocationInfo;
    public boolean created = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.list_view, container, false);

        mrecyclerView = mView.findViewById(R.id.recycler_view);
        mconstellationPannel = mView.findViewById(R.id.constellation_panel);
        mconstellationPannelButton = mView.findViewById(R.id.constellation_panel_button);
        mLocationInfo = mView.findViewById(R.id.location_info);

        mconstellationPannelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mconstellationPannel.deploy();
            }
        });

        mAdapter = new SatelliteItemAdapter(msatList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mrecyclerView.setLayoutManager(mLayoutManager);
        mrecyclerView.setItemAnimator(new DefaultItemAnimator());
        mrecyclerView.setAdapter(mAdapter);

        created = true;

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ImageView topArrow = mView.findViewById(R.id.swipe_arrow_list_top);
        ImageView bottomArrow = mView.findViewById(R.id.swipe_arrow_list_bottom);
        ImageView shipDisabled = mView.findViewById(R.id.ship_disabled);

        topArrow.setBackgroundResource(R.drawable.ship_arrow_anim_right);
        AnimationDrawable topArrow_anim_right = (AnimationDrawable) topArrow.getBackground();
        topArrow_anim_right.start();
        bottomArrow.setBackgroundResource(R.drawable.ship_arrow_anim_right);
        AnimationDrawable bottomArrow_anim_right = (AnimationDrawable) bottomArrow.getBackground();
        bottomArrow_anim_right.start();

        // Animate hologram
        GraphicsTools.pulseAnimate(shipDisabled, 2000);

        Bundle bundle = this.getArguments();
        // Hide "Ship disabled" if appropriate
        GraphicsTools.hideShipDisabledWarning(shipDisabled, bundle);

        mconstellationPannel.checkConstellationBundle(bundle);
    }

    public void setSatellites(List<SatelliteParameters> satellitesList){
        msatList = satellitesList;
        mAdapter.setSatelliteList(satellitesList);
        Log.e(TAG, "Number of sats in the actual view: " + msatList.size());
        mrecyclerView.getRecycledViewPool().clear();
        mAdapter.notifyDataSetChanged();
    }

    public void setLatLong(double latitude, double longitude){
        mLocationInfo.setLatLong(latitude, longitude);
    }

    public void setAltitude(double altitude) {
        mLocationInfo.setAltitude(altitude);
    }

    public void setSpeed(float speed){
        mLocationInfo.setSpeed(speed);
    }

    public String getSelectedConstellation(){
        return mconstellationPannel.getSelectedConst();
    }
}
