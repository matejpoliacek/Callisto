package com.chocolateam.galileospaceship;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.chocolateam.galileospaceship.R;

/**
 * Created by lgr on 07/01/2018.
 */

public class LocInfo extends ConstraintLayout {

    Float mlocation;
    Float mspeed;
    Float maltitude;

    TextView mSpeedView;
    TextView maltitudeView;
    TextView mlatlongView;

    public LocInfo(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocInfo,
                0, 0);

        try {
            mlocation = a.getFloat(R.styleable.LocInfo_locationLatLong, 0);
            mspeed = a.getFloat(R.styleable.LocInfo_speed, 0);
            maltitude = a.getFloat(R.styleable.LocInfo_altitude, 0);
        } finally {
            a.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.loc_info_view, this, true);


        mSpeedView = mView.findViewById(R.id.speed);
        maltitudeView = mView.findViewById(R.id.altitude);
        mlatlongView = mView.findViewById(R.id.latlong);

        setSpeed(12.5f);
        setLatLong(10.25646f, 25.298415f);
        setAltitude(221.25f);

    }

    public LocInfo(Context context) {
        this(context, null);
    }

    public void setSpeed(float speed){

        mSpeedView.setText(String.format("%2.2f", speed) + " m/s");
    }

    public void setLatLong(float latitude, float longitude){

        mlatlongView.setText(String.format("%2.7f%n", latitude) + String.format("%2.7f", longitude));
    }

    public void setAltitude(float altitude){

        maltitudeView.setText(String.format("%2.2f", altitude) + " m");
    }
}
