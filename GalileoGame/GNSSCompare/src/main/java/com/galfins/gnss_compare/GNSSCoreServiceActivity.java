package com.galfins.gnss_compare;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Matej on 10/01/2019.
 */

public class GNSSCoreServiceActivity extends AppCompatActivity implements ServiceConnection {

    OnBoundListener mCallback;

    public interface OnBoundListener {
        public void onFragmentReady();
    }

    private final String TAG = this.getClass().getSimpleName();

    protected GnssCoreService gnssService = null;
    protected GnssCoreService.GnssCoreBinder gnssBinder = null;

    protected Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serviceIntent = new Intent(this, GnssCoreService.class);

        startService(serviceIntent);
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "Service bound in onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
        Log.e(TAG, "Service unbound in onPause");
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        gnssBinder = (GnssCoreService.GnssCoreBinder) binder;
        gnssService = gnssBinder.getService();
        Log.e(TAG, "GNSS Service Bound");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        gnssService = null;
        gnssBinder = null;
        Log.e(TAG, "GNSS Service Disconnected");
    }
}
