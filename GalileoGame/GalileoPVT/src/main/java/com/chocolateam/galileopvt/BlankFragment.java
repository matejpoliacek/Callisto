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
import android.location.cts.nano.Ephemeris;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Peter Vanik on 16/03/2018.
 * Class containing calculated measurement attributes of a satellite
 */

public class BlankFragment extends Fragment implements Runnable, LocationListener {
    public static final int MIN_CARRIER_TO_NOISE = 28;
    private static String CONSTELLATION_SWITCH = "GPS"; // possible values: GPS, GALILEO

    private Context context;
    private LocationManager mLocationManager;
    private GnssClock receiverClock;
    private Collection<GnssMeasurement> noisySatellites;
    private ArrayList<GnssMeasurement> galileoSatellites;
    private ArrayList<GnssMeasurement> gpsSatellites;
    private ArrayList<satellite> pseudoGalSats;
    private ArrayList<satellite> pseudoGpsSats;

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
                //((pvtActivity)context).publishSatcount(String.format("Satellite count: %d", satcount)); //TODO: Everything is ok in logs but doesn't appear in Activity. Y THO? Publishing causes binder error in logs.

                receiverClock = eventArgs.getClock();
                //((pvtActivity)context).publishDiscontinuity(String.format("HW Clock discontinuity: %d", receiverClock.getHardwareClockDiscontinuityCount()));

                // Reset list of Galileo and GPS satellites
                galileoSatellites = new ArrayList<>();
                gpsSatellites = new ArrayList<>();

                // Filter for clock discontinuity
                if (receiverClock.getHardwareClockDiscontinuityCount() == 0) {

                    // For debug checking number of satellites:
                    int gpscount = 0;
                    int galcount = 0;
                    for (GnssMeasurement n : noisySatellites) {
                        if (n.getConstellationType() == GnssStatus.CONSTELLATION_GPS){
                            gpscount+=1;
                        } else if (n.getConstellationType() == GnssStatus.CONSTELLATION_GALILEO){
                            galcount+=1;
                        }
                    }
                    Log.e("total noisy gps: ", String.valueOf(gpscount));
                    Log.e("total noisy galileo: ", String.valueOf(galcount));

                    ////////////////////////////////////////////

                    for (GnssMeasurement m : noisySatellites) {
                        // Filter satellites for bad carrier to noise ratio and bad state
                        if (m.getCn0DbHz() >= MIN_CARRIER_TO_NOISE) {
                            if (m.getConstellationType() == GnssStatus.CONSTELLATION_GPS) {
                                if ((m.getState() & GnssMeasurement.STATE_TOW_DECODED) == GnssMeasurement.STATE_TOW_DECODED) {
                                    gpsSatellites.add(m);
                                }
                            } else if (m.getConstellationType() == GnssStatus.CONSTELLATION_GALILEO) {
                                if ((m.getState() & GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK) == GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK) {
                                    galileoSatellites.add(m);
                                }
                            }
                        }
                    }
                    Log.e("Total cleaned GPS: ", String.valueOf(gpsSatellites.size()));
                    Log.e("Total cleaned Galileo: ", String.valueOf(galileoSatellites.size()));

                    Log.e("","");

                    Log.e("TROPO_mapping: ", String.valueOf(corrections.computeTropoCorrection_SAAS_withMapping(0.9104, 0.005,1.5708  )));
                    Log.e("goGPS_tropo: ", String.valueOf(corrections.computeTropoCorrection_SAAS_goGPS(1.5708,5)));
                    Log.e("simple_tropo: ", String.valueOf(corrections.computeTropoCorrection_SAAS_simple(1.5708)));

                    Log.e("","");

                    /************************************************************
                     Calculate pseudorange of every satellite during the callback
                     ***********************************************************/
                    if (CONSTELLATION_SWITCH.equals("GPS") && (gpsSatellites.size() > 0)) { //TODO change the 0 to 3 for PVT calculation
                        pseudoGpsSats = new ArrayList<>();

                        for (int i = 0; i < gpsSatellites.size(); i++) {
                            satellite pseudosat = new satellite(gpsSatellites.get(i).getSvid());
                            pseudosat.computeGnssTime(
                                    receiverClock.getTimeNanos(), gpsSatellites.get(i).getTimeOffsetNanos(),
                                    receiverClock.getFullBiasNanos(),  receiverClock.getBiasNanos()
                            );
                            pseudosat.computeWeekNumberNanos(receiverClock.getFullBiasNanos());
                            pseudosat.computeReceivedTime(CONSTELLATION_SWITCH);
                            pseudosat.computeTransmittedTime(gpsSatellites.get(i).getReceivedSvTimeNanos());
                            pseudosat.computePseudoRange();
                            pseudoGpsSats.add(pseudosat);
                            Log.e("Pseudorange: ", String.valueOf(pseudosat.getPseudoRange()));
                        }
                    }
                    else if (CONSTELLATION_SWITCH.equals("GALILEO") && (galileoSatellites.size() > 0)) { //TODO change the 0 to 3 for PVT calculation
                        // Galileo pseudorange code
                        Log.e("RED ALERT", String.valueOf("Somehow we ended up in this branch"));
                        pseudoGalSats = new ArrayList<>(galileoSatellites.size());
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
            }
        };

        ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION); // To avoid errors with registering callbacks
        mLocationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventCallback);
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, this
        );

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