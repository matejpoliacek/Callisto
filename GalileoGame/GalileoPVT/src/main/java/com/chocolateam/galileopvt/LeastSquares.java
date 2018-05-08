package com.chocolateam.galileopvt;

/**
 * Created by Matej Poliacek on 18/04/2018.
 */

import java.util.ArrayList;

import Jama.Matrix;

public class LeastSquares {

    static final double C = 299792458.0;

    /**
     * Weighted least squares implementation
     *
     * @param satCoords     calculated coordinates of the available satellites
     * @param pseudoranges  pseudoranges for each available satellite
     * @param initialState  array of length 4 containing the initial position - ECEF coordinates x, y, z and the receiver clock error (in this order)
     * @param svClockError  clock error of each available satellite
     * @param satElevation  elevation of each satellite
     * @return              the calculated difference in position and clock error
     */


    public static double[] lsq(ArrayList<double[]> satCoords, double[] pseudoranges, double[] initialState, double[] svClockError, double[] satElevation) {

        double initialClockError = initialState[3];

        Matrix A = new Matrix(satCoords.size(), 4);             // A = design matrix
        Matrix b = new Matrix(satCoords.size(), 1);             // b = vector of delta_rho's
        Matrix W = new Matrix(satCoords.size(), satCoords.size()); // W = weight matrix
        Matrix x_hat;                                              // x_hat = vector of results

        for (int i = 0; i < satCoords.size(); i++) {

            double r_hat = Math.sqrt(Math.pow((satCoords.get(i)[0]) - initialState[0] , 2) + Math.pow((satCoords.get(i)[1]) - initialState[1], 2)
                    + Math.pow((satCoords.get(i)[2]) - initialState[2], 2));

            double p_calc = 0;

            for (int j = 0; j < 3; j++) {

                double a_ij = (satCoords.get(i)[j] - initialState[j]) / r_hat;

                A.set(i, j, a_ij);

                p_calc += a_ij*(satCoords.get(i)[j] - initialState[j]);

            }

            // Add clock error to the calculated pseudorange
            p_calc += (-1.0)*svClockError[i] + initialClockError;

            A.set(i, 3, -1.0);

            // delta-rho = calculated pseudorange - observed pseudorange
            b.set(i, 0, p_calc - pseudoranges[i]); // aka delta-rho vector


            // set the diagonal entry of W to the associated weight
            // at the moment, this is the R matrix, which when inverted becomes the weight matrix (see below)
            W.set(i, i, (0.13 + (0.53*Math.pow(Math.E, -satElevation[i]/10.0))));

        }


        // once we have b we can then do least squares

        Matrix A_t = A.transpose();
        // R^-1 = W
        W = W.inverse();

        // Full formula: (A_t * W * A)^-1 * A_t * W * b

        // Part 1: inverse = (A_t * W * A)^-1
        Matrix inverse = ((A_t.times(W)).times(A)).inverse();

        // Part 2 added: inverse *  A_t * W * b == (A_t * W * A)^-1 * A_t * W * b
        x_hat = ((inverse.times(A_t)).times(W)).times(b);

        double[] x_hat_array = new double[4];

        for (int i = 0; i < x_hat_array.length; i++) {
            x_hat_array[i] = x_hat.get(i, 0);
        }

        return x_hat_array;

    }

    /**
     *  Repeatedly calls weighted least squares function until convergence
     *
     *  start ->
     *  1. leastsquares(assumed_location) = result
     *  2. do new_location = assumed_location + result
     *  3. if result is large (i.e. large shift from assumed/previous state)
     *      3.1 set assumed_location = new_location
     *      3.2 start over from 1.
     *  4. else if small result (small difference)
     *      4.1 stop -> we have convergence
     *      4.2 return new_location
     *
     * @param satCoords     calculated coordinates of the available satellites
     * @param pseudoranges  pseudoranges for each available satellite
     * @param initialState  array of length 4 containing the initial position - ECEF coordinates x, y, z and the receiver clock error (in this order)
     * @param svClockError  clock error of each available satellite
     * @param satElevation  elevation of each satellite
     * @return              the calculated final position and clock error
     */


    public static double[] recursiveLsq(ArrayList<double[]> satCoords, double[] pseudoranges, double[] initialState, double[] svClockError, double[] satElevation) {

        boolean largeDiff = true;
        double[] result = new double[initialState.length];

        while (largeDiff) {

            result = lsq(satCoords, pseudoranges, initialState, svClockError, satElevation);

            double[] old_state = new double[initialState.length];

            for (int i = 0; i < initialState.length; i++) {
                old_state[i] = initialState[i];
                initialState[i] += result[i];
            }

            // check if we need to iterate again
            double distance = Math.sqrt(Math.pow(old_state[0] - initialState[0] , 2) + Math.pow(old_state[1] - initialState[1], 2)
                    + Math.pow(old_state[2] - initialState[2], 2));


            if (distance < 0.1) {
                largeDiff = false;
            }
        }

        double[] result_vector = new double[initialState.length];
        for (int i = 0; i < result.length; i++) {
            result_vector[i] = initialState[i];
        }

        return result_vector;
    }

}
