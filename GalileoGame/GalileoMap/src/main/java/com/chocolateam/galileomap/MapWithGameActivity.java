package com.chocolateam.galileomap;

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
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
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

import java.util.Observable;
import java.util.Observer;

public class MapWithGameActivity extends MapsActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private ImageButton zoomButton;
    private GameScore inGameScore;
    private GamePanel gameBottomPanel;
    private TutorialView tutorialView;

    private static final int GAME_ZOOM_IN = 20;
    private static final int GAME_ZOOM_OUT = 17;

    private LatLng lastClickedLocation;
    private Marker mMarker;

    /**
     * GAME VARIABLES
     **/
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

    private SensorManager sensorService;
    private Sensor sensor;
    private SensorEventListener mySensorEventListener;

    private boolean cameraMoving = false;
    private final int MAP_ROTATION_SPEED = 200;

    private boolean bDebug = false;
    private boolean bGraphicsDebug = false;

    // selected constellation switch
    private String constellation = "ALL";

    public Observer mapGameUpdater = new Observer() {
        @Override
        public void update(Observable o, Object arg) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // run the rest of the onCreate method from superclass
        super.onCreate(savedInstanceState);

        /** Location Manager **/
        mLocationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                //We're playing the game, set the location to the selected source
                LatLng point;
                switch (constellation) {
                    case "ALL":
                        mLastKnownLocation = location;
                        break;
                    case "GPS":
                        //TODO
                        point = null; //new LatLng(PvtFragment.getUserLatitudeDegreesGPS(), PvtFragment.getUserLongitudeDegreesGPS());
                        mLastKnownLocation.setLatitude(point.latitude);
                        mLastKnownLocation.setLongitude(point.longitude);
                        location.setLatitude(point.latitude);
                        location.setLongitude(point.longitude);
                        break;
                    case "GAL":
                        //TODO
                        point = null; //new LatLng(PvtFragment.getUserLatitudeDegreesGalileo(), PvtFragment.getUserLongitudeDegreesGalileo());
                        mLastKnownLocation.setLatitude(point.latitude);
                        mLastKnownLocation.setLongitude(point.longitude);
                        location.setLatitude(point.latitude);
                        location.setLongitude(point.longitude);
                        break;
                }

                // TODO: the whole if wrapper with bDebug can be removed when debugging is concluded
                if (playing && game != null && !bDebug) {
                    game.setPlayerLocation(location);
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

        /** Sensor setup for compass map-turning support **/
        sensorService = (SensorManager) getSystemService(SENSOR_SERVICE);
        // TODO: is there a better way?

        sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        mySensorEventListener = new SensorEventListener() {

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

        if (sensor != null) {
            sensorService.registerListener(mySensorEventListener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        /** Widgets **/
        // draw class
        draw = new DrawClass();

        zoomButton = (ImageButton) findViewById(R.id.zoomButton);
//        zoomButton.setVisibility(View.INVISIBLE);
//        zoomButton.setEnabled(false);

        inGameScore = findViewById(R.id.in_game_score);
        gameBottomPanel = findViewById(R.id.game_bottom_panel);

        tutorialView = findViewById(R.id.tutorial);
        tutorialView.setGame(this);

        inGameScore.setVisibility(View.VISIBLE);
        gameBottomPanel.setVisibility(View.VISIBLE);
        tutorialView.setVisibility(View.VISIBLE);

        /** Game debugging buttons **/
        Button bDebugButton = findViewById(R.id.debugButton);
        Button bDebugGraphicsButton = findViewById(R.id.debugGraphicsButton);

        bDebugButton.setVisibility(View.GONE);
        bDebugGraphicsButton.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        super.onMapReady(mMap);
        mMap.setOnMapClickListener(this);
    }

    /**********************/
    /*** SENSOR METHODS **/
    /********************/

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

    /***************************/
    /** Game related methods **/
    /*************************/

    @Override
    public void onMapClick(LatLng point) {
        lastClickedLocation = point;
        //Toast.makeText(getApplicationContext(), "Clicked at " + point, Toast.LENGTH_LONG).show();
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
                    stopGame(true);
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

    public void startGameViaButton(View view) {

        constellation = tutorialView.getConst();
        tutorialView.setVisibility(View.GONE);

        startGame();
    }

    public void startGame() {
        // if we're already playing, make next click stop the game first
        if (playing == true) {
            stopGame(true);
        } else {
            Toast.makeText(getApplicationContext(), "Select 2 points to mark playing area", Toast.LENGTH_LONG).show();
            gameSetup = true;
            firstPoint = true;
            point1 = null;
            point2 = null;
        }
    }

    private void playing() {

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        gameSetup = false;

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

    public void stopGame(boolean restart) {

        // disable keeping screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (game != null) {
            game.setPlaying(false);
        }
        gameSetup = false;
        firstPoint = true;


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
            if (gp != null) {
                gp.removeAllObjects();
            }
        }
        if (playing && !gameThread.isInterrupted()) {
            gameThread.interrupt();
        }
        playing = false;
        updateCameraBearing(mMap, 0);

        if (restart) {
            startGame();
        }
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

        game.setContext(MapWithGameActivity.this);
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

    public void backToMenu(View view) {
        stopGame(false);
        finish();
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
            stopGame(true);
        } else {
            debugGraphicsButton .setText("Stop Graphics Debugging");
            bGraphicsDebug = true;
            point1 = new LatLng(52.216596, 4.420682);
            point2 = new LatLng(52.216974, 4.420007);
            gameSetup = false;

            Location fakeLocation = new Location("fake_provider");
            fakeLocation.setAltitude(0);
            fakeLocation.setLatitude(52.216598);
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