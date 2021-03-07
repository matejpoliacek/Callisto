package com.chocolateam.galileospaceship;

import android.content.Context;

import java.util.Random;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.galfins.gnss_compare.Constellations.SatelliteParameters;

/**
 * Created by lgr on 24/01/2018.
 */



public class RadarSatelliteTick extends RelativeLayout {

    TextView mLabel;
    ImageView mTickImage;
    View mView;
    Context mContext;

    public RadarSatelliteTick(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.satellite_radar_point, this, true);

        mView = this.findViewById(R.id.main_layout);
        mLabel = this.findViewById(R.id.label);
        mTickImage = this.findViewById(R.id.tick_image);

        Random rand = new Random();

//        Animation ViewAnimation = AnimationUtils.loadAnimation(mContext, R.anim.blink_slow);
//        ViewAnimation.setStartOffset((long) rand.nextInt(500));
//        mView.startAnimation(ViewAnimation);
    }

    public RadarSatelliteTick(Context context) {
        this(context, null);
    }

    public void setTick(SatelliteParameters satellite){

        String constellation;

        switch (satellite.getConstellationType()) {
            case 1:
                constellation = "GPS";
            case 2:
                constellation = "SBAS";
            case 3:
                constellation = "GLONASS";
            case 4:
                constellation = "QZSS";
            case 5:
                constellation = "BEIDOU";
            case 6:
                constellation = "GALILEO";
            default:
                constellation = "UNKNOWN";
        }

        String prefix = constellation.substring(0, 2);
        int tick;
        int label_color;

        tick = R.drawable.galileo_point;
        label_color = R.color.galileo_color;

        switch (satellite.getConstellationType()) {
            case 1:
                tick = R.drawable.gps_point;
                label_color = R.color.gps_color;
                break;
            case 2:
           //     tick = R.drawable.glonass_point;
           //     label_color = R.color.glonass_color;
                break;
            case 3:
                tick = R.drawable.sbas_point;
                label_color = R.color.sbs_color;
                break;
            case 4:
                tick = R.drawable.qzss_point;
                label_color = R.color.qzss_color;
                break;
            case 5:
           //     tick = R.drawable.beidou_point;
           //     label_color = R.color.beidou_color;
                break;
            case 6:
                tick = R.drawable.galileo_point;
                label_color = R.color.galileo_color;
                break;
            default:
                tick = R.drawable.galileo_point;
                label_color = R.color.galileo_color;
                break;
        }

        Log.e("operator", Integer.toString(satellite.getConstellationType()));
        setLabel(prefix+satellite.getSatId(), label_color);
        setTickImage(tick);
    }

    public void setLabel(String label, int color){

        mLabel.setText(label);
    }

    public void setTickImage(int tickImage){
        Bitmap bm = BitmapFactory.decodeResource(getResources(), tickImage);
        mTickImage.setImageBitmap(bm);
    }

    public void setX(Float x){
        this.setX(x);
    }

    public void setY(Float y) {
        this.setX(y);
    }

}
