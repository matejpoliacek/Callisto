package com.chocolateam.galileospaceship;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chocolateam.galileospaceship.R;
import com.galfins.gnss_compare.Constellations.SatelliteParameters;

import java.util.List;

/**
 * Created by lgr on 24/01/2018.
 */

public class RadarView extends RelativeLayout {

    RelativeLayout mView;
    ImageView mRadarLight;
    Context mContext;

    float mSatTickW;
    float mSatTickH;

    float mViewW;
    float mViewH;

    public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mViewH = getResources().getDimension(R.dimen.radar_width);
        mViewW = mViewH;

        mSatTickH = getResources().getDimension(R.dimen.satellite_tick_height);
        mSatTickW = getResources().getDimension(R.dimen.satellite_tick_width);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.radar_screen_view, this, true);

        mView = this.findViewById(R.id.radar_area);
        mRadarLight = this.findViewById(R.id.radar_light);
        mContext = context;

        Animation ViewAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotation_fast);
        mRadarLight.startAnimation(ViewAnimation);

//        addPoint(new Satellite(1,1,12), new PointF(30f, 100f));
//        addPoint(new Satellite(34,2,12), new PointF(134f, 80f));
//        addPoint(new Satellite(21,3,12), new PointF(-50f, 100f));

        // Resize Radar images
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int phoneWidth = size.x;
        int phoneHeight = size.y;

        ImageView imgBackground = mView.findViewById(R.id.background);
        ImageView imgForeground = mView.findViewById(R.id.foreground);
        ImageView imgLight = mView.findViewById(R.id.radar_light);
        RelativeLayout layoutArea = mView.findViewById(R.id.radar_area);


        imgBackground.getLayoutParams().width = (int)(phoneWidth*0.9); //2960) * imageView.getMeasuredWidth(); //imageView.getLayoutParams().width;
        imgBackground.getLayoutParams().height = (int)(phoneHeight*0.55);//1440)* imageView.getMeasuredHeight(); //imageView.getLayoutParams().height;
        imgBackground.requestLayout();
        imgBackground.invalidate();

        imgForeground.getLayoutParams().width = (int)(phoneWidth*0.9); //2960) * imageView.getMeasuredWidth(); //imageView.getLayoutParams().width;
        imgForeground.getLayoutParams().height = (int)(phoneHeight*0.55);//1440)* imageView.getMeasuredHeight(); //imageView.getLayoutParams().height;
        imgForeground.requestLayout();
        imgForeground.invalidate();

        imgLight.getLayoutParams().width = (int)(phoneWidth*0.9); //2960) * imageView.getMeasuredWidth(); //imageView.getLayoutParams().width;
        imgLight.getLayoutParams().height = (int)(phoneHeight*0.55);//1440)* imageView.getMeasuredHeight(); //imageView.getLayoutParams().height;
        imgLight.requestLayout();
        imgLight.invalidate();

        layoutArea.getLayoutParams().width = (int)(phoneWidth*0.9); //2960) * imageView.getMeasuredWidth(); //imageView.getLayoutParams().width;
        layoutArea.getLayoutParams().height = (int)(phoneHeight*0.55);//1440)* imageView.getMeasuredHeight(); //imageView.getLayoutParams().height;
        layoutArea.requestLayout();
        layoutArea.invalidate();


    }

    public RadarView(Context context) {
        this(context, null);
    }

    private int getPixels(float dipValue){
        Resources r = getResources();
        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,   r.getDisplayMetrics());
        return px;
    }

    public void addPoint(SatelliteParameters satellite, PointF position){

        RadarSatelliteTick satPoint = new RadarSatelliteTick(mContext);
        satPoint.setTick(satellite);

        PointF pointPosition = circToCart(position.x, position.y);

        satPoint.setX(pointPosition.x);
        satPoint.setY(pointPosition.y);

        Log.e("added", "point");

        mView.addView(satPoint);

    }

    public void updateSatellites(List<SatelliteParameters> satellites){
        mView.removeAllViews();
        for (int i=0; i<satellites.size(); i++){
            SatelliteParameters satellite = satellites.get(i);
            Log.d("SAT_POSITION_CHECK", "updateSatellites:" + satellite.getSatellitePosition());
            // crashing
//            addPoint(satellite,
//                    new PointF((float) satellite.getSatellitePosition().getX(),
//                            (float) satellite.getSatellitePosition().getY()));
        }
    }

    public PointF circToCart(float angle, float R){

        PointF coordinates = new PointF(0,0);

        float xo = mViewW/2;
        float yo = mViewH/2;

        angle = (float) Math.toRadians(angle);

        coordinates.x = xo + (R * (float) Math.sin(angle)) - mSatTickW / 2;
        coordinates.y = yo - (R * (float) Math.cos(angle))- mSatTickH * 3f/2f;

        return coordinates;

    }
}
