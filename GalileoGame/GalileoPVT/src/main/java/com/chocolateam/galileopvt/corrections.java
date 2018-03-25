package com.chocolateam.galileopvt;

/**
 * Created by Peter Vaník on 24/03/2018.
 */

public  final class corrections {

    public static final double STANDARD_PRESSURE = 1013.25; //[hPa]
    public static final double STANDARD_TEMPERATURE = 291.15; //[K]
    public static final double STANDARD_WATER_VAPOUR_PRESSURE = 30.3975;//[hPa]

    private corrections(){
    }

    /**
     * @return Troposphere correction value based on Saastamoinen model with inputs from:
     * http://www.navipedia.net/index.php/Galileo_Tropospheric_Correction_Model
     * and
     * GALILEO Positioning Technology edited by Jari Nurmi, Elena Simona Lohan, Stephan Sand, Heikki Hurskainen, p.153
     * and
     * https://d-nb.info/963624393/34
     * @param latitude receiver's latitude [rad]
     * @param heightAboveSeaLevel receiver's surface height above the ellipsoid [km]
     * @param satElevationAngle [rad]
     */
    public static double computeTropoCorrection_SAAS_withMapping (double latitude, double heightAboveSeaLevel, double satElevationAngle){
        double tropoCorr = 0;
        double hydrostaticDelay = 0;
        double wetDelay = 0;

        hydrostaticDelay = 0.002277*STANDARD_PRESSURE / (1-0.00266*(Math.cos(2*latitude))-0.00028*heightAboveSeaLevel);
        wetDelay = 0.002277*(1255/STANDARD_TEMPERATURE + 0.05)*STANDARD_WATER_VAPOUR_PRESSURE;

        double hMapping = 1/(Math.sin(satElevationAngle) + 0.00143/(Math.tan(satElevationAngle)+0.0445));
        double wMapping = 1/(Math.sin(satElevationAngle) + 0.00035/(Math.tan(satElevationAngle)+0.017));

        tropoCorr = hydrostaticDelay*hMapping + wetDelay*wMapping;
        return tropoCorr;
    }

    /**
     * @return Troposphere correction value based on Saastamoinen model by
     * GALILEO Positioning Technology edited by Jari Nurmi, Elena Simona Lohan, Stephan Sand, Heikki Hurskainen, p.153
     * @param satElevationAngle [rad]
     */
    public static double computeTropoCorrection_SAAS_simple(double satElevationAngle) {
        return 2.47/(Math.sin(satElevationAngle) + 0.0121);
    }

    /**
     * @return Troposphere correction value based on Saastamoinen model
     * By GoGPS:
     * Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland, Daisuke Yoshida. All Rights Reserved.
     * https://www.programcreek.com/java-api-examples/index.php?source_dir=goGPS_Java-master/src/main/java/org/gogpsproject/ReceiverPosition.java#
     * @param elevation in radians
     * @param height in meters
     */
    public static double computeTropoCorrection_SAAS_goGPS(double elevation, double height) {

        double tropoCorr = 0;

        if (height < 5000) {

            if (elevation == 0){
                elevation = elevation + 0.01;
            }

            // Numerical constants and tables for Saastamoinen algorithm
            // (troposphere correction)
            double hr = 50.0;
            int[] ha = new int[9];
            double[] ba = new double[9];

            ha[0] = 0;
            ha[1] = 500;
            ha[2] = 1000;
            ha[3] = 1500;
            ha[4] = 2000;
            ha[5] = 2500;
            ha[6] = 3000;
            ha[7] = 4000;
            ha[8] = 5000;

            ba[0] = 1.156;
            ba[1] = 1.079;
            ba[2] = 1.006;
            ba[3] = 0.938;
            ba[4] = 0.874;
            ba[5] = 0.813;
            ba[6] = 0.757;
            ba[7] = 0.654;
            ba[8] = 0.563;

            // Saastamoinen algorithm
            double P = STANDARD_PRESSURE * Math.pow((1 - 0.0000226 * height), 5.225);
            double T = STANDARD_TEMPERATURE - 0.0065 * height;
            double H = hr * Math.exp(-0.0006396 * height);

            // If height is below zero, keep the maximum correction value
            double B = ba[0];
            // Otherwise, interpolate the tables
            if (height >= 0) {
                int i = 1;
                while (height > ha[i]) {
                    i++;
                }
                double m = (ba[i] - ba[i - 1]) / (ha[i] - ha[i - 1]);
                B = ba[i - 1] + m * (height - ha[i - 1]);
            }

            double e = 0.01
                    * H
                    * Math.exp(-37.2465 + 0.213166 * T - 0.000256908
                    * Math.pow(T, 2));

            tropoCorr = ((0.002277 / Math.sin(elevation))
                    * (P - (B / Math.pow(Math.tan(elevation), 2))) + (0.002277 / Math.sin(elevation))
                    * (1255 / T + 0.05) * e);
        }

        return tropoCorr;
    }
}
