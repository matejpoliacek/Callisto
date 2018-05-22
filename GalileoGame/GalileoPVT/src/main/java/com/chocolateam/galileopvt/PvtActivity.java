package com.chocolateam.galileopvt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.net.MalformedURLException;

public class PvtActivity extends AppCompatActivity {

    private TextView msg_satcount;
    private TextView msg_discont;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvt);
        try {
            requestPermission();   // Creates fragment if permission granted
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void createPvtFrag() {
        FragmentManager gamefragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = gamefragmentManager.beginTransaction();
        PvtFragment pvtFrag = new PvtFragment();
        fragmentTransaction.add(android.R.id.content, pvtFrag).commit();
        pvtFrag.setContext(PvtActivity.this);
    }

    public void createCellIDFrag() throws MalformedURLException {
        FragmentManager gamefragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = gamefragmentManager.beginTransaction();
        CellID cellIDFrag = new CellID();
        fragmentTransaction.add(android.R.id.content, cellIDFrag).commit();
        cellIDFrag.setContext(PvtActivity.this);
    }
/*
    public void createNavMsgFrag(){
        FragmentManager gameFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = gameFragmentManager.beginTransaction();
        NavReader navmsgFrag = new NavReader();
        fragmentTransaction.add(android.R.id.content,navmsgFrag).commit();
        navmsgFrag.setContext(PvtActivity.this);
    }
*/
    public void requestPermission() throws MalformedURLException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // permission not granted -> request permission
            Log.e("PERMISSION RESULT:", String.valueOf("not granted, requesting..."));
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        else {
            Log.e("PERMISSION RESULT:", String.valueOf("already granted"));
            createPvtFrag();
            createCellIDFrag();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        Log.e("PERMISSION RESULT:", String.valueOf("started"));

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.e("PERMISSION RESULT:", String.valueOf("granted"));
                    createPvtFrag();
                    try {
                        createCellIDFrag();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO: return to main app menu + infotoast
                    Log.e("PERMISSION RESULT:", String.valueOf("denied"));
                }
                return;
            }
        }
    }

    public void publishSatcount(String input) {

        msg_satcount = (TextView)findViewById(R.id.text_satcount);
        msg_satcount.setText(input);
    }

    public void publishDiscontinuity(String input) {
        msg_discont = (TextView)findViewById(R.id.text_discont);
        msg_discont.setText(input);
    }

}
