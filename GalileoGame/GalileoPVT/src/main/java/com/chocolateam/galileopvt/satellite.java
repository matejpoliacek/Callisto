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
    private double gnssTime;
    private double receivedTime;
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
    private double satelliteClockCorrectionMeters;
    private SatelliteClockCorrectionCalculator.SatClockCorrection satelliteClockCorrection;
    private double correctedRange;

    private NavReader navReader;
    private Ephemeris.GpsNavMessageProto navMessageProto;
    private Ephemeris.GpsEphemerisProto ephemerisProto;

    public Satellite(int id, String constellation, NavReader navReader, long time) {

        this.id = id;
        this.constellation = constellation;
        this.navReader = navReader;

        if (constellation.equals("GPS")) {
            try {
                navMessageProto = navReader.getSuplMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO this code could go into main BlankFragment to check whether the satellite is in almanac
            for (int i=0; i < navMessageProto.ephemerids.length; i++) {
                Ephemeris.GpsEphemerisProto thisSat = navMessageProto.ephemerids[i];
                if (thisSat.prn == id) {
                    ephemerisProto = thisSat;
                }
                else {
                    Log.e("EPHEM ERROR", "The satellite with this ID wasn't found in the almanac.");
                }
            }
            // Compute input time parameters
            long gpsTime = time;
            double timeDifference = receivedTime - transmittedTime;
            double receiverGpsTowAtTimeOfTransmission = gpsTime - timeDifference;
            int receiverGpsWeekAtTimeOfTransmission;
            if (gpsTime < timeDifference) {
                receiverGpsWeekAtTimeOfTransmission = (int)weekNumber - 1;
            } else {
                receiverGpsWeekAtTimeOfTransmission =(int) weekNumber;
            }

            // Compute position. Hard-coded Noordwijk. TODO Later use actual user position from BlankFragment.getX
            try {
                posAndVel = SatellitePositionCalculator.calculateSatellitePositionAndVelocityFromEphemeris(
                        ephemerisProto,
                        receiverGpsTowAtTimeOfTransmission,
                        receiverGpsWeekAtTimeOfTransmission,
                        3904174,
                        301788,
                        5017699);
            } catch (Exception e) {
                e.printStackTrace();
            }
            xECEF = posAndVel.positionXMeters;
            yECEF = posAndVel.positionYMeters;
            zECEF = posAndVel.positionZMeters;
        }
    }

    public void computeGnssTime(long timeNanos, double timeOffsetNanos, long fullBiasNanos, double biasNanos) {
        this.gnssTime = timeNanos + timeOffsetNanos - (fullBiasNanos + biasNanos);
    }

    public void computeWeekNumberNanos(long fullBiasNanos){
        this.weekNumberNanos = (long) Math.floor(-fullBiasNanos/NUMBERNANOSECONDSWEEK)*(long)NUMBERNANOSECONDSWEEK;
    }

    public void computeWeekNumber(long fullBiasNanos){
        this.weekNumberNanos = (long) Math.floor(-fullBiasNanos/NUMBERNANOSECONDSWEEK);
    }

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

    // TODO test the three models, or use Galileo's also for GPS
    public void computeTroposphericCorrection_GPS(double userLatitudeRadians, double userHeightAboveSeaLevelMeters){
        troposphericCorrectionMeters = Corrections.computeTropoCorrection_SAAS_withMapping(userLatitudeRadians,
                userHeightAboveSeaLevelMeters, satElevationRadians);
    }

    // TODO Test the two iono models for GPS, either corrections.IonoGoGPS or google's Ionosphericmodel.Klobuchar
    public void computeIonosphericCorrection_GPS(){
        // TODO Cedric's code provides parameters
        //ionosphericCorrectionSeconds = IonosphericModel.ionoKloboucharCorrectionSeconds(...);
    }

    // Satellite clock offset, drift, drift change and relativistic corrections // TODO test
    public void computeSatClockCorrectionMeters(long gpsTime){
        double timeDifference = receivedTime - transmittedTime;
        double receiverGpsTowAtTimeOfTransmission = gpsTime - timeDifference;
        double receiverGpsWeekAtTimeOfTransmission;
        if (gpsTime < timeDifference) {
            receiverGpsWeekAtTimeOfTransmission = weekNumber - 1;
        } else {
            receiverGpsWeekAtTimeOfTransmission = weekNumber;
        }
        try {
            satelliteClockCorrection = SatelliteClockCorrectionCalculator.calculateSatClockCorrAndEccAnomAndTkIteratively
                    (       ephemerisProto,
                            receiverGpsTowAtTimeOfTransmission,
                            receiverGpsWeekAtTimeOfTransmission
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
        satelliteClockCorrectionMeters = satelliteClockCorrection.satelliteClockCorrectionMeters;
    }

    public void computeDoppler() {
        // TODO
    }

    // TODO are the signs correct?
    public void computeCorrectedRange() {
        /*correctedRange = pseudoRange - troposphericCorrectionMeters - satelliteClockCorrectionMeters
                - LIGHTSPEED*(ionosphericCorrectionSeconds + dopplerCorrectionSeconds);*/
    }

    // Getters
    public double getReceivedTime(){
        return this.receivedTime;
    }

    public long getTransmittedTime(){
        return this.transmittedTime;
    }

    public double getPseudoRange(){
        return this.pseudoRange;
    }

    public double getCorrectedRange(){
        return this.correctedRange;
    }

    public long getWeekNumber() {return this.weekNumber;}
}