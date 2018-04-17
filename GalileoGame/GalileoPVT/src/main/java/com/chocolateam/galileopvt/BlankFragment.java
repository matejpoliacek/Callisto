package com.chocolateam.galileopvt;

import android.Manifest;
import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.cts.nano.Ephemeris;
import android.location.cts.suplClient.SuplRrlpController;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Peter Vanik on 16/03/2018.
 * Class containing calculated measurement attributes of a Satellite
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
    private ArrayList<Satellite> pseudoGalSats;
    private ArrayList<Satellite> pseudoGpsSats;

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
         GNSS Measurements Event for obtaining receiver clock and Satellite measurements
         ******************************************************************************/
        GnssMeasurementsEvent.Callback gnssMeasurementsEventCallback = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived (GnssMeasurementsEvent eventArgs) {
                super.onGnssMeasurementsReceived (eventArgs);
                noisySatellites = eventArgs.getMeasurements();
                //((PvtActivity)context).publishSatcount(String.format("Satellite count: %d", satcount)); //TODO: Everything is ok in logs but doesn't appear in Activity. Y THO? Publishing causes binder error in logs.

                receiverClock = eventArgs.getClock();
                //((PvtActivity)context).publishDiscontinuity(String.format("HW Clock discontinuity: %d", receiverClock.getHardwareClockDiscontinuityCount()));

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

                    cellIDLocation();

                    ////////////////////////////////////////////

                    for (GnssMeasurement m : noisySatellites) {
                        // Filter satellites for bad carrier to noise ratio and bad state
                        // TODO the state MscAmbiguous is tailored for GPS. Reformulate for Galileo (E1C2nd)
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


                    /************************************************************
                     Calculate pseudorange of every Satellite during the callback
                     ***********************************************************/
                    if (CONSTELLATION_SWITCH.equals("GPS") && (gpsSatellites.size() > 0)) { //TODO change the 0 to 3 for PVT calculation
                        pseudoGpsSats = new ArrayList<>();

                        for (int i = 0; i < gpsSatellites.size(); i++) {
                            Satellite pseudosat = new Satellite(gpsSatellites.get(i).getSvid());
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

    public void cellIDLocation(){
        // Update cellCID, cellMCC, cellMNC, cellID, cellLAC from Telephony API
        ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        int cellID = cellLocation.getCid();
        int cellLAC = cellLocation.getLac();
        String cellPosition = cellLocation.toString();

        Log.e("All cell info", String.valueOf(telephonyManager.getAllCellInfo()));
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        CellInfoLte cellInfoLte = (CellInfoLte) cellInfoList.get(0);

        int cellCID = cellInfoLte.getCellIdentity().getCi();
        int cellMCC = cellInfoLte.getCellIdentity().getMcc();
        int cellMNC = cellInfoLte.getCellIdentity().getMnc();

        // Now we should have all the elements to request Google Network Location API

        for (int i = 0; i < cellInfoList.size(); i++){
            Log.e("CELL ", String.valueOf(cellInfoList.get(i)));
        }
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