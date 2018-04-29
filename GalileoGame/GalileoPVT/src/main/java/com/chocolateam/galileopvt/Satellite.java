package com.chocolateam.galileopvt;
import android.location.cts.asn1.supl2.rrlp_components.*;
import android.location.cts.nano.Ephemeris;
import android.util.Log;

/**
 * Created by Peter Vaník on 20/03/2018.
 * Class representing a satellite measurement which contains calculated attributes of the measurement.
 */

public class Satellite {
    public static final double NUMBERNANOSECONDSWEEK = 604800e9;
    public static final double NUMBERNANOSECONDSMILI = 1e+8;
    public static final long LIGHTSPEED = 299792458;

    private int id;
    private String constellation;
    private long fullBiasNanos;
    private long gnssTime;
    private long receivedTime;
    private long transmittedTime;
    private long milliSecondsNumberNanos;
    private long weekNumberNanos;
    private long weekNumber;
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

    // Currently GPS-specific constructor, TODO Galileo
    public Satellite(int id, String constellation, Ephemeris.GpsNavMessageProto navMsg, long fullBiasNanos, double[]userPos) {

        this.id = id;
        this.constellation = constellation;
        this.navMsg = navMsg; // works for GPS only
        this.fullBiasNanos = fullBiasNanos;
        Log.e("FULLBIAS NANOS", String.valueOf(fullBiasNanos));
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
        this.weekNumberNanos = (long) Math.floor(-fullBiasNanos/NUMBERNANOSECONDSWEEK)*(long)NUMBERNANOSECONDSWEEK;
    }

   /* // Current GPS week number
    public void computeWeekNumber(long fullBiasNanos){
        this.weekNumber = (long) Math.floor(-fullBiasNanos/NUMBERNANOSECONDSWEEK);
    }*/

    public void computeMillisecondsNumberNanos(long fullBiasNanos) {
        this.milliSecondsNumberNanos = (long) Math.floor(-fullBiasNanos/NUMBERNANOSECONDSMILI)*(long)NUMBERNANOSECONDSMILI;
    }

    // aka. measurement time
    public void computeReceivedTime(String constellation) {
        if (constellation.equals("GPS")){
            this.receivedTime = gnssTime - weekNumberNanos;
        } else if (constellation.equals("GALILEO")) {
            this.receivedTime = gnssTime - milliSecondsNumberNanos;
        };
    }

    public void computeTransmittedTime(long transmittedTime) {
        this.transmittedTime = transmittedTime;
    }

    public void computePseudoRange(){
        pseudoRange = (receivedTime - transmittedTime)/1E9*LIGHTSPEED;
    }

    public void computeSecondsNumberNanos(long milliSecondsNumberNanos) {
        this.milliSecondsNumberNanos = milliSecondsNumberNanos;
    }


    /***************************** Compute satellite position ********************************
   * @par receiverGpsTowAtTimeOfTransmissionCorrectedSec Receiver estimate of GPS time of week
   *        when signal was transmitted corrected with the satellite clock drift (seconds)
   * @par receiverGpsWeekAtTimeOfTransmission Receiver estimate of GPS week when signal was
   *        transmitted (0-1024+)
    *****************************************************************************************/
    public void computeSatPosGPS() {
        double receiverGpsTowAtTimeOfTransmissionCorrectedSec = (transmittedTime + ephemerisProto.af1)/1E9;
        try {
            posAndVel = SatellitePositionCalculator.calculateSatellitePositionAndVelocityFromEphemeris(
                    ephemerisProto,
                    receiverGpsTowAtTimeOfTransmissionCorrectedSec,
                    ephemerisProto.week,// (int)weekNumber,
                    BlankFragment.getUserPositionECEFmeters()[0], // Noordwijk 3904174
                    BlankFragment.getUserPositionECEFmeters()[1], // Noordwijk 301788
                    BlankFragment.getUserPositionECEFmeters()[2]  // Noordwijk 5017699
            );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("E X C E P T I O N", "Couldn't compute satellite position.");
        }
        xECEF = posAndVel.positionXMeters;
        yECEF = posAndVel.positionYMeters;
        zECEF = posAndVel.positionZMeters;
                Log.e("Satellite " + this.id, "");
                Log.e("X ", String.valueOf(xECEF));
                Log.e("Y ", String.valueOf(yECEF));
                Log.e("Z ", String.valueOf(zECEF));
    }

