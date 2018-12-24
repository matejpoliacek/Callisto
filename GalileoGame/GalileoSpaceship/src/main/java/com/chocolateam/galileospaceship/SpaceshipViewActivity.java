package com.chocolateam.galileospaceship;


import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.os.Bundle;
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
import com.galfins.gnss_compare.Constellations.SatelliteParameters;
import com.galfins.gnss_compare.StartGNSSFragment;

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

public class SpaceshipViewActivity extends AppCompatActivity {

    static final int NUM_PANELS = 3;
    SpacecraftPagerAdapter mAdapter;
    ViewPager mPager;
    Date mInitialTime;

    private Thread listThread;

    static ListViewFragment mListViewFragment;
    static SkyViewFragment mSkyViewFragment;
    static RadarViewFragment mRadarViewFragment;

    /** TODO: Replace with service
    public Observer shipUpdater = new Observer() {
        @Override
        public void update(final Observable o, Object arg) {
            Log.e("OBSERVER - SPACESHIP", "-- observer tick");

            final String calcName = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getName();
            final String currentConstellation = mListViewFragment.getSelectedConstellation();

            Log.e("SPACESHIP - ConstSize", String.valueOf(((CalculationModule.CalculationModuleObservable) o).getParentReference().getConstellation().getUsedConstellationSize()));
            Log.e("SPACESHIP - ConstName", String.valueOf(((CalculationModule.CalculationModuleObservable) o).getParentReference().getConstellation().getName()));


            System.out.println(calcName);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    List<SatelliteParameters> satellites;
                    int numberOfSat;
                    satellites = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getConstellation().getSatellites();

                    if (!satellites.isEmpty()) {
                        setSatellitesList(satellites);
                        setLatLongIndicator(((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticLatitude(),
                                ((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticLongitude());
                        setAltitudeIndicator(((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticHeight());

                        switch (currentConstellation + " " + calcName) {
                            case "GPS GPS":
                                Log.e("SATPOS gps", String.valueOf(satellites.size()));
                            case "Galileo Galileo":
                                Log.e("SATPOS galileo", String.valueOf(satellites.size()));
                            case "Galileo+GPS Galileo+GPS":
                                if (satellites.size() >= 1) {
                                    if (satellites.get(0).getSatellitePosition() != null){
                                        Log.e("GALGPS-SPACESHIP", satellites.get(0).getSatellitePosition().toString());
                                    }
                                    else {
                                        Log.e("GALGPS-SPACESHIP", "Satellite positions are null");
                                    }
                                }

                                mSkyViewFragment.updateSatView(satellites);
                                if (mRadarViewFragment.created) {
                                    mRadarViewFragment.updateSatellites(satellites);
                                }

                                Log.e("SATPOS galgps", String.valueOf(satellites.size()));


                               //  if (satellites.size() > 0) {
                               //  Log.e("SATPOS - pos lat", String.valueOf(satellites.get(0).getSatellitePosition().getGeodeticLatitude()));
                               //  Log.e("SATPOS - pos long", String.valueOf(satellites.get(0).getSatellitePosition().getGeodeticLongitude()));
                               //  }
                        }
                    }



                    if (mRadarViewFragment.created) {
                        mRadarViewFragment.setTimeUTC();
                        mRadarViewFragment.setclock(mInitialTime);
                        switch (calcName) {
                            case "GPS":
                                numberOfSat = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getConstellation().getUsedConstellationSize();
                                mRadarViewFragment.setSatCounts(calcName, numberOfSat);
                            case "Galileo":
                                numberOfSat = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getConstellation().getUsedConstellationSize();
                                mRadarViewFragment.setSatCounts(calcName, numberOfSat);
                        }
                    }


                    if (mListViewFragment != null) {
                        double lat = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticLatitude();
                        double lng = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticLongitude();
                        double alt = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticHeight();

                        mListViewFragment.setLatLong(lat, lng);
                        mListViewFragment.setAltitude(alt);
                    }

                }
            });
        }
    };
     **/

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

        // TODO: Replace with service
        //StartGNSSFragment.gnssInit.addObservers(shipUpdater);
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
}
