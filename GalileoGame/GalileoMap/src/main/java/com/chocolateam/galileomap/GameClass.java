package com.chocolateam.galileomap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Matej Poliacek on 16/02/2018.
 */


public class GameClass extends Thread {

    private final int OBSTACLE_PROPORTION = 2; // = half
    private int[][] playfieldArray;

    private Context context;

    // game boundaries
    /**
    private LatLng upLeft;
    private LatLng upRight;
    private LatLng downRight;
    private LatLng downLeft;
    **/
    private List<LatLng> areaPoints;
    private List<List<LatLng>> obstaclePolys = new ArrayList<>();
    private List<Polygon> collectsPolys = new ArrayList<>();

    private Location playerLocation;

    private int rows;
    private int cols;

    private boolean playing;
    private GoogleMap mMap;

    public GameClass(LatLng startLocation, LatLng endLocation, Context context, GoogleMap mMap) {
        this.areaPoints = PointTools.getPoints(startLocation, endLocation);
        /** reference:
        upLeft = points[0];
        upRight = points[1];
        downRight = points[2];
        downLeft = points[3];
        **/

        this.context = context;
        this.mMap = mMap;
    }

    public int[][] fieldTypeGenerator(int rows, int cols) {

        this.rows = rows;
        this.cols = cols;

        playfieldArray = new int[rows][cols];

        List<Integer> obstacleChooser = new ArrayList<>();
        for (int i = 0; i < rows*cols; i++) {
            obstacleChooser.add(i);
        }

        int noColletibles = 3;

        Collections.shuffle(obstacleChooser);
        obstacleChooser = obstacleChooser.subList(0, ((rows*cols)/OBSTACLE_PROPORTION)+noColletibles);

        for (int i = 0; i < noColletibles; i++) {
            playfieldArray[obstacleChooser.get(i)/cols][obstacleChooser.get(i) % cols]= 2;
        }

        obstacleChooser = obstacleChooser.subList(noColletibles, obstacleChooser.size());

        for (int i = 0; i < obstacleChooser.size(); i++) {
            playfieldArray[obstacleChooser.get(i)/cols][obstacleChooser.get(i) % cols]= 1;
        }

        return playfieldArray;
    }

    public int[][] getPlayfieldArray() {
        return playfieldArray;
    }

    public boolean isValidGame() {

        boolean validGame = true;

        if (rows < 3 || cols < 3) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Playing area too small!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            validGame = false;
        }
        if (!PolyUtil.containsLocation(new LatLng(playerLocation.getLatitude(), playerLocation.getLongitude()), areaPoints, false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Make sure you're inside the desired playing area")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            validGame = false;
        }

        return validGame;
    }

    public void addObstacle(Polygon obstacle) {

        List<LatLng> points = new ArrayList<>();
        // Extract points from received polygon into a list
        for (int i = 0; i < obstacle.getPoints().size(); i++) {
            points.add(obstacle.getPoints().get(i));
        }
        // Store points in the list of obstacles
        obstaclePolys.add(points);
    }

    public void addCollectible(Polygon collectible) {

        collectsPolys.add(collectible);
    }

    public void run() {
       try {
            while (playing){
                Thread.sleep(2000);

                for (int i = 0; i < obstaclePolys.size(); i++) {
                    if (PolyUtil.containsLocation(new LatLng(playerLocation.getLatitude(), playerLocation.getLongitude()), obstaclePolys.get(i), false)) {
                      playing = false;
                      break;
                    }
                }
                collectsPolys.get(0).remove();
            }

        } catch (InterruptedException e) {
            System.err.println("Thread stopped");
        } finally {
          // give some game summary - TODO: create new activity that will have a window with final score, etc
       }
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void setPlayerLocation(Location location) {
        this.playerLocation = location;
    }

}
