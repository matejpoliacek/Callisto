package com.chocolateam.galileospaceship;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.lionelgarcia.galileospaceship.R;

/**
 * Created by Lionel Garcia on 25/01/2018.
 */

public class MeasurementsInfo extends RelativeLayout {

    public MeasurementsInfo(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.right_panel_screens, this, true);
    }

    public MeasurementsInfo(Context context) {
        this(context, null);
    }
}