    /*****************************************************************************************
     *                      Compute corrections and corrected range
     ****************************************************************************************/

    // TODO test the three models, or use Galileo's also for GPS
    public void computeTroposphericCorrection_GPS(double userLatitudeRadians, double userHeightAboveSeaLevelMeters){
        troposphericCorrectionMeters = Corrections.computeTropoCorrection_SAAS_withMapping(userLatitudeRadians,
                userHeightAboveSeaLevelMeters, satElevationRadians);
    }

    // TODO Test the two iono models for GPS, either corrections.IonoGoGPS or google's Ionosphericmodel.Klobuchar
    public void computeIonosphericCorrection_GPS(){
        // TODO
        //ionosphericCorrectionSeconds = IonosphericModel.ionoKloboucharCorrectionSeconds(...);
    }

    /**
     * @para receiverGpsTowAtTimeOfTransmission receiver estimate of GPS time of week when signal was
     *        transmitted (seconds)
     * @para receiverGpsWeekAtTimeOfTransmission Receiver estimate of GPS week when signal was
     *      transmitted
     */
    public void computeSatClockCorrectionMeters(){
        try {
            satelliteClockCorrection = SatelliteClockCorrectionCalculator.calculateSatClockCorrAndEccAnomAndTkIteratively
                    (       ephemerisProto,
                            transmittedTime/1E9,
                            ephemerisProto.week
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
        satelliteClockCorrectionMeters = satelliteClockCorrection.satelliteClockCorrectionMeters;
    }

    /*// Computes satellite clock offset according to Navipedia
    public void computeSatClockOffset(){
        double t = transmittedTime/1E9; // seconds
        double t0 = ephemerisProto.toe;
        double a0 = ephemerisProto.af0;
        double a1 = ephemerisProto.af1;
        double a2 = ephemerisProto.af2;
        double satClockOffsetSec = a0 + a1*(t - t0) + a2*(t - t0)*(t - t0);
        Log.e("My sat clock offset in meters: ", String.valueOf(satClockOffsetSec*LIGHTSPEED));
    }

    // Computes relativistic correction according to Navipedia
    public void computeRelativisticCorrectionMeters(){
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
    }

    // Helper function for relativistic correction
    public static double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }*/

    public void computeDoppler() {
        // TODO
    }

    public void computeCorrectedRange() {
        correctedRange = pseudoRange - troposphericCorrectionMeters - satelliteClockCorrectionMeters;
        //  - LIGHTSPEED*(ionosphericCorrectionSeconds + dopplerCorrectionSeconds)
    }

    /*********************************************************************************************
     *                              Compute satellite elevation
     ********************************************************************************************/
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


    /*******************************************************************************
     *                                  Getters
     ******************************************************************************/
    public double getReceivedTime(){ return this.receivedTime; }

    public long getTransmittedTime(){ return this.transmittedTime; }

    public double getPseudoRange(){ return this.pseudoRange; }

    // Meters
    public double getCorrectedRange(){ return this.correctedRange; }

    public long getWeekNumber() { return this.weekNumber; }

    // Meters
    public double getxECEF() { return this.xECEF; }

    // Meters
    public double getyECEF() { return this.yECEF; }

    // Meters
    public double getzECEF() { return this.zECEF; }

    public double getSatelliteClockCorrectionMeters() { return this.satelliteClockCorrectionMeters; }

    public double getSatElevationRadians() { return this.satElevationRadians; }

    public double[] getSatPositionECEFmeters() {
        double[] satpos = { this.xECEF, this.yECEF, this.yECEF };
        return satpos;
    }

    public int getId() { return this.id; }
}
