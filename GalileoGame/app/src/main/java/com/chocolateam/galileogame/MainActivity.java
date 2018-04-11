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

        GalileoPVT = new com.chocolateam.galileopvt.PVTClass();
    }

    public void goToGame(View view) {
        Intent intent = new Intent(this, com.chocolateam.galileomap.MapsActivity.class);
        intent.putExtra("maptype", "game");
        startActivity(intent);
    }

    public void goToMap(View view) {
        Intent intent = new Intent(this, com.chocolateam.galileomap.MapsActivity.class);
        intent.putExtra("maptype", "map");
        startActivity(intent);
    }

    public void goToSpaceship(View view) {
        Intent intent = new Intent(this, SpaceshipViewActivity.class);
        startActivity(intent);
    }
}
