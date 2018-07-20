package com.chocolateam.galileomap;


import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import com.chocolateam.galileopvt.PvtFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsOnlyActivity extends MapsActivity implements OnMapReadyCallback {

     /** MAP ONLY VARIABLES **/
    private View checkboxLayout;
    private CheckBox checkBoxGPS;
    private CheckBox checkBoxGAL;

    private Marker mGPSMarker;
    private Marker mGALMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // run the rest of the onCreate method from superclass
        super.onCreate(savedInstanceState);

       checkBoxGPS = findViewById(R.id.checkBoxGPS);
       checkBoxGAL = findViewById(R.id.checkBoxGAL);

        /** Location Manager **/
        mLocationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                // SET MAP LOCATION, markers and default
                if (checkBoxGPS.isChecked()){
                    LatLng point = new LatLng(PvtFragment.getUserLatitudeDegreesGPS(),
                            PvtFragment.getUserLongitudeDegreesGPS());

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
                    LatLng point = new LatLng(PvtFragment.getUserLatitudeDegreesGalileo(),
                            PvtFragment.getUserLongitudeDegreesGalileo());

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
        };



        checkboxLayout = findViewById(R.id.checkboxLayout);
        checkboxLayout.setVisibility(View.VISIBLE);
    }
}
