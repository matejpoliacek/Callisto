package com.chocolateam.galileospaceship;

import android.Manifest;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.chocolateam.galileospaceship.R;
import com.galfins.gnss_compare.Constellations.SatelliteParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class ListViewFragment extends Fragment implements Runnable {

    private View mView;
    private RecyclerView mrecyclerView;
    private GConstellationPanel mconstellationPannel;
    private ImageButton  mconstellationPannelButton;
    private List<SatelliteParameters> msatList = new ArrayList<>();
    private SatelliteItemAdapter mAdapter;
    private LocInfo mLocationInfo;

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

        return mView;
    }

    public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void setSatellites(List<SatelliteParameters> satellitesList){
        msatList = satellitesList;
        mAdapter.setSatelliteList(satellitesList);
        System.out.println("Number of sta in the actual fucking view: " + Integer.toString(msatList.size()));
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
        return mconstellationPannel.getActive();
    }
}
