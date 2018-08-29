package com.chocolateam.galileomap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by lgr on 20/01/2018.
 */

public class TutorialView extends LinearLayout {

    private Boolean mswitched;
    private ImageView mbutton;
    private TextView mLabel;
    private View mView;
    private Context mContext;
    private MapWithGameActivity mactivity;

    private RadioButton galButton;
    private RadioButton gpsButton;
    private RadioButton allButton;

    private Button constellationButton;

    private LinearLayout constellationSelection;
    private LinearLayout tutorialLayout;

    private String mLabelText;

    private Bitmap mbitmap_on;
    private Bitmap mbitmap_off;

    public TutorialView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        mbitmap_on = BitmapFactory.decodeResource(getResources(), R.drawable.game_tutorial_1);
        mbitmap_off = BitmapFactory.decodeResource(getResources(), R.drawable.game_tutorial_2);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.tutorial_view, this, true);

        mView = this.findViewById(R.id.main_layout);

        mbutton = mView.findViewById(R.id.button);

        gpsButton = mView.findViewById(R.id.gps_button);
        galButton = mView.findViewById(R.id.galileo_button);
        allButton = mView.findViewById(R.id.all_button);

        constellationSelection = mView.findViewById(R.id.constellation_selection);
        tutorialLayout = mView.findViewById(R.id.tutorial_layout);

        constellationButton = mView.findViewById(R.id.constellationButton);

        constellationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                constellationSelection.setVisibility(View.GONE);
                tutorialLayout.setVisibility(VISIBLE);
                SetState(true);
            }
        });

        tutorialLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(tutorialLayout.getVisibility() == VISIBLE){
                    changeState();
                }

            }
        });

        mswitched = false;

        SetState(mswitched);

        tutorialLayout.setVisibility(View.GONE);

    }

    public TutorialView(Context context) {
        this(context, null);
    }

    public void changeState(){

        mswitched = !mswitched;
        SetState(mswitched);

    }

    public void SetState(Boolean switched){

        mswitched = switched;

        if(switched){
            mbutton.setImageBitmap(mbitmap_on);
        }
        else{
            mbutton.setImageBitmap(mbitmap_off);
        }
    }

    public Boolean getState(){
        return mswitched;
    }

    public void setOnClickListener(OnClickListener view_listener){
        mView.setOnClickListener(view_listener);
    }

    public void setEnabled(Boolean enabled) {
        if(enabled){
            mView.setVisibility(View.VISIBLE);
        }
        else{
            mView.setVisibility(View.INVISIBLE);
        }
    }

    public void setGame(MapWithGameActivity mapsActivity){
        mactivity = mapsActivity;
    }

    public void startGame(View view) {
        mactivity.startGameViaButton(view);
    }

    public String getConst() {
        String constellation = "";
        if (gpsButton.isEnabled()) {
            constellation = "GPS";
        } else if (galButton.isEnabled()) {
            constellation = "Galileo";
        } else if (allButton.isEnabled()) {
            constellation = "ALL";
        }

        return constellation;
    }
}
