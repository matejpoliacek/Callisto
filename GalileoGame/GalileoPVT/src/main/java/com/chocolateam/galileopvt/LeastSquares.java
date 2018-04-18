package com.chocolateam.galileopvt;

/**
 * Created by Matej Poliacek on 18/04/2018.
 */

import java.util.ArrayList;

import Jama.*;

public class LeastSquares {

    static final double C = 299792458.0;

    public static double[] lsq(ArrayList<double[]> satCoords, double[] pseudoranges, double[] assumedLocation, double receiverClockBias) {

        double x0 = assumedLocation[0];
        double y0 = assumedLocation[1];
        double z0 = assumedLocation[2];

        Matrix A = new Matrix(4, satCoords.size());
        Matrix b = new Matrix(satCoords.size(),1); // b = vector of delta_P's

        for (int i = 0; i < satCoords.size(); i++) {

            double r_hat = Math.sqrt(Math.pow((satCoords.get(i)[0]) - assumedLocation[0] , 2) + Math.pow((satCoords.get(i)[1]) - assumedLocation[1], 2)
                    + Math.pow((satCoords.get(i)[2]) - assumedLocation[2], 2));

            for (int j = 0; j < 3; j++) {

                double a_ij = (satCoords.get(i)[j] - assumedLocation[j]) / r_hat;

                A.set(i, j, a_ij);

            }

            A.set(i, 3, -1.0);

            double p_obs = r_hat + C*receiverClockBias;

            b.set(i, 0, (pseudoranges[i] - p_obs)); // aka delta-p vector


        }

        // once we have b we can then do least squares

        Matrix A_t = A.transpose();

        Matrix inverse = (A_t.times(A)).inverse();

        Matrix x_hat = new Matrix(4,1);

        x_hat = inverse.times(A_t.times(b));

        double[] x_hat_array = new double[4];

        for (int i = 0; i < 4; i++) {
            x_hat_array[i] = x_hat.get(i, 0);
        }

        return x_hat_array;

    }


    public static double[] recursiveLsq(ArrayList<double[]> satCoords, double[] pseudoranges, double[] assumedLocation, double receiverClockBias) {
        // start ->
        // 1. linearise(assumed) = result
        // 2. do assumed - result
        // 3. if large difference
        //		3.1 set assumed = result
        //		3.2 start from 1.
        // 4. else if small difference
        //		4.1 stop -> we have convergence
        //		4.2 return result

        boolean largeDiff = true;
        double[] result = new double[assumedLocation.length + 1];

        while (largeDiff) {

            result = lsq(satCoords, pseudoranges, assumedLocation, receiverClockBias);

            for (int i = 0; i < assumedLocation.length; i++) {
                // TODO: only comparing locations, not clock just yet
                if (Math.abs(result[i]) > 10) {
                    break;
                } else {
                    largeDiff = false;
                }
            }

            assumedLocation[0] += result[0];
            assumedLocation[1] += result[1];
            assumedLocation[2] += result[2];
            receiverClockBias += result[3];
        }


        return result;
    }

}
