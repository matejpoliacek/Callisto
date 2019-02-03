package com.chocolateam.galileomap;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.galfins.gnss_compare.GNSSCoreServiceActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends GNSSCoreServiceActivity implements OnMapReadyCallback {

    private final String TAG = this.getClass().getSimpleName();

    protected int locationFuncLevel = 0;

    protected GoogleMap mMap;

    private CameraPosition mCameraPosition;

    // The entry point to the Fused Location Provider.
    protected FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    protected final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    protected static final int DEFAULT_ZOOM = 15;
    protected static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    protected boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    protected Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    protected LocationRequest mLocationRequest;
    protected LocationCallback mLocationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        if (mLocationPermissionGranted == false) {
            mLocationPermissionGranted = getIntent().getExtras().getBoolean("location_permit");
        }

        locationFuncLevel = getIntent().getExtras().getInt("location_functionality");

        // Remove the top option bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
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
        Log.e(TAG, "visited parent onMapReady");
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Location stuff
        createLocationRequest();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        /** Disable marker clicking **/
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

        /** remove my location button **/
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        boolean passed = true;

                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            if (mLastKnownLocation == null) {
                                mLastKnownLocation = task.getResult();
                                // Check if mLastKnownLocation was retrieved succesfully. if we can't get location, advise user
                                if (mLastKnownLocation == null) {
                                    findViewById(R.id.locationText).setVisibility(View.GONE);
                                    locationFailedAlert();
                                    passed = false;
                                }
                            }

                            if (findViewById(R.id.locationText) != null) {
                                findViewById(R.id.locationText).setVisibility(View.GONE);
                            }

                            if (passed) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            findViewById(R.id.locationText).setVisibility(View.GONE);
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    updateLocationUI();
                }
            }
        }

    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // TODO: we probably don't need this
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /***********************/
    /** Location methods **/
    /*********************/

    // TODO: test if we need any of the following 3 methods
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(50);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: add  a listener to continually check if location was disabled
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // TODO: replace with a listener to continually check if location was disabled
    /**
    private void isLocationEnabled() {
        System.out.println("Checking Location availability");
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(getApplicationContext());
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();
        }
        else {
            System.out.println("Location available");
        }
    }
     **/

    /**
     * Method to add the marker to the map based on both checkboxes
     *
     * @param checkbox1Bool     boolean value of the first checkbox coming from checkBox.isChecked()
     * @param checkbox2Bool     boolean value of the second checkbox coming from checkBox.isChecked()
     * @param marker            marker object to be added to the map
     * @param point             location at which the marker should be added
     * @param markerResource      colour to be used by the marker
     */

    protected Marker processMarker(boolean checkbox1Bool, boolean checkbox2Bool, Marker marker, LatLng point, int markerResource) {
        if (checkbox1Bool && checkbox2Bool){
            if (marker == null) {
                Log.e(TAG, "First marker");
            } else {
                marker.remove();
                Log.e(TAG, "New marker");
            }
            marker = mMap.addMarker(new MarkerOptions().position(point));
            marker.setIcon(BitmapDescriptorFactory.fromResource(markerResource));
          //  marker.setDraggable(false);
            return marker;
        } else if ((!checkbox1Bool || !checkbox2Bool)&& marker != null) {
            marker.remove();
            Log.e(TAG, "Remove marker");
            return null;
        } else { // never here
            Log.e(TAG, "Marker error");
            return null;
        }
    }

    /**
     * * Method to add the marker to the map based on one of the checkboxes
     *
     * @param checkbox1Bool     boolean value of the checkbox coming from checkBox.isChecked()
     * @param marker            marker object to be added to the map
     * @param point             location at which the marker should be added
     * @param markerResource    icon to be used by the marker
     */
    protected Marker processMarker(boolean checkbox1Bool, Marker marker, LatLng point, int markerResource) {
        return processMarker(checkbox1Bool, true, marker, point, markerResource);
    }


    protected void locationFailedAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setTitle("Can't Locate You");

        String alertText = "The signal is not good enough to determine your location - if you're indoors, try going outside." ;

        if (locationFuncLevel > LOCATION_DEFAULT_NAV) {
            alertText += " Have a look at the signal and satellite availability by accessing the spaceship from the main menu.";
        }

        alertDialog.setMessage(alertText);
        alertDialog.setPositiveButton("Return to menu", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                (MapsActivity.this).finish();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }
}
