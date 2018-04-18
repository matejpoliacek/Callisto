package com.chocolateam.galileopvt;

/**
 * Created by Peter Vaník on 24/03/2018.
 */

public  final class Corrections {

    public static final double STANDARD_PRESSURE = 1013.25; //[hPa]
    public static final double STANDARD_TEMPERATURE = 291.15; //[K]
    public static final double STANDARD_WATER_VAPOUR_PRESSURE = 30.3975;//[hPa]
    // public static final double RELATIVISTIC_CLOCK_CORRECTION_CONSTANT_COMPONENT = -4.646e-10; according to ESA, not Google

    private Corrections(){
    }

    /** Source: GoGPS (Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland, Daisuke Yoshida. All Rights Reserved. )
     *  https://www.programcreek.com/java-api-examples/index.php?source_dir=goGPS_Java-master/src/main/java/org/gogpsproject/ReceiverPosition.java#
     *  @return ionosphere correction value by Klobuchar model
     */

    // how to get gpsTime: long gpsTime = receiverClock.getTimeNanos() - (long)(fullBiasNanos + biasNanos); // for goGPS iono model
    public double computeIonosphereCorrection_GoGPS (double alpha, double beta,
                                               double latitude, double longitude, double azimuth, double elevation, long gpstime) {
        double ionoCorr = 0;

        elevation = Math.abs(elevation);

        // Parameter conversion to semicircles
        double lon = longitude / 180; // geod.get(0)
        double lat = latitude / 180; //geod.get(1)
        azimuth = azimuth / 180;
        elevation = elevation / 180;

        // Klobuchar algorithm
        double f = 1 + 16 * Math.pow((0.53 - elevation), 3);
        double psi = 0.0137 / (elevation + 0.11) - 0.022;
        double phi = lat + psi * Math.cos(azimuth * Math.PI);
        if (phi > 0.416){
            phi = 0.416;
        }
        if (phi < -0.416){
            phi = -0.416;
        }
        double lambda = lon + (psi * Math.sin(azimuth * Math.PI))
                / Math.cos(phi * Math.PI);
        double ro = phi + 0.064 * Math.cos((lambda - 1.617) * Math.PI);
        double t = lambda * 43200 + gpstime;
        while (t >= 86400)
            t = t - 86400;
        while (t < 0)
            t = t + 86400;
        //double p = iono.getBeta(0) + iono.getBeta(1) * ro + iono.getBeta(2) * Math.pow(ro, 2) + iono.getBeta(3) * Math.pow(ro, 3);
        double p = 0;
        if (p < 72000)
            p = 72000;
        //double a = iono.getAlpha(0) + iono.getAlpha(1) * ro + iono.getAlpha(2) * Math.pow(ro, 2) + iono.getAlpha(3) * Math.pow(ro, 3);
        double a =0;
        if (a < 0)
            a = 0;
        double x = (2 * Math.PI * (t - 50400)) / p;
        if (Math.abs(x) < 1.57){
            ionoCorr = Satellite.LIGHTSPEED
                    * f
                    * (5e-9 + a
                    * (1 - (Math.pow(x, 2)) / 2 + (Math.pow(x, 4)) / 24));
        }else{
            ionoCorr = Satellite.LIGHTSPEED * f * 5e-9;
        }
        return ionoCorr;
    }

    /**
     * @return Troposphere correction value based on Saastamoinen model with inputs from:
     * http://www.navipedia.net/index.php/Galileo_Tropospheric_Correction_Model
     * and
     * GALILEO Positioning Technology edited by Jari Nurmi, Elena Simona Lohan, Stephan Sand, Heikki Hurskainen, p.153
     * and
     * https://d-nb.info/963624393/34
     * @param latitudeRadians receiver's latitude [rad]
     * @param heightAboveSeaLevelMeters receiver's surface height above the ellipsoid [m]
     * @param satElevationAngleRadians [rad]
     */
    public static double computeTropoCorrection_SAAS_withMapping (double latitudeRadians, double heightAboveSeaLevelMeters, double satElevationAngleRadians){
        double tropoCorr = 0;
        double hydrostaticDelay = 0;
        double wetDelay = 0;

        hydrostaticDelay = 0.002277*STANDARD_PRESSURE / (1-0.00266*(Math.cos(2*latitudeRadians))-0.00028*heightAboveSeaLevelMeters/1000);
        wetDelay = 0.002277*(1255/STANDARD_TEMPERATURE + 0.05)*STANDARD_WATER_VAPOUR_PRESSURE;

        double hMapping = 1/(Math.sin(satElevationAngleRadians) + 0.00143/(Math.tan(satElevationAngleRadians)+0.0445));
        double wMapping = 1/(Math.sin(satElevationAngleRadians) + 0.00035/(Math.tan(satElevationAngleRadians)+0.017));

        tropoCorr = hydrostaticDelay*hMapping + wetDelay*wMapping;
        return tropoCorr;
    }

    /**
     * @return Troposphere correction value based on Saastamoinen model by
     * GALILEO Positioning Technology edited by Jari Nurmi, Elena Simona Lohan, Stephan Sand, Heikki Hurskainen, p.153
     * @param satElevationAngleRadians [rad]
     */
    public static double computeTropoCorrection_SAAS_simple(double satElevationAngleRadians) {
        return 2.47/(Math.sin(satElevationAngleRadians) + 0.0121);
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
