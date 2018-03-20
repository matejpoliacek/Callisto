package com.chocolateam.galileopvt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class BlankFragment extends Fragment implements Runnable, LocationListener {
    private static final int MAX_CARRIER_TO_NOISE = 28;

    private Context context;
    private LocationManager mLocationManager;
    private int satcount;
    private GnssClock receiverClock;
    private Collection<GnssMeasurement> noisySatellites;
    private Collection<GnssMeasurement> satellites;
    private Collection<GnssMeasurement> galileoSatellites;
    private Collection<GnssMeasurement> gpsSatellites;

    public BlankFragment() {
        // constructor as empty as my wallet before payday
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        satellites = new <GnssMeasurement>ArrayList();
        galileoSatellites = new <GnssMeasurement>ArrayList();
        gpsSatellites = new <GnssMeasurement>ArrayList();
        // TODO: instantiate receiver clock

        this.run();
    }

    public void run() {
        mLocationManager =
                (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        /*
        ************************
        Location user permission
        ************************
         */
        if (ContextCompat.checkSelfPermission((pvtActivity)context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Code for when permission is not granted.
            Toast.makeText(getActivity(), "Y THO",
                    Toast.LENGTH_LONG).show();
            Thread.currentThread().interrupt();
        }

        /******************************************************************************
        GNSS Measurements Event for obtaining receiver clock and satellite measurements
        ******************************************************************************/
        GnssMeasurementsEvent.Callback gnssMeasurementsEventCallback = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived (GnssMeasurementsEvent eventArgs) {
                super.onGnssMeasurementsReceived (eventArgs);
                noisySatellites = eventArgs.getMeasurements();
                satcount = noisySatellites.size();
                //((pvtActivity)context).publishSatcount(String.format("Satellite count: %d", satcount)); //TODO: Everything is ok in logs but doesn't appear in Activity. Y THO? Publishing causes binder error in logs.

                receiverClock = eventArgs.getClock();
                //((pvtActivity)context).publishDiscontinuity(String.format("HW Clock discontinuity: %d", receiverClock.getHardwareClockDiscontinuityCount()));

                // Filter for bad carrier to noise ration in satellites and Galileo / GPS satellites


                for (GnssMeasurement m : noisySatellites) {
                    if (m.getCn0DbHz() < MAX_CARRIER_TO_NOISE) {
                        satellites.add(m);
                        Log.e("SAT MEASUREMENT ADDED", String.valueOf(m));
                        if (m.getConstellationType() == GnssStatus.CONSTELLATION_GALILEO) {
                            galileoSatellites.add(m);
                        } else if (m.getConstellationType() == GnssStatus.CONSTELLATION_GPS) {
                            gpsSatellites.add(m);
                        }
                    }
                }
                Log.e("GPS", String.valueOf(gpsSatellites.size()));
                Log.e("GALILEO", String.valueOf(galileoSatellites.size()));
                //Log.e("clock", String.valueOf(receiverClock));
            }
        };

        mLocationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventCallback);
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, this
        );

        while(true) {
            if (galileoSatellites.size() > 3) {
                // TODO: continue computation with the following:
                /*
                    stuff we're passing to the satellite:
                    GNSSClock.TimeNanos
                   GNSSClock.FullBiasNanos
                    GNSSClock.BIasNanos
                    GNSSMeasurement.TimeOffsetNanos
                       GNSSClock.FullBiasNanos
                 */
                for (GnssMeasurement sat : galileoSatellites) {
                    // TODO: do stuff with satellite class
                    // create new satellite class instance
                    // pass parameters from sat to satellite's setter and then make a private satellite function to compute
                };
            }
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /*****************************************
    LocationListener boilerplate including:
        onLocationChanged
        onStatusChanged
        onProviderEnabled
        onProviderDisabled
     ****************************************/
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
