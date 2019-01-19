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
import android.widget.Button;
import android.widget.TextView;

import com.galfins.gnss_compare.CalculationModule;
import com.galfins.gnss_compare.CalculationModulesArrayList;
import com.galfins.gnss_compare.GNSSCoreServiceActivity;


import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;


public class MainActivity extends GNSSCoreServiceActivity {

    private final String TAG = this.getClass().getSimpleName();

    private int locationFuncLevel = 0;

    private TextView gpsText;
    private TextView galText;
    private TextView navInfoText;
    private Button confirmButton;

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

            Log.e(TAG, "-- observer tick");

            CalculationModulesArrayList CMArrayList = gnssBinder.getCalculationModules();

            for(CalculationModule calculationModule : CMArrayList) {

                String obsConst = calculationModule.getConstellation().getName();
                final String numSats = ("Used: " + calculationModule.getConstellation().getUsedConstellationSize() + " Visible: " + calculationModule.getConstellation().getVisibleConstellationSize());

                Log.e(TAG,"Observer tick: " + calculationModule.getPose().toString());
                Log.e(TAG, "Observer tick const:" + calculationModule.getConstellation().toString());
                Log.e(TAG, "Observer tick const name:" + calculationModule.getConstellation().getName());
                Log.e(TAG,"Observer tick const size used:" + calculationModule.getConstellation().getUsedConstellationSize()+"");
                Log.e(TAG, "Observer tick const size visible:" + calculationModule.getConstellation().getVisibleConstellationSize()+"");

                if (obsConst.equals(GPSConstName)) {

                    final String gpsString = "GPS " + numSats;

                    gpsText.post(new Runnable() {
                        @Override
                        public void run() {
                            gpsText.setText(gpsString);
                        }
                    });

                    // If not yet done, replace the informing text
                    // with the appropriate string for visible GPS sats
                    navInfoText.post(new Runnable() {
                        @Override
                        public void run() {
                            if (locationFuncLevel < LOCATION_GPS_ONLY) {
                                navInfoText.setText(R.string.gps_sats);
                            }
                        }
                    });

                    final int gpsVisible = calculationModule.getConstellation().getVisibleConstellationSize();
                    if (locationFuncLevel < LOCATION_GPS_ONLY) {
                        confirmButton.post(new Runnable() {
                            @Override
                            public void run() {
                                if (gpsVisible > 0) {
                                    locationFuncLevel = LOCATION_GPS_ONLY;
                                    Log.e(TAG, "LocationFuncLevel set to GPS only: " + locationFuncLevel);
                                }
                                confirmButton.setText("USE GPS ONLY");
                            }
                        });
                    }

                } else if (obsConst.equals(GalConstName)) {

                    final String galString = "GAL " + numSats;

                    galText.post(new Runnable() {
                        @Override
                        public void run() {
                            galText.setText(galString);
                        }
                    });
                    Log.e(TAG, "GAL Pose: " + galString);

                    // If we see a Galileo sat, it is safe to assume the phone offers
                    // raw measurement support for GPS and Galileo - therefore go straight to menu
                    if (calculationModule.getConstellation().getVisibleConstellationSize() > 0) {
                        locationFuncLevel = LOCATION_FULL_FUNC;
                        goToMenuFunction();
                    }
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

        gpsText = findViewById(R.id.gpsText);
        galText = findViewById(R.id.galText);
        navInfoText = findViewById(R.id.navInfoText);
        confirmButton = findViewById(R.id.confirmButton);

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
        if (checkLocationAndMobileDataEnabled()) {
            goToMenuFunction();
        }
    }

    public void goToMenuFunction() {
        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("location_permit", mLocationPermissionGranted);
        intent.putExtra("location_functionality", locationFuncLevel);
        startActivity(intent);
        finish();
    }

    public void goToDesc(View view) {
        Intent intent = new Intent(this, DescriptionActivity.class);
        startActivity(intent);
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

    @Override
    protected void onPause() {
        if (gnssBinder != null) {
            gnssBinder.removeObserver(connCheckUpdater);
            Log.e(TAG, "-- observer REMOVED");
        }
        super.onPause();
    }


}

