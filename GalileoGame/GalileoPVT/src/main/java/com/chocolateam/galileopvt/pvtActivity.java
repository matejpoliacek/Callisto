package com.chocolateam.galileopvt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

public class pvtActivity extends AppCompatActivity {

    private TextView msg_satcount;
    private TextView msg_discont;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvt);
        requestPermission();   // Creates fragment if permission granted
    }

    public void createPvtFrag() {
        FragmentManager gamefragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = gamefragmentManager.beginTransaction();
        BlankFragment pvtFrag = new BlankFragment();
        fragmentTransaction.add(android.R.id.content, pvtFrag).commit();
        pvtFrag.setContext(pvtActivity.this);
    }

    public void requestPermission() {
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
