package com.chocolateam.galileomap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private LatLng lastClickedLocation;
    private Button playButton;
    private TextView scoreText;
    private Button zoomButton;
    private GameScore inGameScore;
    private GamePanel gameBottomPanel;
    private TutorialView tutorialView;

    private static final String TAG = MapsActivity.class.getSimpleName();
    private CameraPosition mCameraPosition;


    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int GAME_ZOOM_IN = 20;
    private static final int GAME_ZOOM_OUT = 17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    /** GAME VARIABLES **/
    // Drawing class to handle game visuals
    private DrawClass draw;
    private Polygon playingArea = null;
    private Polygon[][] gameMapObjects = null;
    private GameFragment game;
    private Thread gameThread = null;

    private boolean gameSetup = false;
    private boolean playing = false;
    private boolean firstPoint = true;
    private LatLng point1 = null;
    private LatLng point2 = null;

    private GraphicalPolygon gp;

    private boolean zoomed = true;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    /** Location manager **/
    private LocationManager locationManager;
    private LocationListener mLocationListenerGPS;
    private boolean LocationManagerSuccess = false;

    private Marker mMarker;

    private SensorManager sensorService;
    private Sensor sensor;

    private boolean cameraMoving = false;
    private final int MAP_ROTATION_SPEED = 200;

    private boolean bDebug = false;
    private boolean bGraphicsDebug = false;

    // flag to indicate if the activity is for the game or just of a map
    private String ACTIVITY_TYPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

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

        /** Location Manager **/

        mLocationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                mLastKnownLocation = location;
                System.out.println("Location Changed");

                if (playing && game != null) {

                    // TODO: the whole if wrapper with bDebug can be removed when debugging is concluded
                    if (!bDebug) {
                        game.setPlayerLocation(location);
                    }
                }
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

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        setLocationSettings();
        // draw class
        draw = new DrawClass();

        scoreText = (TextView) findViewById(R.id.scoretext);
        scoreText.setVisibility(View.INVISIBLE);

        playButton = (Button) findViewById(R.id.playButton);

        zoomButton = (Button) findViewById(R.id.zoomButton);
//        zoomButton.setVisibility(View.INVISIBLE);
//        zoomButton.setEnabled(false);

        playButton = (Button) findViewById(R.id.playButton);
        playButton.setVisibility(View.INVISIBLE);

        inGameScore = findViewById(R.id.in_game_score);
        gameBottomPanel = findViewById(R.id.game_bottom_panel);

        tutorialView = findViewById(R.id.tutorial);
        tutorialView.setGame(this);

        // sensor variables for compass
        sensorService = (SensorManager) getSystemService(SENSOR_SERVICE);
        // TODO: is there a better way?
        sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
            sensorService.registerListener(mySensorEventListener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        ACTIVITY_TYPE = getIntent().getExtras().getString("maptype");

        if (ACTIVITY_TYPE.equals("map")) {
           playButton.setVisibility(View.GONE);
        }

    }

    private void setLocationSettings() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, mLocationListenerGPS);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListenerGPS);
            isLocationEnabled();
            LocationManagerSuccess = true;
        } catch (SecurityException e) {
            // do nothing
        }
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
        mMap.setOnMapClickListener(this);

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

        // TODO: tutorial dialog
