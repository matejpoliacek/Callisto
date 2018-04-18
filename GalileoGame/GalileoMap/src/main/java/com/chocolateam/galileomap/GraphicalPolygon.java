package com.chocolateam.galileomap;

import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Created by Matej Poliacek on 23/03/2018.
 */

public class GraphicalPolygon {

    private int rows;
    private int cols;
    private PolygonOptions[][] polyOptions;
    private Polygon[][] gameMapObjects;
    private GroundOverlay[][] gameMapOverlays;

    public  GraphicalPolygon(PolygonOptions[][] polyOptions) {
        this.polyOptions = polyOptions;
    }

    public void populateMap(GoogleMap mMap) {
        this.rows = this.polyOptions.length;
        this.cols = this.polyOptions[0].length;
        this.gameMapObjects = new Polygon[rows][cols];
        this.gameMapOverlays = new GroundOverlay[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.polyOptions[i][j] != null) {
                    gameMapObjects[i][j] = mMap.addPolygon(polyOptions[i][j]);

                    GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                            .image(BitmapDescriptorFactory.fromResource(R.drawable.menu_button))
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
}
