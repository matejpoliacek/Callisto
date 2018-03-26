package com.chocolateam.galileomap;

import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Random;

/**
 * Created by Matej Poliacek on 23/03/2018.
 */

public class GraphicalPolygon {

    private int rows;
    private int cols;
    private PolygonOptions[][] polyOptions;
    private Polygon[][] gameMapObjects;
    private GroundOverlay[][] gameMapOverlays;
    private int[][] playfieldArray;

    public  GraphicalPolygon(PolygonOptions[][] polyOptions, int[][] playfieldArray) {
        this.polyOptions = polyOptions;
        this.playfieldArray = playfieldArray;
    }

    public void populateMap(GoogleMap mMap) {

        int overlayImage = R.drawable.collectible_1; // Empty

        this.rows = this.polyOptions.length;
        this.cols = this.polyOptions[0].length;
        this.gameMapObjects = new Polygon[rows][cols];
        this.gameMapOverlays = new GroundOverlay[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.polyOptions[i][j] != null) {

                    switch(playfieldArray[i][j]){
                        case 1: overlayImage = randomRock(); // Obstacle
                            break;
                        case 2: overlayImage = R.drawable.collectible_1; // Collectible
                            break;
                        case 3: overlayImage = R.drawable.finish_line; // Finish Line
                    }
                    gameMapObjects[i][j] = mMap.addPolygon(polyOptions[i][j]);

                    GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                            .image(BitmapDescriptorFactory.fromResource(overlayImage))
                            .positionFromBounds(PointTools.polygonBounds(gameMapObjects[i][j]));
                    newarkMap.zIndex(5);

                    gameMapOverlays[i][j] = mMap.addGroundOverlay(newarkMap);
                }
            }
        }
    }

    public Polygon getGameMapObject(int row, int col) {
        return gameMapObjects[row][col];
    }

    public void removeGameMapObject(int row, int col) {
        gameMapObjects[row][col].remove();
        gameMapOverlays[row][col].remove();
    }

    public void removeAllObjects() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.gameMapObjects[i][j] != null) {
                    gameMapObjects[i][j].remove();
                }
                if (this.gameMapOverlays[i][j] != null) {
                    gameMapOverlays[i][j].remove();
                }

            }
        }
    }

    private int randomRock(){

        int rock;

        Random r = new Random();
        int r_int = r.nextInt(4 - 0);

        switch (r_int){
            case 0: rock = R.drawable.rock_1;
                break;
            case 1: rock = R.drawable.rock_2;
                break;
            case 2: rock = R.drawable.rock_3;
                break;
            case 3: rock = R.drawable.rock_4;
                break;
            default: rock = 0;
                break;
        }
        return rock;
    }
}
