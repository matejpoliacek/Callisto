package com.galfins.gnss_compare;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Matej on 10/01/2019.
 */

public class GNSSCoreServiceActivity extends AppCompatActivity implements ServiceConnection {

    final public static String GPSConstName = "GPS L1";
    final public static String GalConstName = "Galileo E1";
    final public static String GalGPSConstName = "Galileo + GPS";

    final protected int LOCATION_DEFAULT_NAV = 0;
    final protected int LOCATION_GPS_ONLY = 1;
    final protected int LOCATION_FULL_FUNC = 2;

    private final String TAG = this.getClass().getSimpleName();

    protected final int MIN_SDK = 24;


    protected GnssCoreService gnssService = null;
    protected GnssCoreService.GnssCoreBinder gnssBinder = null;

    protected Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= MIN_SDK) {
            serviceIntent = new Intent(this, GnssCoreService.class);

            startService(serviceIntent);
            bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= MIN_SDK) {
            bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
            Log.e(TAG, "Service bound in onResume");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= MIN_SDK) {
            unbindService(this);
            Log.e(TAG, "Service unbound in onPause");
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        if (Build.VERSION.SDK_INT >= MIN_SDK) {
            gnssBinder = (GnssCoreService.GnssCoreBinder) binder;
            gnssService = gnssBinder.getService();
            Log.e(TAG, "GNSS Service Bound");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (Build.VERSION.SDK_INT >= MIN_SDK) {
            gnssService = null;
            gnssBinder = null;
            Log.e(TAG, "GNSS Service Disconnected");
        }
    }
}
