package com.chocolateam.galileogame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.chocolateam.galileomap.MapsActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void goToSpaceship(View view) {
        Intent intent = new Intent(this, com.example.lionelgarcia.galapptest2.SpaceshipViewActivity.class);
        startActivity(intent);
    }
}
