package com.chocolateam.galileomap;


import android.location.Location;
import android.location.LocationListener;
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
import com.google.android.gms.maps.model.MarkerOptions;

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

    private Marker mGPSMarker;
    private Marker mGALMarker;

    public Observer mapMarkerUpdater = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Log.e("OBSERVER", "-- observer tick");
            System.out.println("Observer tick: " + ((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().toString());
            System.out.println(((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticLatitude());
            System.out.println(((CalculationModule.CalculationModuleObservable) o).getParentReference().getPose().getGeodeticLongitude());

            ((CalculationModule.CalculationModuleObservable) o).getParentReference().getConstellation().toString();

            //TODO: get location
            //TODO: update map

            // SET MAP LOCATION, markers and default
            if (checkBoxGPS.isChecked()){
                // TODO
                LatLng point = null; // new LatLng(PvtFragment.getUserLatitudeDegreesGPS(), PvtFragment.getUserLongitudeDegreesGPS());
                LatLng GPSpoint = null;
                if (mGPSMarker == null) {
                    mGPSMarker = mMap.addMarker(new MarkerOptions().position(point));
                    mGPSMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                } else {
                    mGPSMarker.remove();
                    mGPSMarker = mMap.addMarker(new MarkerOptions().position(point));
                    mGPSMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
            } else if (!checkBoxGPS.isChecked() && mGPSMarker != null) {
                mGPSMarker.remove();
            }

            if (checkBoxGAL.isChecked()){
                //TODO
                LatLng point = null; // new LatLng(PvtFragment.getUserLatitudeDegreesGalileo(),PvtFragment.getUserLongitudeDegreesGalileo());

                if (mGALMarker == null) {
                    mGALMarker = mMap.addMarker(new MarkerOptions().position(point));
                    mGALMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                } else {
                    mGALMarker.remove();
                    mGALMarker = mMap.addMarker(new MarkerOptions().position(point));
                    mGALMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }
            } else if (!checkBoxGAL.isChecked() && mGALMarker != null) {
                mGALMarker.remove();
            }


            //TODO: would it be better to put this in the subclasses, or at least inherit from a generic observer
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // run the rest of the onCreate method from superclass
        super.onCreate(savedInstanceState);

        mapBottomPanel = findViewById(R.id.map_bottom_panel);
        mapBottomPanel.setVisibility(View.VISIBLE);

//       checkBoxGPS = mapBottomPanel.getCheckBoxGPS();
//       checkBoxGAL = mapBottomPanel.getCheckBoxGAL();

        /** Location Manager **/ /**
        mLocationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                // SET MAP LOCATION, markers and default
                if (checkBoxGPS.isChecked()){
                    // TODO
                    LatLng point = null; // new LatLng(PvtFragment.getUserLatitudeDegreesGPS(), PvtFragment.getUserLongitudeDegreesGPS());

                    if (mGPSMarker == null) {
                        mGPSMarker = mMap.addMarker(new MarkerOptions().position(point));
                        mGPSMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    } else {
                        mGPSMarker.remove();
                        mGPSMarker = mMap.addMarker(new MarkerOptions().position(point));
                        mGPSMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }
                } else if (!checkBoxGPS.isChecked() && mGPSMarker != null) {
                    mGPSMarker.remove();
                }

                if (checkBoxGAL.isChecked()){
                    //TODO
                    LatLng point = null; // new LatLng(PvtFragment.getUserLatitudeDegreesGalileo(),PvtFragment.getUserLongitudeDegreesGalileo());

                    if (mGALMarker == null) {
                        mGALMarker = mMap.addMarker(new MarkerOptions().position(point));
                        mGALMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    } else {
                        mGALMarker.remove();
                        mGALMarker = mMap.addMarker(new MarkerOptions().position(point));
                        mGALMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    }
                } else if (!checkBoxGAL.isChecked() && mGALMarker != null) {
                    mGALMarker.remove();
                }

                mLastKnownLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        }; **/
        
        checkboxLayout = findViewById(R.id.checkboxLayout);
        checkboxLayout.setVisibility(View.VISIBLE);

        StartGNSSFragment.gnssInit.addObservers(mapMarkerUpdater);
    }

    public void backToMenu(View view) {
        finish();
    }
}
