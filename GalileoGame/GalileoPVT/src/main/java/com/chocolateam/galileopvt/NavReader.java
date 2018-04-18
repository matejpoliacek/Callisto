package com.chocolateam.galileopvt;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.cts.nano.Ephemeris;
import android.location.cts.nano.Ephemeris.GpsNavMessageProto;
import android.location.cts.suplClient.SuplRrlpController;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.io.IOException;
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;

/**
 * Created by Cedric on 25/03/2018.
 */

public class NavReader extends Fragment implements LocationListener {
    private static final String SUPL_SERVER_NAME = "supl-dev.google.com";
    private static final int SUPL_SERVER_PORT = 7280;
    private GpsNavMessageProto mHardwareGpsNavMessageProto = null;
    private double[] mReferenceLocation = null;
    private LocationManager mLocationManager;
    private Context context;

    public void setReferencePosition(double lat, double lng, double alt) {
        if (mReferenceLocation == null) {
            mReferenceLocation = new double[3];
        }
        // location coordinates LAT/LONG/ALT
        mReferenceLocation[0] = lat;
        mReferenceLocation[1] = lng;
        mReferenceLocation[2] = alt;

    }

    @SuppressLint("MissingPermission")
    public GpsNavMessageProto getSuplMessage() throws Exception {
        GpsNavMessageProto navMessage;
        if (mReferenceLocation == null)
        {
            Log.d(TAG, "No reference location trying the Cell-ID black magic");
            /***
             *  Using the Cell-ID stuff
             ***/
            mReferenceLocation = cellIDLocation();
        }
        navMessage = getSuplNavMessage((long) mReferenceLocation[0], (long) mReferenceLocation[1]);
        return navMessage;
    }

    @SuppressLint("MissingPermission")
    private double[] cellIDLocation(){
        /**
        Here should be the function that returns cellIDLocation
         **/
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        int cellId = cellLocation.getCid();
        int celLac = cellLocation.getLac();
        String cellPosition = cellLocation.toString();


        Log.e("Cell Position", cellLocation.toString());
        return mReferenceLocation;
    }

    /*
    @param lat receiver latitude
    @param lng receiver longitude
     */
    private GpsNavMessageProto getSuplNavMessage(long lat, long lng) throws UnknownHostException, IOException{
        NavReader nav = new NavReader();
        // This has to be changed to a dynamic value from the Google Geolocation API
        try {
            Ephemeris.GpsNavMessageProto navMsg;
            SuplRrlpController mSuplController = new SuplRrlpController("supl-dev.google.com", 7280);
            navMsg = mSuplController.generateNavMessage(521601100, 44970100); // Hardcoded Noordwijk
            Log.e("NAV MESSAGE OKAY", String.valueOf(navMsg));
            return navMsg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void setContext(Context context) {
        this.context = context;
    }
}
