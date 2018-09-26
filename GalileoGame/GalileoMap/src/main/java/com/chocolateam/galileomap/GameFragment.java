package com.chocolateam.galileomap;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Matej Poliacek on 16/02/2018.
 */


public class GameFragment extends Fragment implements Runnable {

    private final int OBSTACLE_PROPORTION = 2; // = half
    private final int COLLECT_PROPORTION = 5; // = fifth
    private int[][] playfieldArray;

    private int max_collectibles;

    private Context context;
    private FragmentActivity parentActivity;

    private String constellation = "";

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
        ((MapWithGameActivity)context).stopGame(false);
    }

    public int[][] fieldTypeGenerator(int rows, int cols){
        this.rows = rows;
        this.cols = cols;
        playfieldArray = new int[rows][cols];

        List<Integer> obstacleChooser = new ArrayList<>();
        for (int i = 0; i < rows * cols; i++) {
            obstacleChooser.add(i);
        }

        int noColletibles = rows*cols/COLLECT_PROPORTION;

        assert rows*cols > ((rows * cols) / OBSTACLE_PROPORTION) + noColletibles + 1 : "Too many objects to be generated";

        if (checkSize() == 1) {

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

        } else {
            // If we don't have valid size, return empty array
            playfieldArray = null;
        }

        return playfieldArray;
    }

    public int[][] getPlayfieldArray () {
        return playfieldArray;
    }

    public boolean isSizeValid() {

        boolean validGame = false;

        if (checkSize() == 0) {
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
        } else if (checkSize() == -1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Playing area too big!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            validGame = true;
        }
        return validGame;
    }

    /**
     * Method to check if the size of the playing area is valid.
     *
     * @return integer indicating check result: -1 for too big, 0 for too small, 1 for valid
     */
    private int checkSize() {

        int validGame = 1;

        if (rows < 3 || cols < 3) {
            validGame = 0; // too small
        } else if (rows > 15 || cols > 15) {
            validGame = -1; // too big
        }

        return validGame;
    }

    public boolean isLocationValid() {

        boolean validGame = true;

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
        boolean finished = false;
        int collected = 0;

        ScoreClass scoreObj = new ScoreClass(playfieldArray.length * playfieldArray[0].length);

        max_collectibles = collectsPointList.size();

        LatLng currentLatLng;

        try {
            while (playing) {
                Thread.sleep(100);

                currentLatLng = new LatLng(playerLocation.getLatitude(), playerLocation.getLongitude());

                // Hit finish
                if (collected == max_collectibles && PolyUtil.containsLocation(currentLatLng, finish, false)) {
                    playing = false;
                    won = true;
                    finished = true;
                    System.out.println("Hit finish");
                    Thread.currentThread().interrupt();
                } else {
                    // Check if out of bounds
                    if (!PolyUtil.containsLocation(currentLatLng, areaPoints, false)) {
                        System.out.println("Out of bounds");
                        scoreObj.applyObstaclePenalty();
                    } else {
                        // Hit an obstacle
                        for (int i = 0; i < obstaclePointList.size(); i++) {
                            if (PolyUtil.containsLocation(currentLatLng, obstaclePointList.get(i), false)) {
                                scoreObj.applyObstaclePenalty();
                                System.out.println("Hit obstacle");
                                break;

                            } else {
                                // Hit a collectible
                                for (int j = 0; j < collectsPointList.size(); j++) {
                                    if (PolyUtil.containsLocation(currentLatLng, collectsPointList.get(j).getPoints(), false)) {
                                        scoreObj.increaseScore();
                                        // Run on UI thread to remove polygon
                                        // These variables are declared final to be able to pass them to the inner class
                                        final int rowIndex = collectsPointList.get(j).getRow();
                                        final int colIndex = collectsPointList.get(j).getCol();

                                        ((MapWithGameActivity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((MapWithGameActivity) context).removeMapObjectByIndex(rowIndex, colIndex);
                                            }
                                        });

                                        collectsPointList.remove(j);
                                        collected++;

                                        break;

                                    }
                                }
                            }
                        }
                    }
                }
                // update score info
                final int points = scoreObj.getPoints();
                final int secs_passed = scoreObj.getTimeSecs();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MapWithGameActivity) context).showScore(points, secs_passed);
                    }
                });
                // check if the game is lost
                /** TODO: DISABLED FOR NOW, RE-ENABLE ONCE REDESIGNED
                if (scoreObj.getTimeSecs() <= 0) {
                    playing = false;
                    won = false;
                    finished = true;
                    Thread.currentThread().interrupt();
                } else { // if not, apply time penalty
                    scoreObj.applyTimePenalty();
                }
                 **/
            }

        } catch (InterruptedException e) {
            // do nothing
        } finally {
            if (finished) {
                Intent scoreIntent = new Intent(context, SummaryActivity.class);
                scoreIntent.putExtra("won", won);
                scoreIntent.putExtra("score", scoreObj.getPoints());
                scoreIntent.putExtra("time", scoreObj.getTimeFormatted());
                scoreIntent.putExtra("constellation", constellation);
                startActivity(scoreIntent);
            }

            // UI cleanup
            ((MapsActivity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MapWithGameActivity) context).stopGame(true);
                }
            });
            ((MapWithGameActivity) context).finish();
        }
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void setPlayerLocation(Location location) {
        this.playerLocation = location;
    }

    public void setAreaPoints(LatLng startLocation, LatLng endLocation) {
        this.areaPoints = PointTools.getPoints(startLocation, endLocation);
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

    public void setConstellation(String constellation) {this.constellation = constellation;}
}
