package com.chocolateam.galileomap;


import android.content.ComponentName;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.galfins.gnss_compare.CalculationModule;
import com.galfins.gnss_compare.CalculationModulesArrayList;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.lang.reflect.Type;
import java.util.Observable;
import java.util.Observer;

public class MapsOnlyActivity extends MapsActivity implements OnMapReadyCallback {

    private final String TAG = this.getClass().getSimpleName();

     /** MAP ONLY VARIABLES **/
    private View checkboxLayout;
    private CheckBox checkBoxGPS;
    private CheckBox checkBoxGAL;

    private ImageView GPSLegend;
    private ImageView GALLegend;


    private MapPanel mapBottomPanel;

    LatLng GPSpoint = null;
    LatLng GalileoPoint = null;
    LatLng GalGPSPoint = null;

    private Marker mGPSMarker;
    private Marker mGALMarker;
    private Marker mGALGPSMarker;

    public Observer mapMarkerUpdater = new Observer() {
        @Override
        public void update(final Observable o, Object arg) {

            CalculationModulesArrayList CMArrayList = gnssBinder.getCalculationModules();

            for(CalculationModule calculationModule : CMArrayList) {

                Log.e(TAG, "-- observer tick");
                Log.e(TAG, "Observer tick: " + (calculationModule.getPose().toString()));

                final double lat = calculationModule.getPose().getGeodeticLatitude();
                final double lng = calculationModule.getPose().getGeodeticLongitude();

                final String constName = calculationModule.getConstellation().getName();
                Log.e(TAG, "Constellation Name: " + constName);

                // SET MAP LOCATION, markers and default
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (constName.equals(GPSConstName)) {
                            GPSpoint = new LatLng(lat, lng);
                            Log.e(TAG, "MAP-GPSPoint: " + GPSpoint.toString());
                            mGPSMarker = processMarker(checkBoxGPS.isChecked(), mGPSMarker, GPSpoint, R.drawable.gps_marker);

                        } else if (constName.equals(GalConstName)) {
                            GalileoPoint = new LatLng(lat, lng);
                            Log.e(TAG, "MAP-GALPoint: " + GalileoPoint.toString());
                            mGALMarker = processMarker(checkBoxGAL.isChecked(), mGALMarker, GalileoPoint, R.drawable.gal_marker);

                        } else if (constName.equals(GalGPSConstName)) {
                            GalGPSPoint = new LatLng(lat, lng);
                            Log.e(TAG, "MAP-GALGPSPoint: " + GalGPSPoint.toString());
                            mGALGPSMarker = processMarker(checkBoxGPS.isChecked(), checkBoxGAL.isChecked(), mGALGPSMarker, GalGPSPoint, R.drawable.galgps_marker);
                        }
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // run the rest of the onCreate method from superclass
        super.onCreate(savedInstanceState);

        checkBoxGPS = findViewById(R.id.checkBoxGPS);
        checkBoxGAL = findViewById(R.id.checkBoxGAL);

        GPSLegend = findViewById(R.id.GPSLegend);
        GALLegend = findViewById(R.id.GALLegend);

        mapBottomPanel = findViewById(R.id.map_bottom_panel);
        mapBottomPanel.setVisibility(View.VISIBLE);

        checkboxLayout = findViewById(R.id.checkboxLayout);
        checkboxLayout.setVisibility(View.VISIBLE);

        if (locationFuncLevel < LOCATION_FULL_FUNC) {
            checkBoxGAL.setEnabled(false);
            checkBoxGAL.setAlpha(0.5f);

            if (locationFuncLevel < LOCATION_GPS_ONLY) {
                checkBoxGPS.setEnabled(false);
                checkBoxGPS.setAlpha(0.5f);;

            }
        }
    }

    @Override
    protected void onPause() {
        if (gnssBinder != null) {
            gnssBinder.removeObserver(mapMarkerUpdater);
            Log.e(TAG, "-- observer REMOVED");
        }
        super.onPause();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    public void backToMenu(View view) {
        finish();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        super.onServiceConnected(name, binder);
        gnssBinder.addObserver(mapMarkerUpdater);
        Log.e(TAG, "-- observer ADDED");
    }
}
