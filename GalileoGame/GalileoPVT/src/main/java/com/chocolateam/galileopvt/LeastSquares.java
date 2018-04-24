package com.chocolateam.galileopvt;

/**
 * Created by Matej Poliacek on 18/04/2018.
 */

import java.util.ArrayList;

import Jama.*;

public class LeastSquares {

    static final double C = 299792458.0;

    public static double[] lsq(ArrayList<double[]> satCoords, double[] pseudoranges, double[] assumedLocation, double initClockError, double[] svClockError) {

        double x0 = assumedLocation[0];
        double y0 = assumedLocation[1];
        double z0 = assumedLocation[2];

        Matrix A = new Matrix(satCoords.size(), 4);
        Matrix b = new Matrix(satCoords.size(), 1); // b = vector of delta_P's

        for (int i = 0; i < satCoords.size(); i++) {

            //double r_hat = Math.sqrt(Math.pow((satCoords.get(i)[0]) - assumedLocation[0] , 2) + Math.pow((satCoords.get(i)[1]) - assumedLocation[1], 2)
            //+ Math.pow((satCoords.get(i)[2]) - assumedLocation[2], 2));

            double p_calc = 0;

            for (int j = 0; j < 3; j++) {

                double a_ij = (satCoords.get(i)[j] - assumedLocation[j]) / pseudoranges[i];

                A.set(i, j, a_ij);

                p_calc += a_ij*(satCoords.get(i)[j] - assumedLocation[j]);
            }

            A.set(i, 3, -1.0);


            p_calc += (-1.0)*svClockError[i];

            b.set(i, 0, p_calc); // aka delta-p vector


        }

        A.print(5, 5);;

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


    public static double[] recursiveLsq(ArrayList<double[]> satCoords, double[] pseudoranges, double[] assumedLocation, double initClockError, double[] svClockError) {
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

            result = lsq(satCoords, pseudoranges, assumedLocation, initClockError, svClockError);

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
            initClockError += result[3];
        }

        double[] result_vector = new double[4];
        result_vector[0] = assumedLocation[0];
        result_vector[1] = assumedLocation[1];
        result_vector[2] = assumedLocation[2];
        result_vector[3] = initClockError;

        return result_vector;
    }

}
