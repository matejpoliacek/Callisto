package com.chocolateam.galileomap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Matej Poliacek on 16/02/2018.
 */


public class GameClass {

    private final int OBSTACLE_PROPORTION = 2; // = half
    private int[][] playfieldArray;

    public int[][] fieldTypeGenerator(int rows, int cols) {

        playfieldArray = new int[rows][cols];

        List<Integer> obstacleChooser = new ArrayList<>();
        for (int i = 0; i < rows*cols; i++) {
            obstacleChooser.add(i);
        }

        Collections.shuffle(obstacleChooser);
        obstacleChooser = obstacleChooser.subList(0, (rows*cols)/OBSTACLE_PROPORTION);

        for (int i = 0; i < obstacleChooser.size(); i++) {
            playfieldArray[obstacleChooser.get(i)/cols][obstacleChooser.get(i) % cols]= 1;
        }

        return playfieldArray;
    }

    public int[][] getPlayfieldArray() {
        return playfieldArray;
    }
}
