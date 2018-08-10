package com.galfins.gnss_compare;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssMeasurementsEvent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import com.galfins.gnss_compare.Constellations.Constellation;
import com.galfins.gnss_compare.Constellations.GalileoConstellation;
import com.galfins.gnss_compare.Constellations.GalileoGpsConstellation;
import com.galfins.gnss_compare.Constellations.GpsConstellation;
import com.galfins.gnss_compare.Corrections.Correction;
import com.galfins.gnss_compare.Corrections.ShapiroCorrection;
import com.galfins.gnss_compare.Corrections.TropoCorrection;
import com.galfins.gnss_compare.FileLoggers.FileLogger;
import com.galfins.gnss_compare.FileLoggers.NmeaFileLogger;
import com.galfins.gnss_compare.FileLoggers.RawMeasurementsFileLogger;
import com.galfins.gnss_compare.PvtMethods.DynamicExtendedKalmanFilter;
import com.galfins.gnss_compare.PvtMethods.PvtMethod;


public class GNSSCompareInitFragment extends Fragment {

    /**
     * Tag used for logging to logcat
     */
    @SuppressWarnings("unused")
    private final String TAG = "InitFragment";

    /**
     * Tag used to mark module names for savedInstanceStates of the onCreate method.
     */
    private final String MODULE_NAMES_BUNDLE_TAG = "__module_names";

    /**
     * ID for startActivityForResult regarding the preferences screen
     */
    private static int PREFERENCES_REQUEST = 1;

    /**
     * Permission needed for accessing the measurements from the GNSS chip
     */
    private static final String GNSS_REQUIRED_PERMISSIONS = Manifest.permission.ACCESS_FINE_LOCATION;

    /**
     * Permission needed for accessing the measurements from the GNSS chip
     */
    private static final String LOG_REQUIRED_PERMISSIONS = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /**
     * Request code used for permissions
     */
    private static final int PERMISSION_REQUEST_CODE = 1;

    /**
     * Client for receiving the location from Google Services
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * LocationManager object to receive GNSS measurements
     */
    private LocationManager mLocationManager;

    /**
     * Object storing created calculation modules
     */
    public static CalculationModulesArrayList createdCalculationModules;

    /**
     * ViewPager object, which allows for scrolling over Fragments
     */
    private ViewPager mPager;

    /**
     * Raw measurements logger
     */
    public static RawMeasurementsFileLogger rawMeasurementsLogger = new RawMeasurementsFileLogger("rawMeasurements");

    /**
     * Locally saved state of created calculation modules
     */
    private static Bundle savedState;

    /**
     * Callback used for receiving phone's location
     */
    LocationCallback locationCallback;
    private static final Object metaDataMutex = new Object();

    public static Location getLocationFromGoogleServices() {
        synchronized (locationFromGoogleServicesMutex) {
            return locationFromGoogleServices;
        }
    }

    /**
     * Method to synchronize execution of calculations.
     */
    public void notifyCalculationObservers() {
        createdCalculationModules.notifyObservers();
    }

    /**
     * Callback object assigned to the GNSS measurement callback
     */
    GnssMeasurementsEvent.Callback gnssCallback;

    static View mainView;

    private static Location locationFromGoogleServices = null;

    public static boolean isLocationFromGoogleServicesInitialized(){
        synchronized (locationFromGoogleServicesMutex) {
            return locationFromGoogleServices != null;
        }
    }

    private static final Object locationFromGoogleServicesMutex = new Object();

    /**
     * Bundle storing manifest's meta data, so that it can be used outside of this fragment
     */
    private static Bundle metaData;

