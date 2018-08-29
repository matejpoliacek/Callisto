package com.chocolateam.galileospaceship;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chocolateam.galileospaceship.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lionel Garcia on 17/01/2018.
 */



class sat_position {

    private float mX;
    private float mY;
    private float mAngle;

    public sat_position(float X, float Y, float angle) {
        mX = X;
        mY = Y;
        mAngle = angle;
    }

    public sat_position(PointF coordinates, float angle){
        mX = coordinates.x;
        mY = coordinates.y;
        mAngle = angle;
    }

    public float getmAngle() {
        return mAngle;
    }

    public void setmAngle(float mAngle) {
        this.mAngle = mAngle;
    }

    public float getmY() {
        return mY;
    }

    public void setmY(float mY) {
        this.mY = mY;
    }

    public float getmX() {
        return mX;
    }

    public void setmX(float mX) {
        this.mX = mX;
    }

}


public class SatView extends ConstraintLayout {

    private Context mcontext;
    private SatelliteInfoView msatInfoView;

    private float mscreenW;
    private float mviewH = 1000;
    private int msatWidth = 120;
    private int msatHeight;

    private List<Satellite> msatList = new ArrayList<>();
    private List<View> msatViewList = new ArrayList<>();
    private List<sat_position> msatPosList = new ArrayList<>();

    public SatView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mcontext = context;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        mscreenW = displayMetrics.widthPixels;

        Bitmap bp =  BitmapFactory.decodeResource(getResources(), R.drawable.satellite);
        int bpwidth = bp.getWidth();
        int bpheight = bp.getHeight();
        msatHeight = (int) (((float) msatWidth/ (float) bpwidth)* (float) bpheight);

//        addSatellite(new Satellite(1,12,12), new sat_position(mscreenW/2 - msatWidth/2, 1000, 0));

        float altitude = 500;
        addSatellite(new Satellite(1,12,12), 40, altitude-80);
        addSatellite(new Satellite(2,12,12), 15, altitude);
        addSatellite(new Satellite(3,12,12), 0, altitude);
        addSatellite(new Satellite(4,12,12), -15, altitude);
        addSatellite(new Satellite(5,12,12), -30, altitude-80);
        addSatellite(new Satellite(6,12,12), -17, 350);
        addSatellite(new Satellite(7,12,12), 8, 420);
//        addSatellite(new Satellite(1,12,12), new sat_position(100,400, -25));
//        addSatellite(new Satellite(2,12,12), new sat_position(200,350, -15));
//        addSatellite(new Satellite(3,12,12), new sat_position(400,320, 0));
//        addSatellite(new Satellite(3,12,12), new sat_position(600,350, 15));
//        addSatellite(new Satellite(250,12,12), new sat_position(200,350, -15));
//        addSatellite(new Satellite(59,12,12), new sat_position(300,400, -10));
//        addSatellite(new Satellite(78,12,12), new sat_position(400,360, -0));

//        setSatelliteSelected(12);
//        setSatelliteSelected(78);

        this.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("sat_view_clicked", "true");
            }
        });

    }

    public SatView(Context context) {
        this(context, null);
    }

    public PointF circToCart(float angle, float R){

        PointF coordinates = new PointF(0,0);

        float xo = mscreenW/2;
        float yo = mviewH*0.66f;

        angle = (float) Math.toRadians(angle);

        coordinates.x = xo + (R * (float) Math.sin(angle)) - (float) msatWidth/2;
        coordinates.y = yo - (R * (float) Math.cos(angle)) - (float) msatHeight/2;

        return coordinates;

    }

    public void setSatelliteSelected(int satId){

        //PERFORMANCES : Here we are loosing performance by redrawing all the view, for sure we can find another way

        for(int i=0; i<msatList.size();i++){
                this.removeView(msatViewList.get(i));
            }

        for(int i=0; i<msatList.size();i++){
            LinearLayout new_satLayout;

            if(msatList.get(i).getMid() == satId) {
                new_satLayout = createSatLayout(msatPosList.get(i), true, msatList.get(i).getMid());
            }
            else {
                new_satLayout = createSatLayout(msatPosList.get(i), false, msatList.get(i).getMid());
            }

            msatInfoView.SweepOut();

            msatViewList.set(i, new_satLayout);
            this.addView(new_satLayout);
            this.invalidate();
            }
    }

    public LinearLayout createSatLayout(final sat_position satPosition, Boolean activated, final int sat_id){

        final ImageView mimageView = new ImageView(mcontext);
//        Bitmap bm;
//        if(activated){
//            bm = BitmapFactory.decodeResource(getResources(), R.drawable.satellite);
//        }
//        else{
//            bm = BitmapFactory.decodeResource(getResources(), R.drawable.satellite_selected);
//        }
//        mimageView.setImageBitmap(bm);
//
//        Animation animation;
//        animation = new RotateAnimation(satPosition.getmAngle(), satPosition.getmAngle(), 0, 0);
//        animation.setFillAfter(true);
//        animation.setDuration(0);
////        mimageView.setAnimation(animation);

        LinearLayout linLayout = new LinearLayout(mcontext);
        Bitmap bitmapOrg;
        // load the origial BitMap (500 x 500 px)

        if(activated){
            bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.satellite_selected);
        }
        else{
            bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.satellite);
        }

        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();

        // calculate the scale - in this case = 0.4f
        float scaleWidth = ((float) msatWidth) / width;
        float scaleHeight = ((float) msatHeight) / height;

        // createa matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // rotate the Bitmap
        matrix.postRotate(satPosition.getmAngle());

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
                width, height, matrix, true);

        mimageView.setImageBitmap(resizedBitmap);

        mimageView.setLayoutParams(new ConstraintLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        mimageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        linLayout.addView(mimageView, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        linLayout.setX(satPosition.getmX());
        linLayout.setY(satPosition.getmY());

        linLayout.setClickable(true);


        mimageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setSatelliteSelected(sat_id);
            }
        });

        return linLayout;
    }

    public LinearLayout addSatellite(final Satellite satellite, float angle, float altitude_px){

        sat_position satPosition = new sat_position(circToCart(angle, altitude_px), angle);

        LinearLayout satLayout = createSatLayout(satPosition, false, satellite.getMid());

        msatList.add(satellite);
        msatPosList.add(satPosition);
        msatViewList.add(satLayout);

        this.addView(satLayout);

        this.invalidate();

        return satLayout;

    }

    public void addSatellite(Satellite satellite, sat_position satPosition){

        LinearLayout satLayout = createSatLayout(satPosition, false, satellite.getMid());

        msatList.add(satellite);
        msatPosList.add(satPosition);
        msatViewList.add(satLayout);

        this.addView(satLayout);
        this.invalidate();
    }

    public void setSatInfoView(SatelliteInfoView satInfoView){
        msatInfoView = satInfoView;
    }

}