//        showTutorialDialog();
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
                                try {
                                    if (mLastKnownLocation == null) {
                                        Thread.sleep(3000);
                                        mLastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                        if (mLastKnownLocation == null) {
                                            Thread.sleep(3000);
                                            mLastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                            if (mLastKnownLocation == null) {
                                                // if we can't get location, advise user
                                                findViewById(R.id.locationText).setVisibility(View.GONE);
                                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
                                                alertDialog.setTitle("Can't Locate You");
                                                alertDialog.setMessage("The signal is not good enough to determine your location - if you're indoors, try going outside. " +
                                                        "Have a look at the signal and satellite availability by accessing the spaceship from the main menu.");
                                                alertDialog.setPositiveButton("Return to menu", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        (MapsActivity.this).finish();
                                                    }
                                                });

                                                AlertDialog alert = alertDialog.create();
                                                alert.show();
                                                passed = false;
                                            }
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
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

            // If locationManager settings weren't successfully set, try again now, with permission
            if (!LocationManagerSuccess) {
                setLocationSettings();
            }
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
        isLocationEnabled();
    }

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


    /***************************/
    /** Game related methods **/
    /*************************/

    @Override
    public void onMapClick(LatLng point) {
        lastClickedLocation = point;
        Toast.makeText(getApplicationContext(), "Clicked at " + point, Toast.LENGTH_LONG).show();
        if (gameSetup) {
            if (firstPoint) {
                point1 = lastClickedLocation;
                firstPoint = false;
            } else {
                point2 = lastClickedLocation;
                // TODO: gameInit prepares the entire playing field even if the game turns out to be invalid
                // TODO: we should then check before initialising - address when optimising
                gameInit();
                if (game.isLocationValid() && game.isSizeValid()) {
                    playing();
                } else {
                    stopGame();
                }
            }
            // TODO: delete this else after debugs
        } else if (playing && bDebug){
            if (mMarker == null) {
                mMarker = mMap.addMarker(new MarkerOptions().position(point));
            } else {
                mMarker.setPosition(point);
            }
            Location newLoc = mLastKnownLocation;
            newLoc.setLatitude(point.latitude);
            newLoc.setLongitude(point.longitude);
            game.setPlayerLocation(newLoc);

        }
    }

    public void startGame(View view) {

        tutorialView.setVisibility(View.GONE);

        // if we're already playing, make next click stop the game first
        if (playing == true) {
            stopGame();
        } else {
            Toast.makeText(getApplicationContext(), "Select 2 points to mark playing area", Toast.LENGTH_LONG).show();
            gameSetup = true;
            firstPoint = true;
            point1 = null;
            point2 = null;

            // add some button behavior (disable?)
            playButton.setEnabled(false);
            playButton.setAlpha(0.5f);
            playButton.setText("Stop");
        }
    }

    private void playing() {

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // re-enable button
        gameSetup = false;
        playButton.setEnabled(true);
        playButton.setAlpha(1.0f);

        // zoom onto the player
        priorityCameraZoom(mMap, new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()), GAME_ZOOM_IN);

        //show score
//        scoreText.setVisibility(View.VISIBLE);
        showScore(0, 0);

        // reset the zoom button
