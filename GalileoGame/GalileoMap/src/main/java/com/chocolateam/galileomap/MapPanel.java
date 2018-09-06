package com.chocolateam.galileomap;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

/**
 * Created by Lionel Garcia on 06/09/2018.
 */

public class MapPanel extends ConstraintLayout {

    View mView;

    public MapPanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.map_panel, this, true);

    }

    public MapPanel(Context context) {
        this(context, null);
    }

    public CheckBox getCheckBoxGPS(){
        return mView.findViewById(R.id.checkBoxGPS);
    }

    public CheckBox getCheckBoxGAL(){
        return mView.findViewById(R.id.checkBoxGAL);
    }
}
