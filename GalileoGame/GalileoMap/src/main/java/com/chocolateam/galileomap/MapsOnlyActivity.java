package com.chocolateam.galileomap;


import android.graphics.Color;
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

            if (constName.equals("GPS")) {
                GPSpoint = new LatLng(lat, lng);
                Log.e("MAP-GPSPoint", GPSpoint.toString());
            } else if (constName.equals("Galileo")) {
                GalileoPoint = new LatLng(lat, lng);
                Log.e("MAP-GALPoint", GalileoPoint.toString());
            } else if (constName.equals("Galileo + GPS")) {
                GalGPSPoint = new LatLng(lat, lng);
                Log.e("MAP-GALGPSPoint", GalGPSPoint.toString());
            }
            /**
            // SET MAP LOCATION, markers and default
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGPSMarker = processMarker(checkBoxGPS.isChecked(), mGPSMarker, GPSpoint, BitmapDescriptorFactory.HUE_RED);
                    mGALMarker = processMarker(checkBoxGAL.isChecked(), mGALMarker, GalileoPoint, BitmapDescriptorFactory.HUE_BLUE);
                    mGALGPSMarker = processMarker(checkBoxGPS.isChecked(), checkBoxGAL.isChecked(), mGALGPSMarker, GalGPSPoint, BitmapDescriptorFactory.HUE_VIOLET);
                }
            });
            **/

            // replaced by nicer methods -->
            if (checkBoxGPS.isChecked()){
                if (mGPSMarker == null) {
                    mGPSMarker = mMap.addMarker(new MarkerOptions().position(GPSpoint));
                    mGPSMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                } else {
                    mGPSMarker.remove();
                    mGPSMarker = mMap.addMarker(new MarkerOptions().position(GPSpoint));
                    mGPSMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
            } else if (!checkBoxGPS.isChecked() && mGPSMarker != null) {
                mGPSMarker.remove();
            }

            if (checkBoxGAL.isChecked()){
                if (mGALMarker == null) {
                    mGALMarker = mMap.addMarker(new MarkerOptions().position(GalileoPoint));
                    mGALMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                } else {
                    mGALMarker.remove();
                    mGALMarker = mMap.addMarker(new MarkerOptions().position(GalileoPoint));
                    mGALMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }
            } else if (!checkBoxGAL.isChecked() && mGALMarker != null) {
                mGALMarker.remove();
            }


            if (checkBoxGAL.isChecked() && checkBoxGPS.isChecked()){
                if (mGALGPSMarker == null) {
                    mGALGPSMarker = mMap.addMarker(new MarkerOptions().position(GalGPSPoint));
                    mGALGPSMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                } else {
                    mGALGPSMarker.remove();
                    mGALGPSMarker= mMap.addMarker(new MarkerOptions().position(GalGPSPoint));
                    mGALGPSMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                }
            } else if ((!checkBoxGAL.isChecked() || !checkBoxGPS.isChecked())&& mGALGPSMarker != null) {
                mGALGPSMarker.remove();
            }
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

    /**
     * Method to add the marker to the map based on both checkboxes
     *
     * @param checkbox1Bool boolean value of the first checkbox coming from checkBox.isChecked()
     * @param checkbox2Bool boolean value of the second checkbox coming from checkBox.isChecked()
     * @param marker        marker object to be added to the map
     * @param point         location at which the marker should be added
     * @param colour        colour of the added marker
     */

    private Marker processMarker(boolean checkbox1Bool, boolean checkbox2Bool, Marker marker, LatLng point, float colour) {
        if (checkbox1Bool && checkbox2Bool){
            if (marker == null) {
                marker = mMap.addMarker(new MarkerOptions().position(point));
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(colour));
                Log.e("MAP-MARKER", "First marker");
                return marker;
            } else {
                marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(point));
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(colour));
                Log.e("MAP-MARKER", "New marker");
                return marker;
            }
        } else if ((!checkbox1Bool || !checkbox2Bool)&& marker != null) {
            marker.remove();
            Log.e("MAP-MARKER", "Remove marker");
            return null;
        } else { // never here
            Log.e("MAP-MARKER", "Marker error");
            return null;
        }
    }

    /**
     * * Method to add the marker to the map based on one of the checkboxes
     *
     * @param checkbox1Bool boolean value of the checkbox coming from checkBox.isChecked()
     * @param marker        marker object to be added to the map
     * @param point         location at which the marker should be added
     * @param colour        colour of the added marker
     */
    private Marker processMarker(boolean checkbox1Bool, Marker marker, LatLng point, float colour) {
        return processMarker(checkbox1Bool, true, marker, point, colour);
    }
}
