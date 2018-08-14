package com.chocolateam.galileospaceship;


import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.chocolateam.galileospaceship.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class SpaceshipViewActivity extends AppCompatActivity {

    static final int NUM_PANELS = 3;
    SpacecraftPagerAdapter mAdapter;
    ViewPager mPager;

    private Thread listThread;

    static ListViewFragment mListViewFragment;
    static SkyViewFragment mSkyViewFragment;
    static RadarViewFragment mRadarViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.spacecraft_pager);

        mListViewFragment = new ListViewFragment();
        mSkyViewFragment = new SkyViewFragment();
        mRadarViewFragment = new RadarViewFragment();

        // Put fragment on thread and run it
        //listThread = new Thread(mListViewFragment);
        //listThread.start();

        mAdapter = new SpacecraftPagerAdapter(getSupportFragmentManager());

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
//        mPager.setOffscreenPageLimit(2);

        mPager.setCurrentItem(1);
    }

    public static class SpacecraftPagerAdapter extends FragmentPagerAdapter {
        public SpacecraftPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_PANELS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mListViewFragment;
                case 1:
                    return mSkyViewFragment;
                case 2:
                    return mRadarViewFragment;
                default:
                    return new Fragment();
            }
        }
    }

    public void backToMenu(View view) {
        finish();
    }

    // Methods to fill the UI

        // Left panel - mListViewFragment

    public void setSatellitesList(List<Satellite> satellitesList){
        // Set the list of satellites
        // check Satellite class to see how to build a Satellite object from the Observer output
        mListViewFragment.setSatellites(satellitesList);
    }

    public void setLatLongIndicator(float latitude, float longitude){
        // Set Latitude and Longitude indicator (first screen on the Left Spaceship view)
        mListViewFragment.setLatLong(latitude, longitude);
    }

    public void setSpeedIndicator(float speed){
        // Set speed indicator (second screen on the Left Spaceship view)
        mListViewFragment.setSpeed(speed);
    }

    public void setAltitudeIndicator(float altitude){
        // Set Altitude indicator (third screen on the Left Spaceship view)
        mListViewFragment.setAltitude(altitude);
    }

        // Right pannel - mRadarViewFragment

    // TO DO
}
