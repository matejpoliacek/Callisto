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

        TextView message = (TextView) findViewById(R.id.resultView);
        TextView scoreView = (TextView) findViewById(R.id.scoreView);

        Boolean won = getIntent().getExtras().getBoolean("won");
        if (won) {
            message.setText("Victory!");
            scoreView.setVisibility(View.VISIBLE);
            scoreView.setText(getIntent().getExtras().getInt("score"));
        } else {
            message.setText("Game Over!");
            scoreView.setVisibility(View.INVISIBLE);
        }

    }

    public void goBack(View view) {
        (SummaryActivity.this).finish();
    }
}
