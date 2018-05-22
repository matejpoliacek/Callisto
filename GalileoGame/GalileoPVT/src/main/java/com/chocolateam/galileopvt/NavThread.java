package com.chocolateam.galileopvt;

import android.location.cts.nano.Ephemeris;
import android.location.cts.nano.GalileoEphemeris;
import android.location.cts.suplClient.SuplRrlpController;
import android.os.AsyncTask;
import android.util.Pair;

import java.io.IOException;

public class NavThread extends AsyncTask<long[], Integer, Pair<Ephemeris.GpsNavMessageProto, GalileoEphemeris.GalNavMessageProto>> {

    /*public interface AsyncResponse {
        void processFinish(Ephemeris.GpsNavMessageProto output);
    }
    public AsyncResponse delegate = null;

    public NavThread(AsyncResponse delegate){
        this.delegate = delegate;
    }*/

    @Override
    protected Pair<Ephemeris.GpsNavMessageProto, GalileoEphemeris.GalNavMessageProto> doInBackground(long[]... doubles) {
        Pair<Ephemeris.GpsNavMessageProto, GalileoEphemeris.GalNavMessageProto> navMsg;
        SuplRrlpController mSuplController = new SuplRrlpController("supl-dev.google.com", 7280);
        try{
            navMsg = mSuplController.generateNavMessage(521601100, 44970100);
            return navMsg;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
