package com.chocolateam.galileopvt;

import android.util.Log;

/**
 * Created by Peter Vanik on 20/03/2018.
 * Class containing calculated measurement attributes of a satellite
 */

public class Satellite {
    public static final double NUMBERNANOSECONDSWEEK = 604800e9;
    public static final long LIGHTSPEED = 299792458;

    private int id;
    private double gnssTime;
    private double receivedTime;
    private long transmittedTime;
    private double pseudoRange;

    private long milliSecondsNumberNanos;
    private long weekNumberNanos;

    public Satellite(int id) {
        this.id = id;
    }

    public void computeGnssTime(long timeNanos, double timeOffsetNanos, long fullBiasNanos, double biasNanos) {
        this.gnssTime = timeNanos + timeOffsetNanos - (fullBiasNanos + biasNanos);
    }

    public void computeWeekNumberNanos(long fullBiasNanos){
        this.weekNumberNanos = (long) Math.floor(-fullBiasNanos/NUMBERNANOSECONDSWEEK)*(long)NUMBERNANOSECONDSWEEK;
    }

    public void computeReceivedTime(String constellation) {
        if (constellation.equals("GPS")){
            this.receivedTime = gnssTime - weekNumberNanos;
        } else if (constellation.equals("GALILEO")) {
            // milliseconds code
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
}