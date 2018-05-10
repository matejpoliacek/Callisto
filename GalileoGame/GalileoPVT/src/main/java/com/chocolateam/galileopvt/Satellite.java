package com.chocolateam.galileopvt;
import android.location.GnssMeasurement;
import android.location.cts.asn1.supl2.rrlp_components.*;
import android.location.cts.nano.Ephemeris;
import android.util.Log;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

/**
 * Created by Peter Vaník on 20/03/2018.
 * Class representing a satellite which contains calculated attributes of the measurement.
 */

public class Satellite {
    public static final long NUMBERNANOSECONDSWEEK = TimeUnit.DAYS.toNanos(7);
    public static final long NUMBERNANOSECONDS100MILI = TimeUnit.MILLISECONDS.toNanos(100);
    public static final long WEEKSEC = 604800;
    public static final long LIGHTSPEED = 299792458;
    private static final double UNIVERSAL_GRAVITATIONAL_PARAMETER_M3_SM2 = 3.986005e14;
    private static final double EARTH_ROTATION_RATE_RAD_PER_SEC = 7.2921151467e-5;

    private int id;
    private String constellation;
    private int state;
    private long fullBiasNanos;
    private long gnssTime;
    private long receivedTime;
    private long transmittedTime;
    private long milliSecondsNumberNanos;
    private long weekNumberNanos;
    private double pseudoRange;

    private SatellitePositionCalculator.PositionAndVelocity posAndVel;
    private double satElevationRadians;
    private double xECEF;
    private double yECEF;
    private double zECEF;

    private double troposphericCorrectionMeters;
    private double ionosphericCorrectionSeconds;
    private double relativisticCorrectionMeters;
    private double satelliteClockCorrectionMeters;
    private SatelliteClockCorrectionCalculator.SatClockCorrection satelliteClockCorrection;
    private double correctedRange;

    private Ephemeris.GpsNavMessageProto navMsg;
    private Ephemeris.GpsEphemerisProto ephemerisProto;
    private EcefToTopocentricConverter.TopocentricAEDValues elevationAzimuthDist;

    private double[] userPositionTempECEFMeters;
    private double transmittedTimeCorrectedSeconds;

    public Satellite(int id, String constellation, Ephemeris.GpsNavMessageProto navMsg, long fullBiasNanos, double[]userPos, int state) {

        this.id = id;
        this.constellation = constellation;
        this.state = state;
        this.navMsg = navMsg; // works for GPS only
        this.fullBiasNanos = fullBiasNanos;
        this.userPositionTempECEFMeters = userPos;
        Log.e("SAT ID: ", String.valueOf(this.id));

        // Iterates over list of satellites in the navigation message to check if satellite with provided ID exists in the ephemerids list
        boolean satFound = false;
        for (int i = 0; i < navMsg.ephemerids.length; i++) {
            Ephemeris.GpsEphemerisProto thisSat = navMsg.ephemerids[i];
            if (thisSat.prn == id) {
                ephemerisProto = thisSat;
                satFound = true;
            }
        }
        if (satFound == false) {
            Log.e("SAT ID ERROR", "The satellite with this id " + String.valueOf(id) + " wasn't found in the almanac.");
        }
    }

    /********************************************** Compute times and pseudorange *************************************************
     * @param timeNanos Length of time android device has been powered on (receiver's internal hardware clock value)
     * @param timeOffsetNanos Time offset at which the measurement was taken in nanoseconds (for sub-ns precision)
     * @param fullBiasNanos Difference between TimeNanos inside the GPS receiver and the true GPS time since 0000Z, 6 January 1980.
                            If GNSS time was computed with a non-GPS satellite, an offset will be required to align to GPS time.
     * @param biasNanos Clock’s sub-nanosecond bias (for sub-ns precision)
     */
    public void computeGnssTime(long timeNanos, double timeOffsetNanos, long fullBiasNanos, double biasNanos) {
        this.gnssTime = timeNanos + (long)timeOffsetNanos - (fullBiasNanos + (long)biasNanos);
    }

