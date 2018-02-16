package com.chocolateam.galileomap;

import android.content.SyncStatusObserver;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Created by Matej Poliacek on 13/02/2018.
 */

public class DrawClass {

    private LatLng upLeft;
    private LatLng upRight;
    private LatLng downLeft;
    private LatLng downRight;

    private double longDist;
    private double latDist;

    private int cols;
    private int rows;

    private final int HOLO_BLUE_FILL = Color.argb(100, 51, 181, 229);
    private final int HOLO_BLUE_STROKE = Color.argb(150, 51, 181, 229);
    private final int RED_FILL = Color.argb(125, 255, 0, 0);
    private final int RED_STROKE = Color.argb(175, 255, 0, 0);

    // one "box" of playing field in metres
    private final int FIELD_SIZE = 10;

    public PolygonOptions drawRectangle(LatLng startLocation, LatLng endLocation) {

        //Drawing a rectangle also stores the delimiting points in the class for further use
        upLeft = new LatLng(Math.max(startLocation.latitude, endLocation.latitude), Math.min(startLocation.longitude, endLocation.longitude));
        upRight = new LatLng(Math.max(startLocation.latitude, endLocation.latitude), Math.max(startLocation.longitude, endLocation.longitude));
        downLeft = new LatLng(Math.min(startLocation.latitude, endLocation.latitude), Math.max(startLocation.longitude, endLocation.longitude));
        downRight = new LatLng(Math.min(startLocation.latitude, endLocation.latitude), Math.min(startLocation.longitude, endLocation.longitude));
        //Also calculate and store distances
        setPlayfieldSize();

        setRows((int)longDist / FIELD_SIZE);
        setCols((int)latDist / FIELD_SIZE);

        PolygonOptions options = new PolygonOptions();

        // Points must be in order
        options.add(upLeft, upRight, downLeft, downRight);

        options.fillColor(HOLO_BLUE_FILL);
        options.strokeColor(HOLO_BLUE_STROKE);
        options.strokeWidth(10);

        return options;
    }

    public PolygonOptions[][] drawObstacles() {

        double longRemain = longDist % FIELD_SIZE;
        double latRemain = latDist % FIELD_SIZE;

        double longOffset = FIELD_SIZE/2;
        double latOffset = FIELD_SIZE/2;
        // generate an array with rows and cols, then offset it from the left by longRemain /2 and from the top by latRemain/2
        // select fields to contain obstacles

        PolygonOptions[][] obstacles = new PolygonOptions[rows][cols];

        System.out.println("longRemain: " + longRemain + " latRemain: " + latRemain);


        // TODO: the generated obstacles are not precise vertically
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                // move along latitude, i.e. east
                LatLng pt1 = movePoint(upLeft,(longRemain / 2) + (i * FIELD_SIZE) + (longOffset/2), 90);

                // move along longitude, i.e. south
                pt1 = movePoint(pt1,(latRemain / 2) + (j * FIELD_SIZE) + (latOffset/2) , 180);


                // pt2 is pt1 translated east
                LatLng pt2 = movePoint(pt1, longOffset, 90);

                // pt3 is pt2 translated south
                LatLng pt3 = movePoint(pt2, latOffset, 180);

                LatLng pt4 = new LatLng(pt3.latitude, pt1.longitude);

                /** not working ATM
                // a horrible hack to stop obstacles from being generated out of bounds
                // TODO something?
                if (pt4.latitude < downLeft.latitude) {
                    setRows(i-1);
                    break;
                }
                **/

                obstacles[i][j] = new PolygonOptions();

                obstacles[i][j].add(pt1, pt2, pt3, pt4);

                obstacles[i][j].fillColor(RED_FILL);
                obstacles[i][j].strokeColor(RED_STROKE);
                obstacles[i][j].strokeWidth(10);
            }
        }

        return obstacles;
    }

    private void setPlayfieldSize() {
        // calculate longitudal distance
        this.longDist = CalculationByDistance(upLeft, upRight);
        // calculate
        this.latDist = CalculationByDistance(upLeft, downLeft);
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

    private double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        // result in metres
        return  Radius * c * 1000;
    }

    private LatLng movePoint(LatLng origin, double distanceInMetres, double bearing) {
        /** bearing - an angle, direction towards which you want to move the point.
         * 0 is towards the North, 90 - East, 180 - South, 270 - West.
         * And all between, i.e. 45 is North East.
         */

        double brngRad = Math.toRadians(bearing);
        double latRad = Math.toRadians(origin.latitude);
        double lonRad = Math.toRadians(origin.longitude);
        int earthRadiusInMetres = 6371000;
        double distFrac = distanceInMetres / earthRadiusInMetres;

        double latitudeResult = Math.asin(Math.sin(latRad) * Math.cos(distFrac) + Math.cos(latRad) * Math.sin(distFrac) * Math.cos(brngRad));
        double a = Math.atan2(Math.sin(brngRad) * Math.sin(distFrac) * Math.cos(latRad), Math.cos(distFrac) - Math.sin(latRad) * Math.sin(latitudeResult));
        double longitudeResult = (lonRad + a + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return new LatLng(Math.toDegrees(latitudeResult), Math.toDegrees(longitudeResult));
    }
}
