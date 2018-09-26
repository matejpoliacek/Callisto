package com.chocolateam.galileomap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SummaryActivity extends MapsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        TextView resultView = (TextView) findViewById(R.id.resultView);
        TextView scoreView = (TextView) findViewById(R.id.scoreView);
        TextView timeView = (TextView) findViewById(R.id.timeView);
        TextView constView = (TextView) findViewById(R.id.constText);


        Boolean won = getIntent().getExtras().getBoolean("won");
        if (won) {
            resultView.setText("Victory!");
            scoreView.setVisibility(View.VISIBLE);
            scoreView.setText(String.valueOf(getIntent().getExtras().getInt("score")));
            timeView.setText(String.valueOf(getIntent().getExtras().getString("time")));
            constView.setText(getIntent().getExtras().getString("constellation"));

        } else {
            resultView.setText("Game Over!");
            scoreView.setVisibility(View.INVISIBLE);
        }

    }

    public void goBack(View view) {
        (SummaryActivity.this).finish();
    }
}
