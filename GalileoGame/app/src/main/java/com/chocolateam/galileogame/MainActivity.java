package com.chocolateam.galileogame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.chocolateam.galileospaceship.SpaceshipViewActivity;

public class MainActivity extends AppCompatActivity {

    com.chocolateam.galileopvt.PVTClass GalileoPVT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void goToMap(View view) {
        Intent intent = new Intent(this, com.chocolateam.galileomap.MapsActivity.class);
        startActivity(intent);
    }

    public void goToSpaceship(View view) {
        Intent intent = new Intent(this, SpaceshipViewActivity.class);
        startActivity(intent);
    }

    public void goToPVT(View view) {
        Intent intent = new Intent(this, com.chocolateam.galileopvt.pvtActivity.class);
        startActivity(intent);
    }
}
