package com.chocolateam.galileomap;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

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

    public void setClock(int secs_passed){
        DecimalFormat df = new DecimalFormat("00");
        String minutes = df.format(secs_passed/60);
        String seconds = df.format(secs_passed%60);

        mclock.setText(minutes + ":" + seconds);
    }

    public void stopClock(){

    }

    public void getClockTime(){

    }
}
