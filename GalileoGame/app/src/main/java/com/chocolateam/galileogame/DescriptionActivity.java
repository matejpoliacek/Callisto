package com.chocolateam.galileogame;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

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

        TextView desc = findViewById(R.id.descText);
        desc.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backAction();
    }

    public void goBack(View view) {
        backAction();
    }

    private void backAction() {
        aboutBck.setAlpha(255);
        finish();
    }
}
