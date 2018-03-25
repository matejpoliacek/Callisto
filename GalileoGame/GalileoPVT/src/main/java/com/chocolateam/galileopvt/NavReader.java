package com.chocolateam.galileopvt;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.cts.nano.Ephemeris.GpsNavMessageProto;
import android.location.cts.nano.Ephemeris.GpsEphemerisProto;
import android.location.cts.suplClient.SuplRrlpController;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import static android.content.Context.LOCATION_SERVICE;
import java.io.IOException;
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;

/**
 * Created by Cedric on 25/03/2018.
 */

public class NavReader extends Fragment implements LocationListener {
    private static final String SUPL_SERVER_NAME = "supl.google.com";
    private static final int SUPL_SERVER_PORT = 7276;
    private GpsNavMessageProto mHardwareGpsNavMessageProto = null;
    private int[] mReferenceLocation = null;
    private LocationManager mLocationManager;

    public void setReferencePosition(int lat, int lng, int alt) {
        if (mReferenceLocation == null) {
            mReferenceLocation = new int[3];
        }
        // location coordinates LAT/LONG/ALT
        mReferenceLocation[0] = lat;
        mReferenceLocation[1] = lng;
        mReferenceLocation[2] = alt;

    }

    @SuppressLint("MissingPermission")
    public GpsNavMessageProto getSuplMessage(int[] mReferenceLocation) throws Exception {
        GpsNavMessageProto navMessage;
        if (mReferenceLocation == null)
        {
            Log.d(TAG, "No reference location trying the Cell-ID black magic");
            /***
             *  Using the Cell-ID stuff
             ***/
            mReferenceLocation = cellIDLocation();
        }
        navMessage = getSuplNavMessage(mReferenceLocation[0], mReferenceLocation[1]);
        return navMessage;
    }

    @SuppressLint("MissingPermission")
    private int[] cellIDLocation(){
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

    private GpsNavMessageProto getSuplNavMessage(long lat, long lng) throws UnknownHostException, IOException{
        SuplRrlpController suplRrlpController = new SuplRrlpController(SUPL_SERVER_NAME, SUPL_SERVER_PORT);
        GpsNavMessageProto navMessageProto = suplRrlpController.generateNavMessage(lat, lng);
        return navMessageProto;
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
}
