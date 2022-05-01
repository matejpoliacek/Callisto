package com.chocolateam.galileospaceship;

import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.galfins.gnss_compare.Constellations.SatelliteParameters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Lionel Garcia on 26/01/2018.
 */

public class RadarViewFragment extends Fragment implements SensorEventListener {

    View mView;
    Boolean created = false;
    RadarView mRadar;
    MeasurementsInfo mMeasurementsInfo;
    GConstellationPanel mconstellationPannel;
    ImageButton constellationPannelButton;
    List<SatelliteParameters> satList = new ArrayList<>();

    private SensorManager sensorManager;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] R_mes = new float[9];
    private float[] I_mes = new float[9];
    private float azimuth;
    private float azimuthDeg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        mView = inflater.inflate(R.layout.radar_view, container, false);

        mconstellationPannel = mView.findViewById(R.id.constellation_panel_radar);
        constellationPannelButton = mView.findViewById(R.id.constellation_panel_button_radar);

        constellationPannelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mconstellationPannel.deploy();
            }
        });

        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mRadar = mView.findViewById(R.id.radarview);
        mMeasurementsInfo = mView.findViewById(R.id.measurements);

        ImageView topArrow = mView.findViewById(R.id.swipe_arrow_radar_top);
        ImageView bottomArrow = mView.findViewById(R.id.swipe_arrow_radar_bottom);
        ImageView shipDisabled = mView.findViewById(R.id.ship_disabled);

        topArrow.setBackgroundResource(R.drawable.ship_arrow_anim_left);
        AnimationDrawable topArrow_anim = (AnimationDrawable) topArrow.getBackground();
        topArrow_anim.start();
        bottomArrow.setBackgroundResource(R.drawable.ship_arrow_anim_left);
        AnimationDrawable bottomArrow_anim = (AnimationDrawable) bottomArrow.getBackground();
        bottomArrow_anim.start();

        // Animate hologram
        GraphicsTools.pulseAnimate(shipDisabled, 2000);

        Bundle bundle = this.getArguments();
        // Hide "Ship disabled" if appropriate
        GraphicsTools.hideShipDisabledWarning(shipDisabled, bundle);

        mconstellationPannel.checkConstellationBundle(bundle);

        created = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        /**sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
        **/
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);//, SensorManager.SENSOR_DELAY_UI);
            Log.e("COMPASS SENSOR", "Accelerometer registered");
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL);//, SensorManager.SENSOR_DELAY_UI);
            Log.e("COMPASS SENSOR", "Magnetometer registered");
        }
    }

    @Override
    public void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void addSatellites(List<SatelliteParameters> satellites) {
        satList.addAll(satellites);
    }

    public void updateSatellites() {
        mRadar.updateSatellites(satList);
    }

    public void resetSatellites() {
        satList = new ArrayList<>();
    }

    public void setSatCounts() {
        mMeasurementsInfo.setSatCounts(satList);
    }

    public void setTimeUTC(){
        mMeasurementsInfo.setTimeUTC();
    }

    public void setclock(Date initialtime){
        mMeasurementsInfo.setTimeClock(initialtime);
    }

    public void setLatLngXYZ(double lat , double lng, double ECEF_X, double ECEF_Y, double ECEF_Z) {
        mRadar.setLatLngXYZ(lat, lng, ECEF_X, ECEF_Y, ECEF_Z);
    }

    public boolean isCreated() { return created; }


    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                        * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                        * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                        * event.values[2];

                // mGravity = event.values;

                // Log.e(TAG, Float.toString(mGravity[0]));
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // mGeomagnetic = event.values;

                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
                        * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
                        * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
                        * event.values[2];
                // Log.e(TAG, Float.toString(event.values[0]));

            }

            boolean success = SensorManager.getRotationMatrix(R_mes, I_mes, mGravity,
                    mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R_mes, orientation);
                // Log.d(TAG, "azimuth (rad): " + azimuth);
                azimuth = (float) orientation[0];
                azimuthDeg = (float) Math.toDegrees(orientation[0]); // orientation
                // Log.d(TAG, "azimuth (deg): " + azimuth);
            }
        }

        Log.e("COMPASS", "Heading: " + azimuth + " rad, " + azimuthDeg + " deg");

        mRadar.setAzimuth(-azimuth);
        mRadar.drawCompassLine(-azimuth);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

