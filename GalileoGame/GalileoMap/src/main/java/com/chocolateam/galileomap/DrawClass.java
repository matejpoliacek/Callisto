package com.chocolateam.galileomap;

import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

/**
 * Created by Matej Poliacek on 13/02/2018.
 */

public class DrawClass {

    private LatLng upLeft;
    private LatLng upRight;
    private LatLng downRight;
    private LatLng downLeft;

    private double longDist;
    private double latDist;

    private int cols;
    private int rows;

    private final int HOLO_BLUE_FILL = Color.argb(0, 51, 181, 229);
    private final int HOLO_BLUE_STROKE = Color.argb(150, 255, 255, 255);
    private final int RED_FILL = Color.argb(0, 255, 0, 0);
    private final int RED_STROKE = Color.argb(0, 255, 0, 0);
    private final int GREEN_FILL = Color.argb(0, 0, 255, 0);
    private final int GREEN_STROKE = Color.argb(0, 0, 255, 0);
    private final int WHITE_FILL = Color.argb(20, 0, 255, 0);
    private final int WHITE_STROKE = Color.argb(0, 255, 255, 255);

    // one "box" of playing field in metres
    private final double FIELD_SIZE = 10;
    // what porportion of the above box should be covered by the obstacle (FIELD_SIZE/OBSTACLE_PROPORTION)?
    private final double OBSTACLE_PROPORTION = 2;

    public PolygonOptions drawRectangle(LatLng startLocation, LatLng endLocation) {

        //Drawing a rectangle also stores the delimiting points in the class for further use
        List<LatLng> points = PointTools.getPoints(startLocation, endLocation);
        upLeft = points.get(0);
        upRight = points.get(1);
        downRight = points.get(2);
        downLeft = points.get(3);
        //Also calculate and store distances
        setPlayfieldSize();

        setRows((int)(longDist / FIELD_SIZE));
        setCols((int)(latDist / FIELD_SIZE));

        PolygonOptions options = new PolygonOptions();

        // Points must be in order
        options.add(upLeft, upRight, downRight, downLeft);

        options.fillColor(WHITE_FILL);
        options.strokeColor(WHITE_STROKE);
        options.strokeWidth(10);

        return options;
    }

    public PolygonOptions[][] drawObstacles(int[][] playfieldArray, Location playerLocation) {

        double longRemain = longDist % FIELD_SIZE;
        double latRemain = latDist % FIELD_SIZE;

        // get values to move to the top left corner of the obstacle from the top left corner of the field itself
        double longOffset = FIELD_SIZE-(FIELD_SIZE/OBSTACLE_PROPORTION);
        double latOffset = FIELD_SIZE-(FIELD_SIZE/OBSTACLE_PROPORTION);
        // generate an array with rows and cols, then offset it from the left by longRemain /2 and from the top by latRemain/2
        // select fields to contain obstacles

        PolygonOptions[][] obstacles = new PolygonOptions[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                // move along latitude, i.e. east
                LatLng pt1 = PointTools.movePoint(upLeft,(longRemain / 2) + (i * FIELD_SIZE) + (longOffset/2), 90);

                // move along longitude, i.e. south
                pt1 = PointTools.movePoint(pt1,(latRemain / 2) + (j * FIELD_SIZE) + (latOffset/2) , 180);


                // pt2 is pt1 translated east
                LatLng pt2 = PointTools.movePoint(pt1, FIELD_SIZE/OBSTACLE_PROPORTION, 90);

                // pt3 is pt2 translated south
                LatLng pt3 = PointTools.movePoint(pt2, FIELD_SIZE/OBSTACLE_PROPORTION, 180);

                LatLng pt4 = new LatLng(pt3.latitude, pt1.longitude);

                List<LatLng> obstaclePoints = new ArrayList<>();
                obstaclePoints.add(pt1);
                obstaclePoints.add(pt2);
                obstaclePoints.add(pt3);
                obstaclePoints.add(pt4);

                if (playfieldArray[i][j] != 0) {

                    if (playfieldArray[i][j] == 3) { // Finish - we don't mind it the player starts on the finish square

                        obstacles[i][j] = new PolygonOptions();
                        obstacles[i][j].add(pt1, pt2, pt3, pt4);

                        obstacles[i][j].fillColor(WHITE_FILL);
                        obstacles[i][j].strokeColor(WHITE_STROKE);
                        obstacles[i][j].strokeWidth(10);

                        // if not finish, first check if the square we're working with clashes with the player, if yes, do nothing
                    } else if (!PolyUtil.containsLocation(new LatLng(playerLocation.getLatitude(), playerLocation.getLongitude()), obstaclePoints, false)) {

                        // if not, save the point
                        obstacles[i][j] = new PolygonOptions();
                        obstacles[i][j].add(pt1, pt2, pt3, pt4);

                        // and give it adequate properties
                        if (playfieldArray[i][j] == 1) { // Obstacle
                            obstacles[i][j].fillColor(RED_FILL);
                            obstacles[i][j].strokeColor(RED_STROKE);
                            obstacles[i][j].strokeWidth(10);
                        } else if (playfieldArray[i][j] == 2) { // Collectible
                            obstacles[i][j].fillColor(GREEN_FILL);
                            obstacles[i][j].strokeColor(GREEN_STROKE);
                            obstacles[i][j].strokeWidth(10);
                        }
                    }

                }
            }
        }
        return obstacles;
    }

    private void setPlayfieldSize() {
        // calculate longitudal distance
        this.longDist = computeDistanceBetween(upLeft, upRight);
        // calculate
        this.latDist = computeDistanceBetween(upLeft, downLeft);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return  cols;
    }

    public void setRows(int newRows) {
        this.rows = newRows;
    }

    public void setCols(int newCols) {
        this.cols = newCols;
    }
}
