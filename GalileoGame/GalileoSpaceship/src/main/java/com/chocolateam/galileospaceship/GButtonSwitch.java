package com.chocolateam.galileospaceship;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lionelgarcia.galileospaceship.R;

/**
 * Created by lgr on 20/01/2018.
 */

public class GButtonSwitch extends LinearLayout {

    private Boolean mswitched;
    private ImageView mbutton;
    private TextView mLabel;
    private View mView;
    private Context mContext;

    private String mLabelText;

    private Bitmap mbitmap_on;
    private Bitmap mbitmap_off;

    public GButtonSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        mbitmap_on = BitmapFactory.decodeResource(getResources(), R.drawable.on_button);
        mbitmap_off = BitmapFactory.decodeResource(getResources(), R.drawable.off_button);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ButtonSwitch,
                0, 0);

        try {
            mLabelText = a.getString(R.styleable.ButtonSwitch_label);
        } finally {
            a.recycle();
        }


        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.button_switch, this, true);

        mView = this.findViewById(R.id.main_layout);

        mView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeState();
            }
        });

        mbutton = mView.findViewById(R.id.button);
        mLabel = mView.findViewById(R.id.label);

        mLabel.setText(mLabelText);

        mswitched = false;

        SetState(mswitched);
    }

    public GButtonSwitch(Context context) {
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
            mLabel.setTextColor(ContextCompat.getColor(mContext, R.color.label_on));
        }
        else{
            mbutton.setImageBitmap(mbitmap_off);
            mLabel.setTextColor(ContextCompat.getColor(mContext, R.color.label_off));
        }
    }

    public Boolean getState(){
        return mswitched;
    }

    public void setOnClickListener(View.OnClickListener view_listener){
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

}
