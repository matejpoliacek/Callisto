package com.chocolateam.galileospaceship;


import android.content.ComponentName;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.chocolateam.galileospaceship.R;
import com.galfins.gnss_compare.CalculationModule;
import com.galfins.gnss_compare.CalculationModulesArrayList;
import com.galfins.gnss_compare.Constellations.SatelliteParameters;
import com.galfins.gnss_compare.GNSSCoreServiceActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class SpaceshipViewActivity extends GNSSCoreServiceActivity {

    private final String TAG = this.getClass().getSimpleName();

    protected int locationFuncLevel = 0;

    static final int NUM_PANELS = 3;
    SpacecraftPagerAdapter mAdapter;
    ViewPager mPager;
    Date mInitialTime;

    private Thread listThread;

    static ListViewFragment mListViewFragment;
    static SkyViewFragment mSkyViewFragment;
    static RadarViewFragment mRadarViewFragment;

    public Observer shipUpdater = new Observer() {
        @Override
        public void update(final Observable o, Object arg) {

            Log.e(TAG, "-- observer tick");

            CalculationModulesArrayList CMArrayList = gnssBinder.getCalculationModules();

            for (final CalculationModule calculationModule : CMArrayList) {

                final String calcName = calculationModule.getName();
                final String currentConstellation = mListViewFragment.getSelectedConstellation();

                Log.e(TAG, "ConstSize: " + calculationModule.getConstellation().getVisibleConstellationSize());
                Log.e(TAG, "ConstName: " + calculationModule.getConstellation().getName());


                final List<SatelliteParameters> satellites;
                final List<SatelliteParameters> satellitesVisibleOnly;

                final List<SatelliteParameters> satellitesAll;

                satellites = calculationModule.getConstellation().getSatellites();
                satellitesVisibleOnly = calculationModule.getConstellation().getUnusedSatellites();
                Log.e(TAG, "Satellite list not empty? Used: " + String.valueOf(!satellites.isEmpty()) + " Unused but visible: " + String.valueOf(!satellitesVisibleOnly.isEmpty()));

                satellitesAll = calculationModule.getConstellation().getSatellites();
                satellitesAll.addAll(satellitesVisibleOnly);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {



                        if (!satellites.isEmpty()) {
                            /**
                            setSatellitesList(satellites);
                            setLatLongIndicator(calculationModule.getPose().getGeodeticLatitude(),
                                    calculationModule.getPose().getGeodeticLongitude());
                            setAltitudeIndicator(calculationModule.getPose().getGeodeticHeight());
                            **/
                            Log.e(TAG, "Switch string: " + currentConstellation + " " + calcName);
                            switch (currentConstellation + " " + calcName) {
                                case "GPS GPS":
                                    Log.e(TAG, "SATPOS gps: " + String.valueOf(satellites.size()));
                                case "Galileo Galileo":
                                    Log.e(TAG, "SATPOS galileo: " + String.valueOf(satellites.size()));
                                case "Galileo+GPS Galileo+GPS":
                                    if (satellites.size() >= 1) {
                                        if (satellites.get(0).getSatellitePosition() != null) {
                                            Log.e(TAG, "GALGPS-SPACESHIP: " + satellites.get(0).getSatellitePosition().toString());
                                        } else {
                                            Log.e(TAG, "GALGPS-SPACESHIP: Satellite positions are null");
                                        }
                                    }

                                    mSkyViewFragment.updateSatView(satellitesAll);
                                    if (mRadarViewFragment.created) {
                                        mRadarViewFragment.updateSatellites(satellitesAll);
                                    }

                                    Log.e(TAG, "SATPOS galgp: " + String.valueOf(satellitesAll.size()));


                                    //  if (satellites.size() > 0) {
                                    //  Log.e("SATPOS - pos lat", String.valueOf(satellites.get(0).getSatellitePosition().getGeodeticLatitude()));
                                    //  Log.e("SATPOS - pos long", String.valueOf(satellites.get(0).getSatellitePosition().getGeodeticLongitude()));
                                    //  }
                            }
                        }

                        int numberOfSat;

                        if (mRadarViewFragment.created) {
                            mRadarViewFragment.setTimeUTC();
                            mRadarViewFragment.setclock(mInitialTime);
                            switch (calcName) {
                                case "GPS":
                                    numberOfSat = calculationModule.getConstellation().getUsedConstellationSize();
                                    mRadarViewFragment.setSatCounts(calcName, numberOfSat);
                                case "Galileo":
                                    numberOfSat = calculationModule.getConstellation().getUsedConstellationSize();
                                    mRadarViewFragment.setSatCounts(calcName, numberOfSat);
                            }
                        }


                        if (mListViewFragment != null) {
                            double lat = calculationModule.getPose().getGeodeticLatitude();
                            double lng = calculationModule.getPose().getGeodeticLongitude();
                            double alt = calculationModule.getPose().getGeodeticHeight();

                            mListViewFragment.setLatLong(lat, lng);
                            mListViewFragment.setAltitude(alt);
                        }
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.spacecraft_pager);

        mListViewFragment = new ListViewFragment();
        mSkyViewFragment = new SkyViewFragment();
        mRadarViewFragment = new RadarViewFragment();

        mInitialTime = Calendar.getInstance().getTime();

        mAdapter = new SpacecraftPagerAdapter(getSupportFragmentManager());

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        locationFuncLevel = getIntent().getExtras().getInt("location_functionality");
        boolean isNavDefault = (locationFuncLevel == LOCATION_DEFAULT_NAV);


        Bundle bundle = new Bundle();
        bundle.putBoolean("isNavDefault", isNavDefault);
        mListViewFragment.setArguments(bundle);
        mSkyViewFragment.setArguments(bundle);
        mRadarViewFragment.setArguments(bundle);
    }

    public static class SpacecraftPagerAdapter extends FragmentStatePagerAdapter {
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

    public void setSatellitesList(List<SatelliteParameters> satellitesList){
        // Set the list of satellites
        // check Satellite class to see how to build a Satellite object from the Observer output
        mListViewFragment.setSatellites(satellitesList);
    }

    public void setLatLongIndicator(double latitude, double longitude){
        // Set Latitude and Longitude indicator (first screen on the Left Spaceship view)
        mListViewFragment.setLatLong(latitude, longitude);
    }

    public void setSpeedIndicator(float speed){
        // Set speed indicator (second screen on the Left Spaceship view)
        mListViewFragment.setSpeed(speed);
    }

    public void setAltitudeIndicator(double altitude){
        // Set Altitude indicator (third screen on the Left Spaceship view)
        mListViewFragment.setAltitude(altitude);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        super.onServiceConnected(name, binder);
        if (locationFuncLevel > LOCATION_DEFAULT_NAV) {
            gnssBinder.addObserver(shipUpdater);
            Log.e(TAG, "-- observer ADDED");
        }
    }

    @Override
    protected void onPause() {
        if (gnssBinder != null) {
            gnssBinder.removeObserver(shipUpdater);
            Log.e(TAG, "-- observer REMOVED");
        }
        super.onPause();
    }
}