    /**
     * Registers GNSS measurement event manager callback.
     */
    private void registerLocationManagerCallbacks() {
        gnssCallback = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                super.onGnssMeasurementsReceived(eventArgs);

                Log.d(TAG, "onGnssMeasurementsReceived: invoked!");

                for (CalculationModule calculationModule : createdCalculationModules)
                    calculationModule.updateMeasurements(eventArgs);

                notifyCalculationObservers();
            }
        };

        // todo test this "context" operations
        Context applicationContext = getContext();

        if(applicationContext != null) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.registerGnssMeasurementsCallback(
                        gnssCallback);
            }
        }
    }

    /**
     * Class encapsulating generic operations on created CalculationModules.
     */
    public class CalculationModulesArrayList extends ArrayList<CalculationModule> {

        @Override
        public boolean add(final CalculationModule calculationModule) {
            synchronized (this) {
                return super.add(calculationModule);
            }
        }

        @Override
        public boolean remove(Object o){
            synchronized (this) {
                return super.remove(o);
            }
        }

        /**
         * Start threads associated with added CalculationModules. This is a single execution
         * of a calculation module's run() method
         */
        public void notifyObservers() {
            Log.d(TAG, "notifyObservers: invoked");
            synchronized (this) {
                for (CalculationModule calculationModule : this) {
                    calculationModule.run(); // notifies observers and sets up proper flags
                }
            }

        }
    }

    public void addObservers(Observer observer){
        for(CalculationModule calculationModule : createdCalculationModules){
            calculationModule.addObserver(observer);
        }
    }

    /**
     * Creates initial calculation modules
     */
    public void initializeCalculationModules(){
        if(createdCalculationModules==null) {
            createdCalculationModules = new CalculationModulesArrayList();
            if(savedState == null)
                createInitialCalculationModules();
            else if (savedState != null){
                createCalculationModulesFromBundle(savedState);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // todo test this "context" operations
        Context applicationContext = getContext();
        System.out.println("APP CONTEXT: checking in onCreate...");
        if(applicationContext != null) {
            System.out.println("APP CONTEXT: not null");
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext);
        }

//        if(savedInstanceState != null)
//            savedState = savedInstanceState;

        Constellation.initialize();
        Correction.initialize();
        PvtMethod.initialize();
        FileLogger.initialize();

//        initializePager();
//        initializeToolbar();
        initializeCalculationModules();

        registerLocationManagerReference();
        registerLocationManagerCallbacks();

//        mainView = findViewById(R.id.main_view);

//        final Snackbar snackbar = Snackbar
//                .make(mainView,
//                        "All calculations are initialized with phone's FINE location",
//                        Snackbar.LENGTH_LONG);
//
//        snackbar.setAction("Acknowledge", new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                snackbar.dismiss();
//            }
//        });
//
//        snackbar.show();

        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: invoked");
    }

    /**
     * Creates new calculation modules, based on data stored in the bundle
     * TODO - Add flags for status of "Active" and "Log"
     * @param savedInstanceState bundle describing created calculation modules
     */
    private void createCalculationModulesFromBundle(Bundle savedInstanceState) {

        ArrayList<String> modulesNames = savedInstanceState.getStringArrayList(MODULE_NAMES_BUNDLE_TAG);

        for(String name : modulesNames){
            try {
                createdCalculationModules.add(CalculationModule.fromConstructorArrayList(savedInstanceState.getStringArrayList(name)));
            } catch (CalculationModule.NameAlreadyRegisteredException | CalculationModule.NumberOfSeriesExceededLimitException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        saveInstanceState(bundle);
    }

    /**
     * Parses created calculation module definitions to a bundle. This bundle can later be used with
     * createCalculationModulesFromBundle to create a new set of calculationModules
     * @param bundle reference to a Bundle object to which the information is to be stored.
     */
    private void saveInstanceState(Bundle bundle){
        ArrayList<String> modulesNames = new ArrayList<>();

        for (CalculationModule module: createdCalculationModules)
            modulesNames.add(module.getName());

        bundle.putStringArrayList(MODULE_NAMES_BUNDLE_TAG, modulesNames);

        for (CalculationModule module : createdCalculationModules){
            ArrayList<String> moduleDescription = module.getConstructorArrayList();
            bundle.putStringArrayList(module.getName(), moduleDescription);
        }
    }

    /**
     * Creates default, initial calculation modules
     */
    private void createInitialCalculationModules(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {

                        List<CalculationModule> initialModules = new ArrayList<>();

                        initialModules.add(new CalculationModule(
                                "Galileo+GPS",
                                GalileoGpsConstellation.class,
                                new ArrayList<Class<? extends Correction>>() {{
                                    add(ShapiroCorrection.class);
                                    add(TropoCorrection.class);
                                }},
                                DynamicExtendedKalmanFilter.class,
                                NmeaFileLogger.class));

                        initialModules.add(new CalculationModule(
                                "GPS",
                                GpsConstellation.class,
                                new ArrayList<Class<? extends Correction>>() {{
                                    add(ShapiroCorrection.class);
//                                    add(IonoCorrection.class);
                                    add(TropoCorrection.class);
                                }},
                                DynamicExtendedKalmanFilter.class,
                                NmeaFileLogger.class));

                        initialModules.add(new CalculationModule(
                                "Galileo",
                                GalileoConstellation.class,
                                new ArrayList<Class<? extends Correction>>() {{
                                    add(ShapiroCorrection.class);
                                    add(TropoCorrection.class);
                                }},
                                DynamicExtendedKalmanFilter.class,
                                NmeaFileLogger.class));

                        try {
                            for(CalculationModule module : initialModules)
                                createdCalculationModules.add(module);
                        } catch (Exception e){
                            for(CalculationModule module : initialModules) {
                                try {
                                    createdCalculationModules.remove(module);
                                } catch (Exception e2){
                                    e2.printStackTrace();
                                    Log.e(TAG, "run: Removal of initial module failed");
                                }
                            }
                            CalculationModule.clear();
                        }

                        Log.i(TAG, "run: Calculation modules initialized");
                        break;
                    } catch (CalculationModule.NameAlreadyRegisteredException e) {
                        CalculationModule.clear();
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * Called when activity is resumed
     * restarts the data generating threads
     */
    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: invoked");

        final LocationRequest locationRequest = new LocationRequest();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(500);
        locationRequest.setInterval(100);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //TODO: verify if this chunk gets accessed
                final Location lastLocation = locationResult.getLocations().get(locationResult.getLocations().size()-1);
                System.out.println("LAST LOCATION: checking...");
                if(lastLocation != null) {
                    System.out.println("LAST LOCATION: not null");
                    Log.i(TAG, "locationFromGoogleServices: New location (phone): "
                            + lastLocation.getLatitude() + ", "
                            + lastLocation.getLongitude() + ", "
                            + lastLocation.getAltitude());

                    synchronized (GNSSCompareInitFragment.this) {
                        for (CalculationModule calculationModule : createdCalculationModules)
                            calculationModule.updateLocationFromGoogleServices(lastLocation);

                    }
                }
            }
        };

        // todo test this "context" operations
        Context applicationContext = getContext();

        if(applicationContext != null) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }
    }

    /**
     * Called when activity is paused
     * stops the data generating threads
     */
    @Override
    public void onPause() {
        super.onPause();

        mFusedLocationClient.removeLocationUpdates(locationCallback);

        Log.d(TAG, "onPause: invoked");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        savedState = new Bundle();
        saveInstanceState(savedState);

        while(createdCalculationModules.size() > 0) {
            createdCalculationModules.remove(createdCalculationModules.get(0));
        }

        createdCalculationModules = null;
        CalculationModule.clear();

        mLocationManager.unregisterGnssMeasurementsCallback(gnssCallback);
    }

    /**
     * Creates a reference to the location manager service
     */
    private void registerLocationManagerReference() {
        mLocationManager = (LocationManager) getContext().getSystemService(Activity.LOCATION_SERVICE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerLocationManagerReference();
                registerLocationManagerCallbacks();
            }
        }
    }

    public static void makeNotification(final String note){
        Snackbar snackbar = Snackbar
                .make(mainView, note, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

}

