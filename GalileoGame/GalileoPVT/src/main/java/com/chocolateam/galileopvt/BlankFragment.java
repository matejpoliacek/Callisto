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
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.location.cts.nano.Ephemeris.GpsEphemerisProto;
import android.location.cts.nano.Ephemeris.GpsNavMessageProto;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Peter Vanik on 16/03/2018.
 * Class containing calculated measurement attributes of a Satellite
 */

public class BlankFragment extends Fragment implements Runnable, LocationListener {
    public static final int MIN_CARRIER_TO_NOISE = 18;
    public static final double MIN_SAT_ELEVATION = Math.toRadians(10.0);
    private static String CONSTELLATION_SWITCH = "GPS"; // possible values: GPS, GALILEO

    private Context context;
    private LocationManager mLocationManager;
    private GnssClock receiverClock;
    private long fullBiasNanos;
    private boolean fullBiasNanosSet;
    private double biasNanos;
    private boolean biasNanosSet;
    private Collection<GnssMeasurement> noisySatellites;
    private ArrayList<GnssMeasurement> galileoSatellites;
    private ArrayList<GnssMeasurement> gpsSatellites;
    private ArrayList<Satellite> pseudoSats;

    private Ephemeris.GpsNavMessageProto navMsg;

    private static double[] userPositionECEFmeters;