    // Number of nanoseconds that have passed from the beginning of GPS time to the current week number
    public void computeWeekNumberNanos(long fullBiasNanos){
        this.weekNumberNanos = (-fullBiasNanos/NUMBERNANOSECONDSWEEK)*NUMBERNANOSECONDSWEEK;
    }

    public void computeMillisecondsNumberNanos(long fullBiasNanos) {
        this.milliSecondsNumberNanos = (-fullBiasNanos/NUMBERNANOSECONDS100MILI)*NUMBERNANOSECONDS100MILI;
    }

    // aka. measurement time
    public void computeReceivedTime() {
        if (
                (state & GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK) == GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK
                        && constellation.equals("GALILEO")) {
            this.receivedTime = gnssTime - milliSecondsNumberNanos;
        }
        else {
            this.receivedTime = gnssTime - weekNumberNanos;
        }
    }

    public void computeTransmittedTime(long transmittedTime) {
        this.transmittedTime = transmittedTime;
    }

    public void computePseudoRange(){
        if (
                (state & GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK) == GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK
                && constellation.equals("GALILEO")) {
            pseudoRange = (gnssTime - transmittedTime) % NUMBERNANOSECONDS100MILI; // TODO test
        }
        else {
            pseudoRange = (receivedTime - transmittedTime)/1E9*LIGHTSPEED;
        }
    }

    /*****************************************************************************************
     *                      Compute satellite clock correction and recompute Tx
     ****************************************************************************************/
    /**
     * Computes the GPS time of week at the time of transmission and as well the corrected GPS week
     * taking into consideration week rollover. The returned GPS time of week is corrected by the
     * computed satellite clock drift.
     * @para receiverGpsTowAtTimeOfTransmission receiver estimate of GPS time of week when signal was
     *        transmitted (seconds)
     * @para receiverGpsWeekAtTimeOfTransmission Receiver estimate of GPS week when signal was
     *      transmitted (0-1024+)
     * @para rxError Receiver clock error in meters
     */
    // calculateSatPosAndResiduals
    // within lsq you must recompute satellite positions based on transmitted time-receiver clock error

    public void computeSatClockCorrectionAndRecomputeTransmissionTime(double rxError){
        try {
            GpsTimeOfWeekAndWeekNumber correctedTowAndWeek =
                    calculateCorrectedTransmitTowAndWeek(ephemerisProto, receivedTime/1E9 - rxError/LIGHTSPEED,
                            ephemerisProto.week, pseudoRange);
            transmittedTimeCorrectedSeconds = correctedTowAndWeek.gpsTimeOfWeekSeconds;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EXCEPTION","HERE!");
        }

        computeSatClockCorrectionMeters();
        Log.e("New transmitted time:", String.valueOf(transmittedTimeCorrectedSeconds));
    }

