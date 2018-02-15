package com.example.lionelgarcia.galapptest2;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

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

        satellite= new Satellite(4580,3, 4);
        msatList.add(satellite);

        satellite= new Satellite(89,4, 6);
        msatList.add(satellite);

        satellite= new Satellite(38,5, 1);
        msatList.add(satellite);

        satellite= new Satellite(45,6, 1);
        msatList.add(satellite);

        satellite= new Satellite(102,1, 5);
        msatList.add(satellite);

        satellite= new Satellite(209,1, 2);
        msatList.add(satellite);

        satellite= new Satellite(10,4, 3);
        msatList.add(satellite);

        satellite= new Satellite(5,2, 5);
        msatList.add(satellite);

        satellite= new Satellite(1,4, 1);
        msatList.add(satellite);

        satellite= new Satellite(28,3, 10);
        msatList.add(satellite);

        satellite= new Satellite(23,2, 5);
        msatList.add(satellite);

        satellite= new Satellite(21,6, 4);
        msatList.add(satellite);
        satellite= new Satellite(22,3, 0);
        msatList.add(satellite);


        mAdapter.notifyDataSetChanged();
    }
}
