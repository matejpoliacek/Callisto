package com.chocolateam.galileogame;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DescriptionActivity extends AppCompatActivity {

    View aboutLayout;
    Drawable aboutBck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        aboutLayout = findViewById(R.id.aboutLayout);
        aboutBck = aboutLayout.getBackground();
        aboutBck.setAlpha(75);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        aboutBck.setAlpha(255);
        finish();
    }

    public void goBack(View view) {
        aboutBck.setAlpha(255);
        finish();
    }
}