    public void computeSatClockCorrectionMeters(){
        try {
            satelliteClockCorrection = SatelliteClockCorrectionCalculator.calculateSatClockCorrAndEccAnomAndTkIteratively
                    (       ephemerisProto,
                            transmittedTime/1E9,
                            (double)ephemerisProto.week
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
        satelliteClockCorrectionMeters = satelliteClockCorrection.satelliteClockCorrectionMeters;
        // TODO Single frequency users must remove the group delay term(TGD) from the nav message to their SV clock correction term (from p. 90 of ICD-GPS-200C)
    }

    // Custom function to compute satellite clock correction (Navipedia) - alternative to computeSatClockCorrectionMeters()
    public double getMySatClockOffsetMeters(long timeOfTransmissionNanos){
        double t = timeOfTransmissionNanos/1E9; // seconds
        double t0 = ephemerisProto.toe;
        double a0 = ephemerisProto.af0;
        double a1 = ephemerisProto.af1;
        double a2 = ephemerisProto.af2;
        double satClockOffsetSec = a0 + a1*(t - t0) + a2*(t - t0)*(t - t0);
        double satClockOffsetMeters = satClockOffsetSec*LIGHTSPEED;
        double mySatClockOffset = satClockOffsetMeters + getRelativisticCorrectionMeters();
        Log.e("My sat clock offset in meters: ", String.valueOf(mySatClockOffset));
        return mySatClockOffset;
    }

    // Custom function to compute relativistic correction according to Navipedia
    public double getRelativisticCorrectionMeters(){
        double constantComponent = -4.464*(10^(-10));
        double[] satPosVector = new double[3];
        double[] satVelVector = new double[3];
        satPosVector[0] = xECEF;
        satPosVector[1] = yECEF;
        satPosVector[2] = zECEF;
        satVelVector[0] = posAndVel.velocityXMetersPerSec;
        satVelVector[1] = posAndVel.velocityYMetersPerSec;
        satVelVector[2] = posAndVel.velocityZMetersPerSec;
        double periodicComponent = -2*dotProduct(satPosVector, satVelVector)/(LIGHTSPEED*LIGHTSPEED);
        relativisticCorrectionMeters = constantComponent + periodicComponent;
        Log.e("My relativistic clock correction in meters: ", String.valueOf(relativisticCorrectionMeters));
        return relativisticCorrectionMeters;
    }

    // Helper function for relativistic correction
    public static double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    /***************************** Compute satellite position ********************************
   * @par receiverGpsTowAtTimeOfTransmissionCorrectedSec Receiver estimate of GPS time of week
   *        when signal was transmitted corrected with the satellite clock drift (seconds)
   * @par receiverGpsWeekAtTimeOfTransmission Receiver estimate of GPS week when signal was
   *        transmitted
    *****************************************************************************************/
    public void computeSatellitePosition() {
        try {
            posAndVel = SatellitePositionCalculator.calculateSatellitePositionAndVelocityFromEphemeris(
                    ephemerisProto,
                    transmittedTimeCorrectedSeconds,
                    ephemerisProto.week,
                    BlankFragment.getUserPositionECEFmeters()[0],//3904174BlankFragment.getUserPositionECEFmeters()[0]
                    BlankFragment.getUserPositionECEFmeters()[1],//301788BlankFragment.getUserPositionECEFmeters()[1]
                    BlankFragment.getUserPositionECEFmeters()[2]//5017699BlankFragment.getUserPositionECEFmeters()[2]
            );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("E X C E P T I O N", "Couldn't compute satellite position.");
        }
        xECEF = posAndVel.positionXMeters;
        yECEF = posAndVel.positionYMeters;
        zECEF = posAndVel.positionZMeters;
                Log.e("X Satellite: ", String.valueOf(posAndVel.positionXMeters));
                Log.e("Y Satellite: ", String.valueOf(posAndVel.positionYMeters));
                Log.e("Z Satellite: ", String.valueOf(posAndVel.positionZMeters));
    }

    // Custom computation of calculating satellite positions based on navipedia
    public void computeMySatPos() {
        double t = transmittedTime/1E9 - satelliteClockCorrectionMeters/LIGHTSPEED;
        double toe = ephemerisProto.toe;
        double tk = t - toe;
        if (tk > 302400){
            tk -= 604800;
        } else if (tk < -302400) {
            tk += 604800;
        }

        // mean anomaly
        double m0 = ephemerisProto.m0;
        double MU = UNIVERSAL_GRAVITATIONAL_PARAMETER_M3_SM2;
        double n0 = Math.sqrt(MU) / Math.pow(ephemerisProto.rootOfA, 3.0); // computed mean motion
        double n = n0 + ephemerisProto.deltaN; // corrected mean motion
        double mk = m0 + n*tk;

        // compute Kepler's equation for eccentric anomaly Ek
        double e = ephemerisProto.e;
        double ek = kepler(mk, e);

        // true anomaly vk
        double my_a = Math.sqrt(1-e*e)*Math.sin(ek);
        double my_b = Math.cos(ek)-e;
        double vk = Math.atan(my_a / my_b);

        // argument of latitude uk
        double omega = ephemerisProto.omega;
        double cuc = ephemerisProto.cuc;
        double cus = ephemerisProto.cus;
        double uk = omega + vk + cuc*Math.cos(2*(omega+vk)) + cus*Math.sin(2*(omega+vk));

        // radial distance rk
        double crc = ephemerisProto.crc;
        double crs = ephemerisProto.crs;
        double a = ephemerisProto.rootOfA*ephemerisProto.rootOfA;
        double rk = a*(1-e*Math.cos(ek)) + crc*Math.cos(2*(omega+vk)) + crs*Math.sin(2*omega+vk);

        // inclination ik
        double i0 = ephemerisProto.i0;
        double idot = ephemerisProto.iDot;
        double cic = ephemerisProto.cic;
        double cis = ephemerisProto.cis;
        double ik = i0 + idot*tk + cic*Math.cos(2*omega + vk) + cis*Math.sin(2*(omega+vk));

        // longitude of ascending node
        double omega_e = EARTH_ROTATION_RATE_RAD_PER_SEC;
        double omega0 = ephemerisProto.omega0;
        double omegadot = ephemerisProto.omegaDot;
        double lambda_k = omega0 + (omegadot - omega_e)*tk - omega_e*toe;

        double[] mySatXyz = RotationMatrix.rotate(lambda_k, ik, uk, rk);
        /*xECEF = mySatXyz[0];
        yECEF = mySatXyz[1];
        zECEF = mySatXyz[2];*/
        Log.e("My X: ", String.valueOf(mySatXyz[0]));
        Log.e("My Y: ", String.valueOf(mySatXyz[1]));
        Log.e("My Z: ", String.valueOf(mySatXyz[2]));
    }

    // Helper function that calculates the eccentric anomaly iteratively with Wegstein's accelerator
    private double kepler(double mk, double e){
        double x,y,x1,y1,x2;
        int i;
        x = mk;
        y = mk - (x - e*Math.sin(x));
        x1 = x;
        x = y;
        for (i = 0; i <16; i++){
            x2=x1;
            x1=x;
            y1=y;
            y=mk-(x-e*Math.sin(x));
            if(Math.abs(y-y1)<1.0E-15) {
                break;
            }
            x=(x2*y-x*y1)/(y-y1);
        }
        double ek = x;
        return ek;
    }

    /*****************************************************************************************
     *                      Compute atmospheric corrections and corrected range
     ****************************************************************************************/
    // Computes the elevation angle in radians of the satellite
    public void computeSatElevationRadians() {
        double[] satposecefm = {xECEF, yECEF, zECEF};
        double[] user = userPositionTempECEFMeters;
        elevationAzimuthDist =
                EcefToTopocentricConverter.convertCartesianToTopocentericRadMeters(
                        user,
                        GpsMathOperations.subtractTwoVectors(
                                satposecefm, user
                        )
                );
        satElevationRadians = elevationAzimuthDist.elevationRadians;
    }

    public void computeTroposphericCorrection_GPS(double userLatitudeRadians, double userHeightAboveSeaLevelMeters){
        troposphericCorrectionMeters = 0.0;
        troposphericCorrectionMeters = Corrections.computeTropoCorrection_SAAS_withMapping(userLatitudeRadians,
                userHeightAboveSeaLevelMeters, satElevationRadians);
    }

    public void computeIonosphericCorrection_GPS(double[] alpha, double[] beta){
        ionosphericCorrectionSeconds = 0.0;
        ionosphericCorrectionSeconds = IonosphericModel.ionoKloboucharCorrectionSeconds(
                userPositionTempECEFMeters,
                getSatPositionECEFmeters(),
                transmittedTime/1E9,
                alpha,
                beta,
                IonosphericModel.L1_FREQ_HZ
        );
    }

    /*******************************************************************************
     *                                  Corrected range
     ******************************************************************************/
    public void computeCorrectedRange() {
        correctedRange = pseudoRange - troposphericCorrectionMeters + satelliteClockCorrectionMeters
                - LIGHTSPEED*(ionosphericCorrectionSeconds);
    }


    /*******************************************************************************
     *                                  Getters
     ******************************************************************************/
    public double getReceivedTime(){ return this.receivedTime; }

    public long getTransmittedTime(){ return this.transmittedTime; }

    public double getPseudoRange(){ return this.pseudoRange; }

    public double getCorrectedRange(){ return this.correctedRange; }

    public double getSatelliteClockCorrectionMeters() { return this.satelliteClockCorrectionMeters; }

    public double getSatElevationRadians() { return this.satElevationRadians; }

    public double[] getSatPositionECEFmeters() {
        double[] satpos = { this.xECEF, this.yECEF, this.zECEF };
        return satpos;
    }
    public double getTroposphericCorrectionMeters() { return this.troposphericCorrectionMeters;}

    public  double getIonosphericCorrectionSeconds() { return this.ionosphericCorrectionSeconds;}

    public int getId() { return this.id; }

    public long getGnssTime() {return this.gnssTime; }

    public double getTransmittedTimeCorrectedSeconds() {return  this.transmittedTimeCorrectedSeconds; }



    private static class GpsTimeOfWeekAndWeekNumber {
        /** GPS time of week in seconds */
        private final double gpsTimeOfWeekSeconds;

        /** GPS week number */
        private final int weekNumber;

        /** Constructor */
        private GpsTimeOfWeekAndWeekNumber(double gpsTimeOfWeekSeconds, int weekNumber) {
            this.gpsTimeOfWeekSeconds = gpsTimeOfWeekSeconds;
            this.weekNumber = weekNumber;
        }
    }

    private static GpsTimeOfWeekAndWeekNumber calculateCorrectedTransmitTowAndWeek(
            Ephemeris.GpsEphemerisProto ephemerisProto, double receiverGpsTowAtReceptionSeconds,
            int receiverGpsWeek, double pseudorangeMeters) throws Exception {
        // GPS time of week at time of transmission: Gps time corrected for transit time (page 98 ICD
        // GPS 200)
        double receiverGpsTowAtTimeOfTransmission =
                receiverGpsTowAtReceptionSeconds - pseudorangeMeters / LIGHTSPEED;

        // Adjust for week rollover
        if (receiverGpsTowAtTimeOfTransmission < 0) {
            receiverGpsTowAtTimeOfTransmission += WEEKSEC;
            receiverGpsWeek -= 1;
        } else if (receiverGpsTowAtTimeOfTransmission > WEEKSEC) {
            receiverGpsTowAtTimeOfTransmission -= WEEKSEC;
            receiverGpsWeek += 1;
        }

        // Compute the satellite clock correction term (Seconds)
        double clockCorrectionSeconds =
                SatelliteClockCorrectionCalculator.calculateSatClockCorrAndEccAnomAndTkIteratively(
                        ephemerisProto, receiverGpsTowAtTimeOfTransmission,
                        receiverGpsWeek).satelliteClockCorrectionMeters / LIGHTSPEED;

        // Correct with the satellite clock correction term
        double receiverGpsTowAtTimeOfTransmissionCorrectedSec =
                receiverGpsTowAtTimeOfTransmission + clockCorrectionSeconds;

        // Adjust for week rollover due to satellite clock correction
        if (receiverGpsTowAtTimeOfTransmissionCorrectedSec < 0.0) {
            receiverGpsTowAtTimeOfTransmissionCorrectedSec += WEEKSEC;
            receiverGpsWeek -= 1;
        }
        if (receiverGpsTowAtTimeOfTransmissionCorrectedSec > WEEKSEC) {
            receiverGpsTowAtTimeOfTransmissionCorrectedSec -= WEEKSEC;
            receiverGpsWeek += 1;
        }
        return new GpsTimeOfWeekAndWeekNumber(receiverGpsTowAtTimeOfTransmissionCorrectedSec,
                receiverGpsWeek);
    }
}
