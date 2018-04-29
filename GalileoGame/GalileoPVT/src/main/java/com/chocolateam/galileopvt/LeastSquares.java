package com.chocolateam.galileopvt;

/**
 * Created by Matej Poliacek on 18/04/2018.
 */

import java.util.ArrayList;

import Jama.*;

public class LeastSquares {

    static final double C = 299792458.0;

    /**
     *
     * @param satCoords     calculated coordinates of the available satellites
     * @param pseudoranges  pseudoranges for each available satellite
     * @param initialState  array of length 4 containing the initial position - ECEF coordinates x, y, z and the receiver clock error (in this order)
     * @param svClockError  clock error of each available satellite
     * @return              the calculated difference in position and clock error
     */


    public static double[] lsq(ArrayList<double[]> satCoords, double[] pseudoranges, double[] initialState, double[] svClockError) {

        double initialClockError = initialState[3];

        Matrix A = new Matrix(satCoords.size(), 4);
        Matrix b = new Matrix(satCoords.size(), 1); // b = vector of delta_P's

        for (int i = 0; i < satCoords.size(); i++) {

            double r_hat = Math.sqrt(Math.pow((satCoords.get(i)[0]) - initialState[0] , 2) + Math.pow((satCoords.get(i)[1]) - initialState[1], 2)
                    + Math.pow((satCoords.get(i)[2]) - initialState[2], 2));

            double p_calc = 0;

            for (int j = 0; j < 3; j++) {

                double a_ij = (satCoords.get(i)[j] - initialState[j]) / r_hat;
                A.set(i, j, a_ij);
                p_calc += a_ij*(satCoords.get(i)[j] - initialState[j]);
            }

            p_calc += (-1.0)*svClockError[i] + initialClockError;

            A.set(i, 3, -1.0);
            b.set(i, 0, p_calc - pseudoranges[i]); // aka delta-p vector
        }


        // once we have b we can then do least squares

        Matrix A_t = A.transpose();
        Matrix inverse = (A_t.times(A)).inverse();
        Matrix x_hat = new Matrix(4,1);
        x_hat = inverse.times(A_t).times(b);

        double[] x_hat_array = new double[4];

        for (int i = 0; i < 4; i++) {
            x_hat_array[i] = x_hat.get(i, 0);
        }

        return x_hat_array;

    }


    public static double[] recursiveLsq(ArrayList<double[]> satCoords, double[] pseudoranges, double[] initialState, double[] svClockError) {
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
        double[] result = new double[initialState.length];

        while (largeDiff) {

            result = lsq(satCoords, pseudoranges, initialState, svClockError);

            double[] old_state = new double[initialState.length];
            old_state[0] = initialState[0];
            old_state[1] = initialState[1];
            old_state[2] = initialState[2];
            old_state[3] = initialState[3];

            initialState[0] += result[0];
            initialState[1] += result[1];
            initialState[2] += result[2];
            initialState[3] += result[3];

            // check if we need to iterate again
            double distance = Math.sqrt(Math.pow(old_state[0] - initialState[0] , 2) + Math.pow(old_state[1] - initialState[1], 2)
                    + Math.pow(old_state[2] - initialState[2], 2));


            if (distance < 0.1) {
                largeDiff = false;
            }
        }

        double[] result_vector = new double[4];
        result_vector[0] = initialState[0];
        result_vector[1] = initialState[1];
        result_vector[2] = initialState[2];
        result_vector[3] = initialState[3];

        return result_vector;
    }

}
