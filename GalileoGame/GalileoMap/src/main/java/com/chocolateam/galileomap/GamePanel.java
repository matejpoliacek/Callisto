package com.chocolateam.galileomap;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Lionel Garcia on 22/03/2018.
 */

public class GamePanel extends ConstraintLayout {

    TextView mclock;
    ImageView mcompassArrow;

    public GamePanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.game_panel, this, true);

        mclock= findViewById(R.id.clock);
//        mcompassArrow = findViewById(R.id.compass_arrow);
    }

    public GamePanel(Context context) {
        this(context, null);
    }

    public void setCompasOrientation(double angle){

    }

    public void startClock(){

    }

    public void stopClock(){

    }

    public void getClockTime(){

    }
}
