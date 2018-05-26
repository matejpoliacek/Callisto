package com.chocolateam.galileospaceship;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.lionelgarcia.galileospaceship.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class ListViewFragment extends Fragment {

    private View mView;
    private RecyclerView mrecyclerView;
    private GConstellationPanel mconstellationPannel;
    private ImageButton  mconstellationPannelButton;
    private List<Satellite> msatList = new ArrayList<>();
    private SatelliteItemAdapter mAdapter;

    private Collection mMeasurements;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.list_view, container, false);

        mrecyclerView = mView.findViewById(R.id.recycler_view);
        mconstellationPannel = mView.findViewById(R.id.constellation_panel);
        mconstellationPannelButton = mView.findViewById(R.id.constellation_panel_button);

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


    private void prepareSatellitesData() {

        Satellite satellite= new Satellite(582,5, 2);
        msatList.add(satellite);

        mAdapter.notifyDataSetChanged();
    }
}
