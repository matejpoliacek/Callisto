package com.chocolateam.galileomap;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matej Poliacek on 19/02/2018.
 */

public class PointTools {

    public static List<LatLng> getPoints(LatLng startLocation, LatLng endLocation) {
        List<LatLng> points = new ArrayList<>();
        points.add(new LatLng(Math.max(startLocation.latitude, endLocation.latitude), Math.min(startLocation.longitude, endLocation.longitude)));
        points.add(new LatLng(Math.max(startLocation.latitude, endLocation.latitude), Math.max(startLocation.longitude, endLocation.longitude)));
        points.add(new LatLng(Math.min(startLocation.latitude, endLocation.latitude), Math.max(startLocation.longitude, endLocation.longitude)));
        points.add(new LatLng(Math.min(startLocation.latitude, endLocation.latitude), Math.min(startLocation.longitude, endLocation.longitude)));

        return points;
    }

    /** Replaced by google maps utility method

    public static double CalculationByDistance(LatLng StartP, LatLng EndP) {
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
    **/

    public static LatLng movePoint(LatLng origin, double distanceInMetres, double bearing) {
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
