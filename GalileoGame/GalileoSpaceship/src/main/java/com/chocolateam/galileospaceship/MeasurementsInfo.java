package com.chocolateam.galileospaceship;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.galfins.gnss_compare.Constellations.SatelliteParameters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class MeasurementsInfo extends RelativeLayout {

    TextView mUTC;
    TextView mClock;
    TextView mDOP;
    TextView mGAL;
    TextView mGPS;
    TextView mQZS;
    TextView mBDS;
    TextView mGLO;

    public MeasurementsInfo(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.right_panel_screens, this, true);

        mUTC = mView.findViewById(R.id.timestamp);
        mClock = mView.findViewById(R.id.clock);
        mDOP = mView.findViewById(R.id.dop);
        mGAL = mView.findViewById(R.id.ngalileo);
        mGPS = mView.findViewById(R.id.ngps);
        mQZS = mView.findViewById(R.id.nqzs);
        mBDS = mView.findViewById(R.id.nbds);
        mGLO = mView.findViewById(R.id.nglo);
    }

    public MeasurementsInfo(Context context) {
        this(context, null);
    }

    public void setSatCounts(List<SatelliteParameters> satellites){

        int gps_count = 0;
        int glo_count = 0;
        int qzs_count = 0;
        int bei_count = 0;
        int gal_count = 0;

        for (SatelliteParameters satellite : satellites) {
            switch (satellite.getConstellationType()) {
                case 1:
                    gps_count++;
                    break;
                case 2:
                    glo_count++;
                    break;
                case 3:
                    glo_count++;
                    break;
                case 4:
                    qzs_count++;
                    break;
                case 5:
                    bei_count++;
                    break;
                case 6:
                    gal_count++;
                    break;
            }
        }

        Log.e("MEASUREMENTS-INFO", "Sizes: GPS - " + gps_count + ", Glo - " + glo_count + ", QZSS - " + qzs_count + ", Beidou - " + bei_count + ", Galileo - " + gal_count);

        final int gps_count_final = gps_count;
        final int glo_count_final = glo_count;
        final int qzs_count_final = qzs_count;
        final int bei_count_final = bei_count;
        final int gal_count_final = gal_count;

        mGPS.post(new Runnable() {
            @Override
            public void run() {
                mGPS.setText(Integer.toString(gps_count_final));
            }
        });
        mGAL.post(new Runnable() {
            @Override
            public void run() {
                mGAL.setText(Integer.toString(gal_count_final));
            }
        });
        mQZS.post(new Runnable() {
            @Override
            public void run() {
                mQZS.setText(Integer.toString(qzs_count_final));
            }
        });
        mGLO.post(new Runnable() {
            @Override
            public void run() {
                mGLO.setText(Integer.toString(glo_count_final));
            }
        });
        mBDS.post(new Runnable() {
            @Override
            public void run() {
                mBDS.setText(Integer.toString(bei_count_final));
            }
        });

    }

    public void setTimeUTC(){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        mUTC.setText(df.format(Calendar.getInstance().getTime()));
    }

    public void setTimeClock(Date initialTime){
        long millis = Calendar.getInstance().getTime().getTime() - initialTime.getTime();
        int mins = (int) (millis/(1000*60)) % 60;
        int secs = (int) ((millis/1000) % 60) % 60;
        String diff = String.format("%02d", mins) + ":" + String.format("%02d", secs);
        mClock.setText(diff);
    }
}
