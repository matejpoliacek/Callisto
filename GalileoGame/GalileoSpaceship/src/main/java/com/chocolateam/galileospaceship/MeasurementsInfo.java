package com.chocolateam.galileospaceship;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chocolateam.galileospaceship.R;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class MeasurementsInfo extends RelativeLayout {

    TextView mUTC;
    TextView mClock;
    TextView mDOP;
    TextView mGAL;
    TextView mGPS;

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
    }

    public MeasurementsInfo(Context context) {
        this(context, null);
    }

    public void setSatCounts(String constellationType, int numberOfSat){
        switch (constellationType){
            case "GPS":
                mGPS.setText(Integer.toString(numberOfSat));
            case "Galileo":
                mGAL.setText(Integer.toString(numberOfSat));
        }
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
