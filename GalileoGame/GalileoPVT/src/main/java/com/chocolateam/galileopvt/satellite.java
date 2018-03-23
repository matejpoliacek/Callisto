package com.chocolateam.galileopvt;

/**
 * Created by Peter Vanik on 20/03/2018.
 * Class containing claculated measurement attributes of a satellite
 */

public class satellite {
    private long gnssTime;
    private long milliSecondsNumberNanos;
    private long receivedTime;
    private long transmittedTime;

    public satellite() {

    }

    /*
                    GNSSClock.TimeNanos
                   GNSSClock.FullBiasNanos
                    GNSSClock.BIasNanos
                    GNSSMeasurement.TimeOffsetNanos
                       GNSSClock.FullBiasNanos
                 */

    // TODO: change the parameters inside computeX() functions to starting points of calculations

    public long getGnssTime() {
        return gnssTime;
    }

    public long getMilliSecondsNumberNanos() {
        return milliSecondsNumberNanos;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public long getTransmittedTime() {
        return transmittedTime;
    }

    public void computeGnssTime(long gnssTime) {
        this.gnssTime = gnssTime;
    }

    public void computeSecondsNumberNanos(long milliSecondsNumberNanos) {
        this.milliSecondsNumberNanos = milliSecondsNumberNanos;
    }

    public void computeReceivedTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }

    public void computeTransmittedTime(long transmittedTime) {
        this.transmittedTime = transmittedTime;
    }
}