package com.chocolateam.galileopvt;

import Jama.Matrix;

/**
 * Created by Matej Poliacek on 01/05/2018.
 */

public class RotationMatrix {

    public static double[] rotate(double lambda, double i, double u, double r) {

        Matrix R3_1 = setR3(-lambda);
        Matrix R1 = setR1(-i);
        Matrix R3_2 = setR3(-u);


        Matrix rk = new Matrix(3,1);
        rk.set(0, 0, r);

        Matrix result = R3_1.times(R1).times(R3_2).times(rk);

        double[] result_array = new double[result.getRowDimension()];

        for (int row = 0; row < result.getRowDimension(); row++) {
            result_array[row] = result.get(row, 0);
        }

        return result_array;
    }

    public static Matrix setR1(double theta) {
        Matrix R1 = new Matrix(3,3);

        R1.set(0, 0, 1);
        setRotationSubMatrix(R1, theta, 1, 1);

        return R1;
    }

    public static Matrix setR3(double theta) {
        Matrix R3 = new Matrix(3,3);

        setRotationSubMatrix(R3, theta, 0, 0);
        R3.set(2, 2, 1);

        return R3;
    }

    public static void setRotationSubMatrix(Matrix R, double theta, int start_i, int start_j) {
        R.set(start_i, start_j, Math.cos(theta));
        R.set(start_i, start_j+1, Math.sin(theta));
        R.set(start_i+1, start_j, (-1.0)*Math.sin(theta));
        R.set(start_i+1, start_j+1, Math.cos(theta));
    }
}
