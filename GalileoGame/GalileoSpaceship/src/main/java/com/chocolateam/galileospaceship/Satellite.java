package com.chocolateam.galileospaceship;

/**
 * Created by lgr on 06/01/2018.
 */

import android.graphics.PointF;

/**
 * Satellite object class
 */
public class Satellite {
    private int mid;
    private int moperator;
    private int msignal;

    private float maltitude;
    private PointF mposition;

    public Satellite(int id, int operator, int signal) {
        this.mid = id;
        this.moperator = operator;
        this.msignal = signal;
    }

    public int getMoperator() {
        return moperator;
    }

    public void setMoperator(int moperator) {
        this.moperator = moperator;
    }

    public int getMsignal() {
        return msignal;
    }

    public void setMsignal(int msignal) {
        this.msignal = msignal;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public String getConstellationName(int constellation) {
        switch (this.moperator) {
            case 1:
                return "GPS";
            case 2:
                return "SBAS";
            case 3:
                return "GLONASS";
            case 4:
                return "QZSS";
            case 5:
                return "BEIDOU";
            case 6:
                return "GALILEO";
            default:
                return "UNKNOWN";
        }
    }
}
