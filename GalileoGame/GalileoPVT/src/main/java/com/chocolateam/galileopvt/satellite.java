package com.chocolateam.galileopvt;

import android.location.cts.asn1.supl2.rrlp_components.*;
import android.util.Log;

/**
 * Created by Peter Vanik on 20/03/2018.
 * Class containing calculated measurement attributes of a satellite
 */

public class Satellite {
    public static final double NUMBERNANOSECONDSWEEK = 604800e9;
    public static final double NUMBERNANOSECONDSMILI = 1e+8;
    public static final long LIGHTSPEED = 299792458;

    private int id;
    private double gnssTime;
    private double receivedTime;
    private long transmittedTime;
    private long milliSecondsNumberNanos;
    private long weekNumberNanos;
    private double pseudoRange;

    private double satElevationRadians;
    private double xECEF;
    private double yECEF;
    private double zECEF;

    private double troposphericCorrectionMeters;
    private double ionosphericCorrectionSeconds;
    private double correctedRange;


    public Satellite(int id) {
        this.id = id;
    }

    public void computeGnssTime(long timeNanos, double timeOffsetNanos, long fullBiasNanos, double biasNanos) {
        this.gnssTime = timeNanos + timeOffsetNanos - (fullBiasNanos + biasNanos);
    }

    public void computeWeekNumberNanos(long fullBiasNanos){
        this.weekNumberNanos = (long) Math.floor(-fullBiasNanos/NUMBERNANOSECONDSWEEK)*(long)NUMBERNANOSECONDSWEEK;
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

    // TODO test best of the three models, or use Galileo's also for GPS
    public void computeTroposphericCorrection_GPS(double userLatitudeRiadians, double userHeightAboveSeaLevelMeters){
        troposphericCorrectionMeters = Corrections.computeTropoCorrection_SAAS_withMapping(userLatitudeRiadians,
                userHeightAboveSeaLevelMeters, satElevationRadians);
    }

    // TODO test the two iono models for GPS, either corrections.IonoGoGPS or google's Ionosphericmodel.Klobuchar
    public void computeIonosphericCorrection_GPS(){
        // TODO Cedric's code provides parameters
        //ionosphericCorrectionSeconds = IonosphericModel.ionoKloboucharCorrectionSeconds();
    }

    public void computeSatClockCorrection(){
        // TODO Cedric's code
    }

    public void computeDoppler() {
        // TODO me
    }

    // TODO what signs should these have? It's clear for delays but not the others
    public void computeCorrectedRange() {
        /*correctedRange = pseudoRange - troposphericCorrectionMeters
                + LIGHTSPEED*(ionosphericCorrectionSeconds + dopplerCorrectionSeconds + satClockCorrectionSeconds);*/
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
}