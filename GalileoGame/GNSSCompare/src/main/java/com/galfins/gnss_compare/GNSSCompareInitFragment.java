/*
 * Copyright 2018 TFI Systems

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.galfins.gnss_compare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.GnssMeasurementsEvent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.androidplot.util.PixelUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Observable;
import java.util.Observer;

import com.galfins.gnss_compare.FileLoggers.RawMeasurementsFileLogger;


public class GNSSCompareInitFragment extends Fragment {

    /**
     * Listener to notify parent activity that the fragment is ready
     */

    OnFinishedListener mCallback;

    public interface OnFinishedListener {
        public void onFragmentReady();
    }

    /**
     * Flag to monitor if notification that fragment is ready was sent
     */
    private boolean FragmentReadyNotified = false;

    private final String TAG = "GNSSCompareInitFragment";

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
     * Raw measurements logger
     */
    public static RawMeasurementsFileLogger rawMeasurementsLogger = new RawMeasurementsFileLogger("rawMeasurements");

    private Observer calculationModuleObserver;

    private GnssCoreService.GnssCoreBinder gnssCoreBinder;

    private boolean mGnssCoreBound = false;

    CalculationModule newModule = null;

    Context applicationContext;
    Activity activity;

    private class GnssCoreServiceConnector implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            if(!mGnssCoreBound) {
                gnssCoreBinder = (GnssCoreService.GnssCoreBinder) service;
                mGnssCoreBound = true;

                gnssCoreBinder.addObserver(calculationModuleObserver);

                if(newModule!=null) {
                    gnssCoreBinder.addModule(newModule);
                    CreateModulePreference.notifyModuleCreated();
                    Log.e(TAG,"Module " + newModule.getName() + " created...");
                    newModule = null;
                }
            }
        }

        public void resetConnection(){
            if(gnssCoreBinder != null && mGnssCoreBound) {
                gnssCoreBinder.removeObserver(calculationModuleObserver);
                mGnssCoreBound = false;

                gnssCoreBinder = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            resetConnection();
        }

    }

    private ServiceConnection mConnection = new GnssCoreServiceConnector() ;

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
     * Callback object assigned to the GNSS measurement callback
     */
    GnssMeasurementsEvent.Callback gnssCallback;


    private static Location locationFromGoogleServices = null;

    public static boolean isLocationFromGoogleServicesInitialized(){
        synchronized (locationFromGoogleServicesMutex) {
            return locationFromGoogleServices != null;
        }
    }

    private static final Object locationFromGoogleServicesMutex = new Object();

    /**
     * Bundle storing manifest's meta data, so that it can be used outside of GNSSCompareInitFragment
     */
    private static Bundle metaData;

    /**
     * Registers GNSS measurement event manager callback.
     */
    private void registerLocationManager() {

        if(applicationContext != null) {
            mLocationManager = (LocationManager) applicationContext.getSystemService(Activity.LOCATION_SERVICE);
            Log.e(TAG, "mLocationManager instantiated in registerLocationManager");
        } else {
            Log.e(TAG, "applicationContext null for mLocationManager");
        }

        if(applicationContext != null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext);
            Log.e(TAG, "mFusedLocationClient instantiated in registerLocationManager");
        } else {
            Log.e(TAG, "applicationContext null for mFusedLocationClient");
        }

        gnssCallback = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                super.onGnssMeasurementsReceived(eventArgs);

                Log.d(TAG, "onGnssMeasurementsReceived (GNSSCompareInitFragment): invoked!");

                if (rawMeasurementsLogger.isStarted())
                    rawMeasurementsLogger.onGnssMeasurementsReceived(eventArgs);
            }
        };

        final LocationRequest locationRequest = new LocationRequest();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(500);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(100);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {

                final Location lastLocation = locationResult.getLocations().get(locationResult.getLocations().size()-1);

                if(lastLocation != null) {
                    Log.i(TAG, "locationFromGoogleServices: New location (phone): "
                            + lastLocation.getLatitude() + ", "
                            + lastLocation.getLongitude() + ", "
                            + lastLocation.getAltitude());
                }
            }
        };

        if(applicationContext != null) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mFusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null);

                mLocationManager.registerGnssMeasurementsCallback(
                        gnssCallback);
            }
        } else {
            Log.e(TAG, "applicationContext null for ActivityCompat.checkSelfPermission");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        applicationContext = getContext();
        activity = getActivity();

        initializeGnssCompareMainActivity();

        if (hasGnssAndLogPermissions()) {
            Log.e(TAG, "Registering location manager in onCreate");
            registerLocationManager();
        } else {
            Log.e(TAG, "Requesting GNSS and Log permissions in onCreate");
            requestGnssAndLogPermissions();
        }

        showInitializationDisclamer();

        if(activity != null) {
            activity.startService(new Intent(activity, GnssCoreService.class));
        } else {
            Log.e(TAG, "activity null for startService in onCreate");
        }
    }

    private void initializeGnssCompareMainActivity() {

        initializeMetaDataHandler();

        calculationModuleObserver = new Observer() {

            class UiThreadRunnable implements Runnable {

                CalculationModulesArrayList calculationModules;

                public void setCalculationModules(CalculationModulesArrayList newCalculationModules){
                    synchronized (this) {
                        calculationModules = newCalculationModules;
                    }
                }

                @Override
                public void run() {
                    synchronized (this) {

                    }
                }
            }

            UiThreadRunnable uiThreadRunnable = new UiThreadRunnable();

            @Override
            public void update(Observable o, Object calculationModules) {

                uiThreadRunnable.setCalculationModules((CalculationModulesArrayList) calculationModules);

                if(activity != null) {
                    activity.runOnUiThread(uiThreadRunnable);
                } else {
                    Log.e(TAG, "activity null for runOnUiThread in initializeGnssCompareMainActivity()");
                }

            }
        };

        initializeMetaDataHandler();
    }

    private void showInitializationDisclamer() {
        Log.e(TAG,"All calculations are initialized with phone's FINE location");
    }

    private void initializeMetaDataHandler() {
        ApplicationInfo ai = null;
        try {
            if(activity != null) {
                ai = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
            } else {
                Log.e(TAG, "activity null for getPackageManager()");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(ai!=null) {
            synchronized (metaDataMutex) {
                metaData = ai.metaData;
            }
        }
    }

    public static String getMetaDataString(String key){
        return metaData.getString(key);
    }

    public static int getMetaDataInt(String key){
        return metaData.getInt(key);
    }

    public static boolean getMetaDataBoolean(String key){
        return metaData.getBoolean(key);
    }

    public static float getMetaDataFloat(String key){
        return metaData.getFloat(key);
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: invoked");
    }


    /**
     * Called when activity is resumed
     * restarts the data generating threads
     */
    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                startAndBindGnssCoreService();
            }
        }).start();

        Log.d(TAG, "onResume: invoked");
    }

    public void startAndBindGnssCoreService(){
        if(!GnssCoreService.isServiceStarted()) {
            //todo: encapsulate this in GnssCoreService
            if(activity != null) {
                activity.startService(new Intent(activity, GnssCoreService.class));
                Log.e(TAG, "startService invoked in startAndBindGnssCoreService()");
            } else {
                Log.e(TAG, "activity null for startService in startAndBindGnssCoreService()");
            }



            if(!GnssCoreService.waitForServiceStarted()){
                Log.e(TAG, "Issue starting GNSS Core service...");

                //todo: consider a return here?
            }

        }

        //todo: encapsulate this in GnssCoreService

        if(activity != null) {
            activity.bindService(
                    new Intent(activity, GnssCoreService.class),
                    mConnection,
                    Context.BIND_AUTO_CREATE);
        } else {
            Log.e(TAG, "activity null for bindService in startAndBindGnssCoreService()");
        }

    }

    /**
     * Called when activity is paused
     * stops the data generating threads
     */
    @Override
    public void onPause() {
        super.onPause();

        if(mGnssCoreBound) {
            if(activity != null) {
                activity.unbindService(mConnection);
                ((GnssCoreServiceConnector) mConnection).resetConnection();
            } else {
                Log.e(TAG, "activity null for unbindService in onPause()");
            }
        }

        Log.d(TAG, "onPause: invoked");
    }

    /** UNUSED
    @Override
    protected void onRestart() {
        super.onRestart();

    }
    **/
    @Override
    public void onDestroy() {
        super.onDestroy();

        mLocationManager.unregisterGnssMeasurementsCallback(gnssCallback);
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PREFERENCES_REQUEST && resultCode == activity.RESULT_OK) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

            try {
                newModule = CalculationModule.createFromDescriptions(
                        sharedPreferences.getString(CreateModulePreference.KEY_NAME, null),
                        sharedPreferences.getString(CreateModulePreference.KEY_CONSTELLATION, null),
                        sharedPreferences.getStringSet(CreateModulePreference.KEY_CORRECTION_MODULES, null),
                        sharedPreferences.getString(CreateModulePreference.KEY_PVT_METHOD, null),
                        sharedPreferences.getString(CreateModulePreference.KEY_FILE_LOGGER, null));

            } catch (CalculationModule.NameAlreadyRegisteredException
                    | CalculationModule.NumberOfSeriesExceededLimitException
                    | CalculationModule.CalculationSettingsIncompleteException e) {
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerLocationManager();
            }
        }
    }

    /**
     * Checks if the permission has been granted
     * @return True of false depending on if permission has been granted
     */
    @SuppressLint("ObsoleteSdkInt")
    private boolean hasGnssAndLogPermissions() {
        // Permissions granted at install time.
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                (ContextCompat.checkSelfPermission(activity, GNSS_REQUIRED_PERMISSIONS)
                            == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(activity, LOG_REQUIRED_PERMISSIONS)
                                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Requests permission to access GNSS measurements
     */
    private void requestGnssAndLogPermissions() {
        ActivityCompat.requestPermissions(activity, new String[]{GNSS_REQUIRED_PERMISSIONS, LOG_REQUIRED_PERMISSIONS}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            //parent = activity;
            mCallback = (OnFinishedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFinishedListener");
        }

    }

}

