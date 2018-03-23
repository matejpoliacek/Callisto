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
import android.support.v4.app.ActivityCompat;
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
    public static final int MAX_CARRIER_TO_NOISE = 28;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static String CONSTELLATION_SWITCH = "GPS"; // possible values: GPS, GALILEO
    private Context context;
    private LocationManager mLocationManager;
    private int satcount;
    private GnssClock receiverClock;
    private Collection<GnssMeasurement> noisySatellites;
    private Collection<GnssMeasurement> satellites;
    private Collection<GnssMeasurement> galileoSatellites;
    private Collection<GnssMeasurement> gpsSatellites;

    private double pseudoRange;

    public BlankFragment() {
        // constructor as empty as my wallet before payday
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        run();
    }

    public void run() {
        mLocationManager =
                (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        /*******************************************************************************
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

                // Reset list of Galileo and GPS satellites
                galileoSatellites = new <GnssMeasurement>ArrayList();
                gpsSatellites = new <GnssMeasurement>ArrayList();

                // Filter for bad carrier to noise ration in satellites
                for (GnssMeasurement m : noisySatellites) {
                    if (m.getCn0DbHz() < MAX_CARRIER_TO_NOISE) {
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


                /******************************************************
                 Calculate pseudorange every callback, i.e. every second - alternative is to do it outside of this function with locks
                ******************************************************/
                // Careful, pseudorange can be null

                if (CONSTELLATION_SWITCH.equals("GPS") && (gpsSatellites.size() > 3)) {
                    pseudoRange = calcPseudoRange_GPS();
                }
                if (CONSTELLATION_SWITCH.equals("GALILEO")&& (galileoSatellites.size() > 3)) {
                    pseudoRange = calcPseudoRange_Galileo();
                }

                /***********************************************************************
                 Add corrections to pseudorange if not null to get corrected pseudorange
                 **********************************************************************/
                // Enter your code here

                /*************************
                 Calculate computed range
                 ************************/
                // Enter your code here


                /***************************************************************
                 If computed range not null, perform Linearisation and get x y z
                 **************************************************************/
                // Enter your code here
            }
        };

        ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION); // To avoid errors with registering callbacks
        mLocationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventCallback);
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, this
        );

    }

    /****************************************************
    Pseudorange calculation functions for GPS and Galileo
    ****************************************************/
    public double calcPseudoRange_GPS() {
            // TODO: continue computation with the following:
                    /*
                        stuff we're passing to the satellite:
                        GNSSClock.TimeNanos
                       GNSSClock.FullBiasNanos
                        GNSSClock.BIasNanos
                        GNSSMeasurement.TimeOffsetNanos
                           GNSSClock.FullBiasNanos
                     */
            for (GnssMeasurement sat : gpsSatellites) {
                // TODO: do stuff with satellite class
                // create new satellite class instance
                // pass parameters from sat to satellite's setter and then make a private satellite function to compute
            }
        return 0.0;
    }

    public double calcPseudoRange_Galileo() {
        return 0.0;
    }


    /***
    Misc
    ***/

    public void setContext(Context context) {
        this.context = context;
    }

    public void switchConstellation(String constellation) {
        CONSTELLATION_SWITCH = constellation;
    }

    /****************************
     LocationListener boilerplate
     ***************************/
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