package com.chocolateam.galileogame;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.chocolateam.galileopvt.PvtFragment;
import com.chocolateam.galileopvt.PvtActivity;
import com.chocolateam.galileospaceship.SpaceshipViewActivity;
import com.galfins.gnss_compare.GNSSCompareInitFragment;

public class MainActivity extends AppCompatActivity {

    private boolean mLocationPermissionGranted = false;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int LOCATION_REQUEST_ID = 1;

    public static GNSSCompareInitFragment gnssInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        requestPermissionAndSetupFragments(this);

        while ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){
            requestPermissionAndSetupFragments(this);
        }

        /**
         * Initialise GNSS Compare Fragment
         */
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        gnssInit = new GNSSCompareInitFragment();
        fragmentTransaction.add(android.R.id.content, gnssInit).commit();
    }

    @Override
    protected void onResume() {
        // Start the blank fragment initiating Gal/Gps PVT on app start
        super.onResume();
        if ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            PvtFragment pvtFrag = new PvtFragment();
            fragmentTransaction.add(android.R.id.content, pvtFrag).commit();
//            Log.e("uvodny text", String.valueOf(PvtFragment.getUserLatitudeDegrees()));
        }
    }

    public void goToGame(View view) {
        Intent intent = new Intent(this, com.chocolateam.galileomap.MapWithGameActivity.class);
        intent.putExtra("location_permit", mLocationPermissionGranted);
        startActivity(intent);
    }

    public void goToMap(View view) {
        Intent intent = new Intent(this, com.chocolateam.galileomap.MapsOnlyActivity.class);
        intent.putExtra("location_permit", mLocationPermissionGranted);
        startActivity(intent);
    }

    public void goToSpaceship(View view) {
        Intent intent = new Intent(this, SpaceshipViewActivity.class);
        startActivity(intent);
    }

    public void goToPVT(View view) {
        Intent intent = new Intent(this, PvtActivity.class);
        startActivity(intent);
    }

    public void goToDesc(View view) {
        Intent intent = new Intent(this, DescriptionActivity.class);
        startActivity(intent);
    }

    private boolean hasPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permissions granted at install time.
            return true;
        }
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissionAndSetupFragments(final Activity activity) {
        if (hasPermissions(activity)) {
            return;
        } else {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, LOCATION_REQUEST_ID);
        }
    }
}