    public BlankFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        run();
    }

    public void run() {
        biasNanosSet = false;
        fullBiasNanosSet = false;
        mLocationManager =
                (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        // Set initial location TODO cellID()
        // Hardcoded Noordwijk
        userPositionECEFmeters = new double[3];
        userPositionECEFmeters [0] = 3904174;
        userPositionECEFmeters [1] = 301788;
        userPositionECEFmeters [2] = 5017699;

        /****************************************************
                       Obtain Navigation message
        ****************************************************/

        NavThread navThread = new NavThread();

        try {
            long[] mReferenceLocation = new long[] {0,0};
            navMsg = new NavThread().execute(mReferenceLocation).get();
            Log.e("Obtaining navigation message...", "SUCCESSFUL");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Obtaining navigation message...", "FAILED");
        }

        /*******************************************************************************
         GNSS Measurements Event for obtaining receiver clock and Satellite measurements
         ******************************************************************************/
        GnssMeasurementsEvent.Callback gnssMeasurementsEventCallback = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived (GnssMeasurementsEvent eventArgs) {
                super.onGnssMeasurementsReceived (eventArgs);
                noisySatellites = eventArgs.getMeasurements();
                receiverClock = eventArgs.getClock();

                // Obtain only the first measurement of BiasNanos and FullBiasNanos
                if (fullBiasNanosSet == false) {
                    fullBiasNanos = receiverClock.getFullBiasNanos();
                    fullBiasNanosSet = true;
                }
                if (biasNanosSet == false ) {
                    biasNanos = receiverClock.getBiasNanos();
                    biasNanosSet = true;
                }
                Log.e("Full Bias Nanos: ", String.valueOf(fullBiasNanos));
                Log.e("Bias Nanos: ", String.valueOf(biasNanos));

                // Reset list of Galileo and GPS satellites
                galileoSatellites = new ArrayList<>();
                gpsSatellites = new ArrayList<>();

                // Filter for clock discontinuity
                if (receiverClock.getHardwareClockDiscontinuityCount() == 0) {

                    // Display number of all visible satellites
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


                    /***************************************************
                     * Clean the list of satellites in measurement event
                     **************************************************/
                    for (GnssMeasurement m : noisySatellites) {
                        // Filter satellites for bad carrier to noise ratio (suboptimal signal quality)
                        if (m.getCn0DbHz() >= MIN_CARRIER_TO_NOISE) {
                            if (CONSTELLATION_SWITCH.equals("GPS")) {
                                if (m.getConstellationType() == GnssStatus.CONSTELLATION_GPS) {
                                    if (
                                            (
                                                    (m.getState() & GnssMeasurement.STATE_TOW_DECODED) == GnssMeasurement.STATE_TOW_DECODED
                                                            ||
                                                            (m.getState() & GnssMeasurement.STATE_TOW_KNOWN) == GnssMeasurement.STATE_TOW_KNOWN
                                            ) &&
                                                    (m.getState() & GnssMeasurement.STATE_CODE_LOCK) == GnssMeasurement.STATE_CODE_LOCK
                                            ) {
                                        gpsSatellites.add(m);
                                    }
                                }
                            } else if (CONSTELLATION_SWITCH.equals("GALILEO")) {
                                if (m.getConstellationType() == GnssStatus.CONSTELLATION_GALILEO) {
                                    if (
                                            (m.getState() & GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK) == GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK
                                                    ||
                                                    (m.getState() & GnssMeasurement.STATE_TOW_DECODED) == GnssMeasurement.STATE_TOW_DECODED
                                                    ||
                                                    (m.getState() & GnssMeasurement.STATE_TOW_KNOWN) == GnssMeasurement.STATE_TOW_KNOWN
                                            ) {
                                        galileoSatellites.add(m);
                                    }
                                }
                            }
                        }
                    }
                    Log.e("Total cleaned GPS: ", String.valueOf(gpsSatellites.size()));
                    Log.e("Total cleaned Galileo: ", String.valueOf(galileoSatellites.size()));

                    /*********************************************************************************
                     For every cleaned satellite in constellation, compute pseudorange and corrections
                     ********************************************************************************/
                    // TODO only start computing satellite data if there are enough for a PVT (>3)
                    if (CONSTELLATION_SWITCH.equals("GPS") && (gpsSatellites.size() > 0)) {
                        pseudoSats = new ArrayList<>();

                        for (int i = 0; i < gpsSatellites.size(); i++) {
                            Satellite pseudosat = new Satellite(gpsSatellites.get(i).getSvid(), CONSTELLATION_SWITCH, navMsg, fullBiasNanos, userPositionECEFmeters, gpsSatellites.get(i).getState());

                            // Pseudorange
                            pseudosat.computeGnssTime(
                                    receiverClock.getTimeNanos(), gpsSatellites.get(i).getTimeOffsetNanos(),
                                    fullBiasNanos,  biasNanos);
                            pseudosat.computeWeekNumberNanos(fullBiasNanos);
                            pseudosat.computeReceivedTime();
                            Log.e("State: ", String.valueOf(gpsSatellites.get(i).getState()));
                            Log.e("Time offset nanos: ", String.valueOf(gpsSatellites.get(i).getTimeOffsetNanos()));
                            pseudosat.computeTransmittedTime(gpsSatellites.get(i).getReceivedSvTimeNanos() + (long)gpsSatellites.get(i).getTimeOffsetNanos());
                            pseudosat.computePseudoRange();
                            Log.e("Pseudorange: ", String.valueOf(pseudosat.getPseudoRange()));
                            // TODO if a pseudorange is > 3, there's a clock error so stop the thread and execute run() again

                            // Satellite clock correction
                            pseudosat.computeSatClockCorrectionAndRecomputeTransmissionTime();
                            Log.e("Sat clock correction meters: ", String.valueOf(pseudosat.getSatelliteClockCorrectionMeters()));
                            pseudosat.computeSatellitePosition();
                            //pseudosat.computeMySatPos();

                            // Satellite elevation and atmospheric corrections TODO only every 10seconds
                            pseudosat.computeSatElevationRadians();
                            Log.e("Sat elevation in radians: ", String.valueOf(pseudosat.getSatElevationRadians()));
                            Ecef2LlaConverter.GeodeticLlaValues geoValues = Ecef2LlaConverter.convertECEFToLLACloseForm(
                                    getUserPositionECEFmeters()[0], getUserPositionECEFmeters()[1], getUserPositionECEFmeters()[2]);
                            pseudosat.computeTroposphericCorrection_GPS(geoValues.latitudeRadians, geoValues.altitudeMeters);
                            Log.e("Tropo correction meters: ", String.valueOf(pseudosat.getTroposphericCorrectionMeters()));
                            double alpha[] = navMsg.iono.alpha;
                            double beta[] = navMsg.iono.beta;
                            pseudosat.computeIonosphericCorrection_GPS(alpha, beta);
                            Log.e("IONO correction meters: ", String.valueOf(pseudosat.getIonosphericCorrectionSeconds()*pseudosat.LIGHTSPEED));

                            // Corrected pseudorange
                            pseudosat.computeCorrectedRange();
                            Log.e("CORRECTED RANGE: ", String.valueOf(pseudosat.getCorrectedRange()));
                            pseudoSats.add(pseudosat);
                            Log.e("",""); // empty line
                        }
                    }
                    /*********************************** GALILEO **********************************/
                    // TODO only start computing satellite data if there are enough for a PVT (>3)
                    else if (CONSTELLATION_SWITCH.equals("GALILEO") && (galileoSatellites.size() > 0)) {

                        pseudoSats = new ArrayList<>(galileoSatellites.size());

                        for (int i = 0; i < galileoSatellites.size(); i++) {
                            Satellite pseudosat = new Satellite(galileoSatellites.get(i).getSvid(), CONSTELLATION_SWITCH, navMsg, fullBiasNanos, userPositionECEFmeters, galileoSatellites.get(i).getState());

                            // Pseudorange
                            pseudosat.computeGnssTime(
                                    receiverClock.getTimeNanos(), galileoSatellites.get(i).getTimeOffsetNanos(),
                                    fullBiasNanos,  biasNanos);
                            pseudosat.computeWeekNumberNanos(fullBiasNanos);
                            pseudosat.computeMillisecondsNumberNanos(fullBiasNanos);
                            pseudosat.computeReceivedTime();
                            Log.e("State: ", String.valueOf(galileoSatellites.get(i).getState()));
                            Log.e("Time offset nanos: ", String.valueOf(galileoSatellites.get(i).getTimeOffsetNanos()));
                            pseudosat.computeTransmittedTime(galileoSatellites.get(i).getReceivedSvTimeNanos() + (long)galileoSatellites.get(i).getTimeOffsetNanos()); // TODO test the time offset nano
                            pseudosat.computePseudoRange();
                            Log.e("Pseudorange: ", String.valueOf(pseudosat.getPseudoRange()));
                            // TODO if a pseudorange is > 3, there's a clock error so stop the thread and execute run() again
                            // Satellite clock correction
                            pseudosat.computeSatClockCorrectionAndRecomputeTransmissionTime();
                            Log.e("Sat clock correction meters: ", String.valueOf(pseudosat.getSatelliteClockCorrectionMeters()));
                            pseudosat.computeSatellitePosition();
                            //pseudosat.computeMySatPos();

                            // Satellite elevation and atmospheric corrections TODO only every 10seconds
                            pseudosat.computeSatElevationRadians();
                            Log.e("Sat elevation in radians: ", String.valueOf(pseudosat.getSatElevationRadians()));
                            Ecef2LlaConverter.GeodeticLlaValues geoValues = Ecef2LlaConverter.convertECEFToLLACloseForm(
                                    getUserPositionECEFmeters()[0], getUserPositionECEFmeters()[1], getUserPositionECEFmeters()[2]);
                            pseudosat.computeTroposphericCorrection_GPS(geoValues.latitudeRadians, geoValues.altitudeMeters);
                            Log.e("Tropo correction meters: ", String.valueOf(pseudosat.getTroposphericCorrectionMeters()));
                            double alpha[] = navMsg.iono.alpha;
                            double beta[] = navMsg.iono.beta;
                            pseudosat.computeIonosphericCorrection_GPS(alpha, beta);
                            Log.e("IONO correction meters: ", String.valueOf(pseudosat.getIonosphericCorrectionSeconds()*pseudosat.LIGHTSPEED));
                            // Corrected pseudorange
                            pseudosat.computeCorrectedRange();
                            Log.e("CORRECTED RANGE: ", String.valueOf(pseudosat.getCorrectedRange()));
                            pseudoSats.add(pseudosat);
                            Log.e("",""); // empty line
                        }

                    }

                    /************************************************************************************
                     If there are enough satellites with pseudorange, perform linearisation and get x y z
                     ***********************************************************************************/
                    if (pseudoSats.size() > 3) {
                        Log.e("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "It's happening!!!");
                        ArrayList<double[]> satCoords = new ArrayList<>();
                        double[] correctedRanges = new double[pseudoSats.size()];
                        double[] satClockErrors = new double[pseudoSats.size()];
                        double[] userPosECEFandReceiverClockError = new double[4];
                        for (int i = 0; i < userPositionECEFmeters.length; i++) {
                            userPosECEFandReceiverClockError[i] = userPositionECEFmeters[i];
                        }
                        userPosECEFandReceiverClockError[3] = 0.0; // initial clock error

                        for (int i = 0; i < pseudoSats.size(); i++) {
                            Satellite thisSat = pseudoSats.get(i);
                            satCoords.add(thisSat.getSatPositionECEFmeters());
                            correctedRanges[i] = thisSat.getCorrectedRange();
                            satClockErrors[i] = thisSat.getSatelliteClockCorrectionMeters();// getMySatClockOffsetSeconds(thisSat.getTransmittedTime())*Satellite.LIGHTSPEED; // using my offset instead of Google's
                        }

                        userPosECEFandReceiverClockError = LeastSquares.recursiveLsq(satCoords, correctedRanges, userPosECEFandReceiverClockError, satClockErrors);
                        userPositionECEFmeters[0] = userPosECEFandReceiverClockError[0];
                        userPositionECEFmeters[1] = userPosECEFandReceiverClockError[1];
                        userPositionECEFmeters[2] = userPosECEFandReceiverClockError[2];
                        Log.e("USER X: ", String.valueOf(userPositionECEFmeters[0]/1000.0));
                        Log.e("USER Y: ", String.valueOf(userPositionECEFmeters[1]/1000.0) );
                        Log.e("USER Z: ", String.valueOf(userPositionECEFmeters[2]/1000.0) );
                        Log.e("RX ERROR meters", String.valueOf(userPosECEFandReceiverClockError[3]*Satellite.LIGHTSPEED));
                    }

                } else {
                    fullBiasNanosSet = false;
                    biasNanosSet = false;
                    Log.e("CLOCK DISCONTINUITY", "Hardware clock discontinuity is not zero.");
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

    public static double[] getUserPositionECEFmeters() {
        return userPositionECEFmeters;
    }

    public void switchConstellation(String constellation) {
        CONSTELLATION_SWITCH = constellation;
    }

    public String getConstellationSwitch() {return CONSTELLATION_SWITCH;};

    public void cellIDLocation(){
        // Update cellCID, cellMCC, cellMNC, cellID, cellLAC from Telephony API
        ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        int cellID = cellLocation.getCid();
        int cellLAC = cellLocation.getLac();
        String cellPosition = cellLocation.toString();

        //Log.e("All cell info", String.valueOf(telephonyManager.getAllCellInfo()));
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        CellInfoLte cellInfoLte = (CellInfoLte) cellInfoList.get(0);

        int cellCID = cellInfoLte.getCellIdentity().getCi();
        int cellMCC = cellInfoLte.getCellIdentity().getMcc();
        int cellMNC = cellInfoLte.getCellIdentity().getMnc();

        // Now we should have all the elements to request Google Network Location API

        for (int i = 0; i < cellInfoList.size(); i++){
            //Log.e("CELL ", String.valueOf(cellInfoList.get(i)));
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
