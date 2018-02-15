package com.chocolateam.galileomap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private LatLng lastClickedLocation;
    private boolean play = false;
    private boolean firstPoint = true;
    private LatLng point1 = null;
    private LatLng point2 = null;
    private View playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        // add navigation - separate activity?

    }

    public void startGame(View view) {
        playButton = view;
        Toast.makeText(getApplicationContext(), "Select 2 points to mark playing area", Toast.LENGTH_LONG).show();
        play = true;
        firstPoint = true;
        LatLng point1 = null;
        LatLng point2 = null;
        mMap.clear();
        // add some button behavior (disable?)
        playButton.setEnabled(false);
        playButton.setAlpha(0.5f);
    }


    @Override
    public void onMapClick(LatLng point) {
        lastClickedLocation = point;
        Toast.makeText(getApplicationContext(), "Clicked at " + point, Toast.LENGTH_LONG).show();
        //drawRectangle(point);
        if (play) {
            if (firstPoint) {
                point1 = lastClickedLocation;
                firstPoint = false;
            } else {
                point2 = lastClickedLocation;
                drawRectangle(point1, point2);
                play = false;
                firstPoint = true;
                // re-enable button
                playButton.setEnabled(true);
                playButton.setAlpha(1.0f);
            }
        }
    }

    private void drawRectangle(LatLng startLocation, LatLng endLocation) {
        LatLng pt1 = new LatLng(Math.max(startLocation.latitude, endLocation.latitude), Math.min(startLocation.longitude, endLocation.longitude));
        LatLng pt2 = new LatLng(Math.max(startLocation.latitude, endLocation.latitude), Math.max(startLocation.longitude, endLocation.longitude));
        LatLng pt3 = new LatLng(Math.min(startLocation.latitude, endLocation.latitude), Math.max(startLocation.longitude, endLocation.longitude));
        LatLng pt4 = new LatLng(Math.min(startLocation.latitude, endLocation.latitude), Math.min(startLocation.longitude, endLocation.longitude));

        PolygonOptions options = new PolygonOptions();

        // TODO test if we can enter points out of order
        options.add(pt1, pt2, pt3, pt4);

        // TODO these are hardcoded RGB values of holo_blue_dark
        int fill = Color.argb(100, 51, 181, 229);
        options.fillColor(fill);
        options.strokeColor(getResources().getColor(android.R.color.holo_blue_dark));
        options.strokeWidth( 10 );

        mMap.addPolygon( options );
    }
}
