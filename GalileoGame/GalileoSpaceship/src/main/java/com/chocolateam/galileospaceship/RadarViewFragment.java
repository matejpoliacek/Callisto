package com.chocolateam.galileospaceship;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.lionelgarcia.galileospaceship.R;

/**
 * Created by Lionel Garcia on 26/01/2018.
 */

public class RadarViewFragment extends Fragment {

    View mView;
    GConstellationPanel mconstellationPannel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.radar_view, container, false);

//        mView = getView().findViewById(R.id.main_layout);

        mconstellationPannel = mView.findViewById(R.id.constellation_panel);
        ImageButton constellationPannelButton = mView.findViewById(R.id.constellation_panel_button);

        constellationPannelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mconstellationPannel.deploy();
            }
        });

        return mView;
    }
}

