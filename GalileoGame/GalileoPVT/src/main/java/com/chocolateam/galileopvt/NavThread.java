package com.chocolateam.galileopvt;

import android.location.cts.nano.Ephemeris;
import android.location.cts.suplClient.SuplRrlpController;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

public class NavThread extends AsyncTask<long[], Integer, Ephemeris.GpsNavMessageProto> {

    /*public interface AsyncResponse {
        void processFinish(Ephemeris.GpsNavMessageProto output);
    }
    public AsyncResponse delegate = null;

    public NavThread(AsyncResponse delegate){
        this.delegate = delegate;
    }*/

    @Override
    protected Ephemeris.GpsNavMessageProto doInBackground(long[]... doubles) {
        Ephemeris.GpsNavMessageProto navMsg;
        SuplRrlpController mSuplController = new SuplRrlpController("supl-dev.google.com", 7280);
        try{
            navMsg = mSuplController.generateNavMessage(521601100, 44970100);
            return navMsg;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Ephemeris.GpsNavMessageProto navMsg) {
        Log.e("WENT THRU THE LOOOOP", "wasntme");
    }
}
