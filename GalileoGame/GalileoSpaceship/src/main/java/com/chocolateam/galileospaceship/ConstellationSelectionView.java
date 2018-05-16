package com.chocolateam.galileospaceship;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.lionelgarcia.galileospaceship.R;

/**
 * Created by lgr on 20/01/2018.
 */

public class ConstellationSelectionView extends LinearLayout {

    private Boolean mswitched;
    private ImageView mbutton;
    private TextView mLabel;
    private View mView;
    private Context mContext;

    private RadioButton galButton;
    private RadioButton gpsButton;
    private RadioButton allButton;

    private Button constellationButton;

    private LinearLayout constellationSelection;

    private String mLabelText;

    public ConstellationSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.tutorial_view, this, true);

        mView = this.findViewById(R.id.main_layout);

        mbutton = mView.findViewById(R.id.button);

        gpsButton = mView.findViewById(R.id.gps_button);
        galButton = mView.findViewById(R.id.galileo_button);
        allButton = mView.findViewById(R.id.all_button);

        constellationSelection = mView.findViewById(R.id.constellation_selection);

        constellationButton = mView.findViewById(R.id.constellationButton);

    }

    public ConstellationSelectionView(Context context) {
        this(context, null);
    }

    public int getConstellation(View view) {
        if(gpsButton.isChecked()){
            return 0;
        }
        else if(galButton.isChecked()){
            return 1;
        }
        else if(allButton.isChecked()){
            return 2;
        }
        else{
            return -1;
        }
    }
}
