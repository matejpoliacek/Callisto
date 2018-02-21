package com.chocolateam.galileomap;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Matej Poliacek on 16/02/2018.
 */


public class GameFragment extends Fragment implements Runnable {

    private final int OBSTACLE_PROPORTION = 2; // = half
    private int[][] playfieldArray;

    private int max_collectibles;

    private Context context;

    // game boundaries
    /**
    private LatLng upLeft;
    private LatLng upRight;
    private LatLng downRight;
    private LatLng downLeft;
    **/
    private List<LatLng> areaPoints;
    private List<List<LatLng>> obstaclePointList = new ArrayList<>();
    private List<MapObjectWithIndex> collectsPointList = new ArrayList<>();
    private List<LatLng> finish = new ArrayList<>();


    private Location playerLocation;

    private int rows;
    private int cols;

    private boolean playing;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
        ((MapsActivity)context).stopGame();
    }

    public int[][] fieldTypeGenerator(int rows, int cols){
        this.rows = rows;
        this.cols = cols;
        playfieldArray = new int[rows][cols];

        List<Integer> obstacleChooser = new ArrayList<>();
        for (int i = 0; i < rows * cols; i++) {
            obstacleChooser.add(i);
        }

        int noColletibles = 3;

        Collections.shuffle(obstacleChooser);

        // set up finish
        playfieldArray[obstacleChooser.get(0) / cols][obstacleChooser.get(0) % cols] = 3;

        // 1 as start, and +1 as end to allow for finish
        obstacleChooser = obstacleChooser.subList(1, ((rows * cols) / OBSTACLE_PROPORTION) + noColletibles + 1);

        for (int i = 0; i < noColletibles; i++) {
            playfieldArray[obstacleChooser.get(i) / cols][obstacleChooser.get(i) % cols] = 2;
        }

        obstacleChooser = obstacleChooser.subList(noColletibles, obstacleChooser.size());

        for (int i = 0; i < obstacleChooser.size(); i++) {
            playfieldArray[obstacleChooser.get(i) / cols][obstacleChooser.get(i) % cols] = 1;
        }

        return playfieldArray;
    }

    public int[][] getPlayfieldArray () {
        return playfieldArray;
    }

    public boolean isSizeValid() {

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

        return validGame;
    }

    public boolean isLocationValid() {

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
        obstaclePointList.add(points);
    }

    public void addCollectible(int row, int col, Polygon collectible) {

        List<LatLng> points = new ArrayList<>();
        // Extract points from received polygon into a list
        for (int i = 0; i < collectible.getPoints().size(); i++) {
            points.add(collectible.getPoints().get(i));
        }

        collectsPointList.add(new MapObjectWithIndex(row, col, points));
    }

    public void addFinish(Polygon finish) {
        for (int i = 0; i < finish.getPoints().size(); i++) {
            this.finish.add(finish.getPoints().get(i));
        }
    }
    public void run() {
        boolean won = false;
        int collected = 0;
        max_collectibles = collectsPointList.size();
        try {
            while (playing) {
                Thread.sleep(2000);
                // Hit finish
                if (collected == max_collectibles && PolyUtil.containsLocation(new LatLng(playerLocation.getLatitude(), playerLocation.getLongitude()), finish, false)) {
                    playing = false;
                    won = true;
                    break;
                } else {
                    // Hit an obstacle
                    for (int i = 0; i < obstaclePointList.size(); i++) {
                        if (PolyUtil.containsLocation(new LatLng(playerLocation.getLatitude(), playerLocation.getLongitude()), obstaclePointList.get(i), false)) {
                            playing = false;
                            won = false;
                            break;
                        } else {
                            // Hit a collectible
                            for (int j = 0; j < collectsPointList.size(); j++) {
                                if (PolyUtil.containsLocation(new LatLng(playerLocation.getLatitude(), playerLocation.getLongitude()), collectsPointList.get(j).getPoints(), false)) {
                                    // Run on UI thread to remove polygon

                                    // These variables are declared final to be able to pass them to the inner class
                                    final int listIndex = j;
                                    final int finalCollected = collected;

                                    ((MapsActivity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((MapsActivity) context).removeMapObjectByIndex(collectsPointList.get(listIndex).getRow(), collectsPointList.get(listIndex).getCol());
                                            // TODO: score is just a count of collected collectibles, probably do something more engaging
                                            // TODO: e.g. determine max from playfield size, reduce score with time, add points for colletibles
                                            ((MapsActivity) getActivity()).showScore(finalCollected);
                                        }
                                    });
                                    collectsPointList.remove(j);
                                    collected++;
                                }
                            }
                        }
                    }
                }
            }

        } catch (InterruptedException e) {
            System.err.println("Thread stopped");
        } finally {
            // give some game summary - TODO: create new fragment that will have a window with final score, etc
            Intent intent = new Intent(context, ScoreActivity.class);
            intent.putExtra("won", won);
            intent.putExtra("score", collected);
        }
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void setPlayerLocation(Location location) {
        this.playerLocation = location;
    }

    public void setAreaPoints(LatLng startLocation, LatLng endLocation) {
        this.areaPoints =PointTools.getPoints(startLocation, endLocation);
        /** reference:
         upLeft = points[0];
         upRight = points[1];
         downRight = points[2];
         downLeft = points[3];
         **/
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
