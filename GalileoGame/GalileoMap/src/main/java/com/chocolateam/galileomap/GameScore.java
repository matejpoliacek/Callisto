package com.chocolateam.galileomap;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Lionel Garcia on 23/03/2018.
 */

public class GameScore extends ConstraintLayout {

    TextView mscore;

    public GameScore(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.in_game_score, this, true);

        mscore= findViewById(R.id.score);
//        mcompassArrow = findViewById(R.id.compass_arrow);
    }

    public GameScore(Context context) {
        this(context, null);
    }

    public void setMscore(int score){
        mscore.setText(String.valueOf(score));
    }

    public int getMscore(){

        return 0;
    }
}
