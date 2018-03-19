package com.chocolateam.galileopvt;

import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class pvtActivity extends AppCompatActivity {

    private TextView msg_satcount;
    private TextView msg_discont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvt);

        // Begin Activate fragment block
        FragmentManager gamefragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = gamefragmentManager.beginTransaction();

        BlankFragment pvtFrag = new BlankFragment();
        fragmentTransaction.add(android.R.id.content, pvtFrag).commit();

        // End Activate fragment block
        pvtFrag.setContext(pvtActivity.this); //Matej's code for setting context


    }

    public void publishSatcount(String input) {

        msg_satcount = (TextView)findViewById(R.id.text_satcount);
        msg_satcount.setText(input);
    }

    public void publishDiscontinuity(String input) {
        msg_discont = (TextView)findViewById(R.id.text_discont);
        msg_discont.setText(input);
    }

    public void onFragmentInteraction(Uri uri) {

    };

}