//        zoomButton.setVisibility(View.VISIBLE);
////        zoomButton.setText("Zoom Out");
//        zoomButton.setEnabled(true);
        zoomed = true;

        // disable map scrolling/zooming
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        playing = true;
        game.setPlaying(true);
        gameThread = new Thread(game);
        gameThread.start();
    }

    public void stopGame() {

        // disable keeping screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        game.setPlaying(false);
        gameSetup = false;
        firstPoint = true;
        // re-enable button
        playButton.setEnabled(true);
        playButton.setAlpha(1.0f);
        playButton.setText("Play");

        // hide score text
        scoreText.setVisibility(View.INVISIBLE);
        showScore(0,0);

//        zoomButton.setVisibility(View.INVISIBLE);
//        zoomButton.setEnabled(false);

        // enable map scrolling/zooming
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        // clear possible existing drawings
        if (playingArea != null) {
            playingArea.remove();
            playingArea = null;
            gp.removeAllObjects();
        }
        if (playing && !gameThread.isInterrupted()) {
            gameThread.interrupt();
        }
        playing = false;
        updateCameraBearing(mMap, 0);
    }
    // TODO: switch arrays to lists for faster access?
    private void gameInit() {

        // TODO: should the drawing here be moved to the game class?

        playingArea = mMap.addPolygon(draw.drawRectangle(point1, point2));

        // add game as a fragment
        FragmentManager gamefragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = gamefragmentManager.beginTransaction();

        game = new GameFragment();
        fragmentTransaction.add(android.R.id.content, game).commit();

        game.setContext(MapsActivity.this);
        game.setAreaPoints(point1, point2);
        game.setPlayerLocation(mLastKnownLocation);

        int obstacleRows = draw.getRows();
        int obstacleCols = draw.getCols();
        int[][] playFieldArray = game.fieldTypeGenerator(obstacleRows, obstacleCols);

        if (playFieldArray != null) { // playfieldarray will be null if size is too small
            PolygonOptions[][] obstacleOptions = draw.drawObstacles(playFieldArray, mLastKnownLocation);
            gp = new GraphicalPolygon(obstacleOptions, playFieldArray);
            //gameMapObjects = new Polygon[obstacleRows][obstacleCols];
            gp.populateMap(mMap);

            for (int i = 0; i < obstacleRows; i++) {
                for (int j = 0; j < obstacleCols; j++) {
                    if (obstacleOptions[i][j] != null) {
                        if (playFieldArray[i][j] == 1) {
                            game.addObstacle(gp.getGameMapObject(i,j));

                        } else if (playFieldArray[i][j] == 2) {
                            game.addCollectible(i, j, gp.getGameMapObject(i,j));

                        } else if (playFieldArray[i][j] == 3) {
                            game.addFinish(gp.getGameMapObject(i,j));
                        }
                    }
                }
            }
        }
    }

    public void removeMapObjectByIndex(int row, int col) {
        gp.removeGameMapObject(row, col);
    }

    public void showScore(int score, int secs_passed) {
        inGameScore.setMscore(score);
        gameBottomPanel.setClock(secs_passed);
    }

    /**********************/
    /*** SENSOR METHODS **/
    /********************/

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float azimuth = event.values[0];
            if (playing && !cameraMoving) {
                updateCameraBearing(mMap, azimuth);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    };

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .bearing(bearing)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos), MAP_ROTATION_SPEED, null);
    }

    /*************/
    /*** MISC ***/
    /***********/

    private void priorityCameraZoom(GoogleMap map, LatLng latlng, int zoom) {

        // implementing the cancellable callback allows the zoom to finish before rotating takes over

        cameraMoving = true;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom),
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        cameraMoving = false;
                    }

                    @Override
                    public void onCancel() {
                        cameraMoving = false;
                    }
                }
        );
    }

    public void toggleZoom(View view) {
        if (zoomed) {
//            zoomButton.setText("Zoom In");
            priorityCameraZoom(mMap, new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()), GAME_ZOOM_OUT);
            zoomed = false;
        } else {
//            zoomButton.setText("Zoom Out");
            priorityCameraZoom(mMap, new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()), GAME_ZOOM_IN);
            zoomed = true;
        }
    }

    private void showTutorialDialog() {
        FragmentManager fm = getSupportFragmentManager();
        TutorialDialogFragment editNameDialogFragment = TutorialDialogFragment.newInstance("Some Title");
        editNameDialogFragment.show(fm, "fragment_edit_name");

    }

    // TODO: this method can be deleted with the debug button when not necessary anymore
    public void toggleDebug(View view) {
        Button debugButton = (Button)view;
        if (bDebug) {
            debugButton.setText("Start Debugging");
            bDebug = false;
            mMap.getUiSettings().setScrollGesturesEnabled(false);
            mMap.getUiSettings().setZoomGesturesEnabled(false);
            mMap.getUiSettings().setRotateGesturesEnabled(false);
        } else {
            debugButton.setText("Stop Debugging");
            bDebug = true;
            mMap.getUiSettings().setScrollGesturesEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
        }
    }

    // TODO: this method can be deleted with the debug button when not necessary anymore
    public void toggleGraphicsDebug(View view) {
        Button debugGraphicsButton = (Button)view;
        if (bGraphicsDebug) {
            debugGraphicsButton .setText("Start Graphics Debugging");
            bGraphicsDebug = false;
            stopGame();
        } else {
            debugGraphicsButton .setText("Stop Graphics Debugging");
            bGraphicsDebug = true;
            point1 = new LatLng(52.216596, 4.420682);
            point2 = new LatLng(52.215974, 4.422007);
            gameSetup = false;

            Location fakeLocation = new Location("fake_provider");
            fakeLocation.setAltitude(0);
            fakeLocation.setLatitude(52.216523);
            fakeLocation.setLongitude(4.421139);
            fakeLocation.setSpeed(0);

            mLastKnownLocation = fakeLocation;

            gameInit();
            Log.e("Size valid", String.valueOf(game.isSizeValid()));
            Log.e("Location valid", String.valueOf(game.isLocationValid()));
            playing();
        }
    }

}
