package com.chocolateam.galileomap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matej Poliacek on 21/02/2018.
 */

public class MapObjectWithIndex {

    private int row;
    private int col;
    private List<LatLng> points = new ArrayList<>();

    public MapObjectWithIndex(int row, int col, List<LatLng> points) {
        this.row = row;
        this.col = col;
        for (int i = 0; i < points.size(); i++) {
            this.points.add(points.get(i));
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public List<LatLng>  getPoints() {
        return points;
    }
}
