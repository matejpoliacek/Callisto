package com.chocolateam.galileopvt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;

import static android.content.Context.LOCATION_SERVICE;


public class BlankFragment extends Fragment implements Runnable, LocationListener {
    private Context context;
    private LocationManager mLocationManager;
    private int satcount;
    private GnssClock receiverClock;
    private Collection<GnssMeasurement> noisySatellites;
    private Collection<GnssMeasurement> satellites;

    public BlankFragment() {
        // constructor as empty as my wallet before payday
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.run();
    }

    public void run() {
        mLocationManager =
                (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        /*
        ************************
        Location user permission
        ************************
         */
        if (ContextCompat.checkSelfPermission((pvtActivity)context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Code for when permission is not granted.
            Toast.makeText(getActivity(), "Y THO",
                    Toast.LENGTH_LONG).show();
            Thread.currentThread().interrupt();
        }

        /******************************************************************************
        GNSS Status for satellite count - why doesn't sattelite list .getsize() work?
        ******************************************************************************/
        GnssStatus.Callback gnssStatusCallBack = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged (status);
                satcount = status.getSatelliteCount();
                ((pvtActivity)context).publishSatcount(String.format("Satellite count: %d", satcount));
            }

            @Override
            public void onStarted() {
                ((pvtActivity)context).publishSatcount("started");
            }
        };
        if (ContextCompat.checkSelfPermission((pvtActivity)context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Code for when permission is not granted.
            Toast.makeText(getActivity(), "Y THO",
                    Toast.LENGTH_LONG).show();
            Thread.currentThread().interrupt();
        }
        mLocationManager.registerGnssStatusCallback(gnssStatusCallBack);
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, this
        );

        /******************************************************************************
        GNSS Measurements Event for obtaining receiver clock and satellite measurements
        ******************************************************************************/
        GnssMeasurementsEvent.Callback gnssMeasurementsEventCallback = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived (GnssMeasurementsEvent eventArgs) {
                noisySatellites = eventArgs.getMeasurements();
                // satcount = noisySatellites.size();
                //((pvtActivity)context).publishSatcount(String.format("Satellite count: %d", satcount)); TODO: Why doesn't satcount display anything if I use this instad of gnssStatus?

                receiverClock = eventArgs.getClock(); // TODO: if clock discontinuity is different from previous clock, use previous clock object and write error message
                ((pvtActivity)context).publishDiscontinuity(String.format("HW Clock discontinuity: %d", receiverClock.getHardwareClockDiscontinuityCount()));

                //   TODO: Next step: create another list of satellites ("satellites") filtered for ones with bad signal
            }
        };
        mLocationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventCallback);

    }

    public void setContext(Context context) {
        this.context = context;
    }

    /*****************************************
    LocationListener boilerplate including:
        onLocationChanged
        onStatusChanged
        onProviderEnabled
        onProviderDisabled
     ****************************************/
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
