package com.example.lionelgarcia.galapptest2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Satellite> msatList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SatelliteItemAdapter mAdapter;
    private ImageButton mconstellationPannelButton;
    private GConstellationPanel mconstellationPannel;

    private Collection mMeasurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //listView();
        satView();
//        radarView();

    }

    private void listView() {

        setContentView(R.layout.activity_main_list_view);

        recyclerView = findViewById(R.id.recycler_view);
        mconstellationPannel = findViewById(R.id.constellation_panel);
        mconstellationPannelButton = findViewById(R.id.constellation_panel_button);

        mconstellationPannelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mconstellationPannel.deploy();
            }
        });

        // Set the
        mAdapter = new SatelliteItemAdapter(msatList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        prepareSatellitesData();

    }

    private void radarView() {

        setContentView(R.layout.activity_main_radar_view);

        mconstellationPannel = findViewById(R.id.constellation_panel);
        mconstellationPannelButton = findViewById(R.id.constellation_panel_button);

        mconstellationPannelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mconstellationPannel.deploy();
            }
        });

    }

    private void satView() {

        setContentView(R.layout.sky_view);

        SatelliteInfoView satinfoview = findViewById(R.id.satinfoview);
        SatView satview = findViewById(R.id.satview);
        ImageView clouds = findViewById(R.id.clouds);

        Animation ViewAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation_slow);
        clouds.startAnimation(ViewAnimation);

        satview.setSatInfoView(satinfoview);
    }

    private void prepareSatellitesData() {

        Satellite satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,3, 4);
        msatList.add(satellite);

        satellite= new Satellite(0,4, 6);
        msatList.add(satellite);

        satellite= new Satellite(0,5, 0);
        msatList.add(satellite);

        satellite= new Satellite(45,6, 1);
        msatList.add(satellite);

        satellite= new Satellite(102,1, 5);
        msatList.add(satellite);

        satellite= new Satellite(209,1, 2);
        msatList.add(satellite);

        satellite= new Satellite(10,4, 3);
        msatList.add(satellite);

        satellite= new Satellite(0,2, 5);
        msatList.add(satellite);

        satellite= new Satellite(1,4, 0);
        msatList.add(satellite);

        satellite= new Satellite(28,0, 10);
        msatList.add(satellite);

        satellite= new Satellite(23,2, 0);
        msatList.add(satellite);

        satellite= new Satellite(21,0, 0);
        msatList.add(satellite);
        satellite= new Satellite(22,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);
        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);

        satellite= new Satellite(0,0, 0);
        msatList.add(satellite);


        mAdapter.notifyDataSetChanged();
    }
}
