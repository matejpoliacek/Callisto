package com.galfins.gnss_compare;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * Created by Matej Poliacek on 09/08/2018.
 */

public class StartGNSSFragment extends Fragment {
    /**
     * Initialise GNSS Compare Fragment
     */
    public static GNSSCompareInitFragment gnssInit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("START", "Starting GNSSCompare Fragment");
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        gnssInit = new GNSSCompareInitFragment();
        fragmentTransaction.add(android.R.id.content, gnssInit).commit();

        this.setRetainInstance(true);
    }
}
