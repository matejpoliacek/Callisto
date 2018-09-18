package com.chocolateam.galileomap;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.galfins.gnss_compare.CalculationModule;
import com.galfins.gnss_compare.StartGNSSFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Observable;
import java.util.Observer;

public class MapsOnlyActivity extends MapsActivity implements OnMapReadyCallback {

     /** MAP ONLY VARIABLES **/
    private View checkboxLayout;
    private CheckBox checkBoxGPS;
    private CheckBox checkBoxGAL;

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
            Log.e("MAP - OBSERVER", "-- observer tick");
            Log.e("Observer tick: " , ((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().toString());

            final double lat = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticLatitude();
            final double lng = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticLongitude();

            final String constName = ((CalculationModule.CalculationModuleObservable) o).getParentReference().getConstellation().getName();
            Log.e("MAP-CONST", constName);

            // SET MAP LOCATION, markers and default
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (constName.equals("GPS")) {
                        GPSpoint = new LatLng(lat, lng);
                        Log.e("MAP-GPSPoint", GPSpoint.toString());
                        mGPSMarker = processMarker(checkBoxGPS.isChecked(), mGPSMarker, GPSpoint, R.drawable.gps_marker);
                    } else if (constName.equals("Galileo")) {
                        GalileoPoint = new LatLng(lat, lng);
                        Log.e("MAP-GALPoint", GalileoPoint.toString());
                        mGALMarker = processMarker(checkBoxGAL.isChecked(), mGALMarker, GalileoPoint, R.drawable.gal_marker);
                    } else if (constName.equals("Galileo + GPS")) {
                        GalGPSPoint = new LatLng(lat, lng);
                        Log.e("MAP-GALGPSPoint", GalGPSPoint.toString());
                        mGALGPSMarker = processMarker(checkBoxGPS.isChecked(), checkBoxGAL.isChecked(), mGALGPSMarker, GalGPSPoint, R.drawable.galgps_marker);
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // run the rest of the onCreate method from superclass
        super.onCreate(savedInstanceState);

        checkBoxGPS = findViewById(R.id.checkBoxGPS);
        checkBoxGAL = findViewById(R.id.checkBoxGAL);

        mapBottomPanel = findViewById(R.id.map_bottom_panel);
        mapBottomPanel.setVisibility(View.VISIBLE);

        checkboxLayout = findViewById(R.id.checkboxLayout);
        checkboxLayout.setVisibility(View.VISIBLE);

        StartGNSSFragment.gnssInit.addObservers(mapMarkerUpdater);
    }

    @Override
    protected void onPause() {
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
}
