package com.chocolateam.galileopvt;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.cts.nano.Ephemeris;
import android.location.cts.nano.Ephemeris.GpsNavMessageProto;
import android.location.cts.suplClient.SuplRrlpController;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import java.lang.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Cedric on 25/03/2018.
 */

public class NavReader extends Fragment implements LocationListener {
    private static final String SUPL_SERVER_NAME = "supl-dev.google.com";
    private static final int SUPL_SERVER_PORT = 7280;
    private GpsNavMessageProto mHardwareGpsNavMessageProto = null;
    private long[] mReferenceLocation = new long[] {0,1};
    private LocationManager mLocationManager;
    public Ephemeris.GpsNavMessageProto navMessage;

    public void setReferencePosition(long lat, long lng, long alt) {
        if (mReferenceLocation == null) {
            mReferenceLocation = new long[3];
        }
        mReferenceLocation[0] = lat;
        mReferenceLocation[1] = lng;
        mReferenceLocation[2] = alt;
    }

    @SuppressLint("MissingPermission")
    public GpsNavMessageProto getSuplMessage() throws Exception {
        //if (mReferenceLocation == null) {
        //  Cell ID black magic non functioning, waiting on Paolo's advice regarding Google Geolocation API
        //  mReferenceLocation = cellIDLocation();
        //}
        navMessage = getSuplNavMessage(0,0);
        return null;
    }

    @SuppressLint("MissingPermission")

    /*
    @param lat receiver latitude
    @param lng receiver longitude
     */
    // This function works without providing anything really, as we can't get our lat/lon from Telephony yet
    // Calls Google's server to get Navigation Message
    private GpsNavMessageProto getSuplNavMessage(long lat, long lng) {
        // This has to be changed to a dynamic value from the Google Geolocation API
        mReferenceLocation[0] = lat;
        mReferenceLocation[1] = lng;
        try {
            Ephemeris.GpsNavMessageProto navMsg = new NavThread().execute(mReferenceLocation).get();
            return navMsg;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

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
}
