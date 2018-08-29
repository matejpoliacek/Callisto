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

import com.chocolateam.galileopvt.PvtFragment;
import com.chocolateam.galileospaceship.R;

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
    private List<Satellite> msatList = new ArrayList<>();
    private SatelliteItemAdapter mAdapter;
    private LocInfo mLocationInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

        // Set the
        mAdapter = new SatelliteItemAdapter(msatList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mrecyclerView.setLayoutManager(mLayoutManager);
        mrecyclerView.setItemAnimator(new DefaultItemAnimator());
        mrecyclerView.setAdapter(mAdapter);

        prepareSatellitesData();
        return mView;
    }


    public void prepareSatellitesData() {
        if (PvtFragment.getNoisySatellites() != null) {
            msatList.clear();
            for (GnssMeasurement m : PvtFragment.getNoisySatellites()) {
                Satellite satellite = new Satellite(m.getSvid(), m.getConstellationType(), (int) (m.getCn0DbHz() * 0.15));
                msatList.add(satellite);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            prepareSatellitesData();
        }
    }

    public void setSatellites(List<Satellite> satellitesList){
        msatList.clear();
        msatList = satellitesList;
        mAdapter.notifyDataSetChanged();
    }

    public void setLatLong(float latitude, float longitude){
        mLocationInfo.setLatLong(latitude, longitude);
    }

    public void setAltitude(float altitude) {
        mLocationInfo.setAltitude(altitude);
    }

    public void setSpeed(float speed){
        mLocationInfo.setSpeed(speed);
    }


}
