package com.chocolateam.galileogame;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.galfins.gnss_compare.CalculationModule;
import com.galfins.gnss_compare.CalculationModulesArrayList;
import com.galfins.gnss_compare.GNSSCompareInitFragment;
import com.galfins.gnss_compare.GNSSCoreServiceActivity;
import com.galfins.gnss_compare.StartGNSSFragment;


import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;


public class MainActivity extends GNSSCoreServiceActivity implements GNSSCompareInitFragment.OnFinishedListener {

    private final String TAG = this.getClass().getSimpleName();

    TextView gpsText;
    TextView galText;

    private boolean mLocationPermissionGranted = false;
    //private StartGNSSFragment startedFragment;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int LOCATION_REQUEST_ID = 1;

    //TODO: Replace with service
    public Observer connCheckUpdater = new Observer() {

        @Override
        public void update(final Observable o, Object arg) {

            //TODO: Can this be made more robust via getters from GNSSCompare? - same in TutorialView in map
            final String GPSConstName = "GPS";
            final String GalConstName = "Galileo";
            final String GalGPSConstName = "Galileo + GPS";

            Log.e(TAG, "-- observer tick");

            CalculationModulesArrayList CMArrayList = gnssBinder.getCalculationModules();

            for(CalculationModule calculationModule : CMArrayList) {

                String obsConst = calculationModule.getConstellation().getName();
                final String numSats = ("Used: " + calculationModule.getConstellation().getUsedConstellationSize() + " Visible: " + calculationModule.getConstellation().getVisibleConstellationSize());

                Log.e("Observer tick: " , calculationModule.getPose().toString());
                Log.e("Observer tick const:" , calculationModule.getConstellation().toString());
                Log.e("Observer tick const name:" , calculationModule.getConstellation().getName());
                Log.e("Observer tick const size used:" , calculationModule.getConstellation().getUsedConstellationSize()+"");
                Log.e("Observer tick const size visible:" , calculationModule.getConstellation().getVisibleConstellationSize()+"");

                if (obsConst.equals(GPSConstName)) {
                    final String gpsString = "GPS " + numSats;
                    gpsText.post(new Runnable() {
                        @Override
                        public void run() {
                            gpsText.setText(gpsString);
                        }
                    });
                    Log.e(TAG, "GPS Pose: " + gpsString);

                } else if (obsConst.equals(GalConstName)) {
                    final String galString = "GAL " + numSats;
                    galText.post(new Runnable() {
                        @Override
                        public void run() {
                            galText.setText(galString);
                        }
                    });

                    Log.e("TAG", "GAL Pose: " + galString);
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        requestPermissionAndSetupFragments(this);

        gpsText = (TextView) findViewById(R.id.gpsText);
        galText = (TextView) findViewById(R.id.galText);

        while ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){
            requestPermissionAndSetupFragments(this);
        }

       /** FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        startedFragment = new StartGNSSFragment();
        fragmentTransaction.add(android.R.id.content, startedFragment).commit();
**/

 /*     findViewById(R.id.GameButton).setEnabled(false);
        findViewById(R.id.GameButton).setAlpha(0.6f);
        findViewById(R.id.spaceshipButton).setEnabled(false);
        findViewById(R.id.spaceshipButton).setAlpha(0.6f);
        findViewById(R.id.MapButton).setEnabled(false);
        findViewById(R.id.MapButton).setAlpha(0.6f);
*/
        checkLocationAndMobileDataEnabled();
    }

    @Override
    protected void onResume() {
        // Start the blank fragment initiating Gal/Gps PVT on app start
        super.onResume();
        if ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            // FragmentManager fragmentManager = getSupportFragmentManager();
            // FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // PvtFragment pvtFrag = new PvtFragment();
            // fragmentTransaction.add(android.R.id.content, pvtFrag).commit();
//            Log.e(TAG, String.valueOf(PvtFragment.getUserLatitudeDegrees()));
        }
    }

    public void goToMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("location_permit", mLocationPermissionGranted);
      //  if (checkLocationAndMobileDataEnabled()) {
            startActivity(intent);
     //   }
    }

    private boolean hasPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permissions granted at install time.
            return true;
        }
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissionAndSetupFragments(final Activity activity) {
        if (hasPermissions(activity)) {
            return;
        } else {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, LOCATION_REQUEST_ID);
        }
    }

    @Override
    public void onFragmentReady() {
 /*       findViewById(R.id.GameButton).setEnabled(true);
        findViewById(R.id.GameButton).setAlpha(1.0f);
        findViewById(R.id.spaceshipButton).setEnabled(true);
        findViewById(R.id.spaceshipButton).setAlpha(1.0f);
        findViewById(R.id.MapButton).setEnabled(true);
        findViewById(R.id.MapButton).setAlpha(1.0f);

        findViewById(R.id.warningText).setVisibility(View.GONE);
       */

        //TODO: check if this start and bind

        /**new Thread(new Runnable() {
            @Override
            public void run() {
                StartGNSSFragment.gnssInit.startAndBindGnssCoreService();
                Log.d(TAG, "startAndBindGnssCoreService: invoked");
            }
        }).start();
    **/
        Log.e(TAG, "binder is null: " + (gnssBinder==null));
        Log.e(TAG, "service is null: " + (gnssService==null));


    }

    /**
     * Checks if Mobile data and Location Services are enabled and displays an Alert dialog box
     * warning the user that they are required.
     */
    public boolean checkLocationAndMobileDataEnabled() {
        final Context context = getApplicationContext();
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean gps_enabled = false;
        boolean network_enabled = false;
        boolean mobileDataEnabled = false;
        NetworkInfo activeNetwork = null;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        try {
            activeNetwork = cm.getActiveNetworkInfo();
        } catch(Exception ex) {}

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            mobileDataEnabled = isMobileDataEnabled(context);
        }

        if(!(gps_enabled && network_enabled && mobileDataEnabled)) {
            // notify user
            Log.e("CHECK 1", "Services not enabled ");
            Log.e("Location:", String.valueOf(gps_enabled));
            Log.e("Network:", String.valueOf(network_enabled));
            Log.e("Data:", String.valueOf(mobileDataEnabled));
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(context.getResources().getString(R.string.services_not_enabled));
            dialog.setNeutralButton(context.getString(R.string.Ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // do Nothing
                }
            });
            dialog.show();
            return false;
        }
        else {
            Log.e("CHECK 2", "All services enabled ");
            return true;
        }
    }

    public boolean isMobileDataEnabled(Context context) {
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean)method.invoke(cm);
        } catch (Exception e) {
            Log.e(TAG, "Mobile data checking error");
        }
        return mobileDataEnabled;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        super.onServiceConnected(name, binder);
        gnssBinder.addObserver(connCheckUpdater);
        Log.e(TAG, "-- observer ADDED");
    }
}

