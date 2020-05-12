package com.chocolateam.galileospaceship;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
import com.galfins.gnss_compare.Constellations.Pseudorange;
import com.galfins.gnss_compare.Constellations.SatelliteParameters;
import com.galfins.gogpsextracts.SatellitePosition;

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

    double lat;
    double lng;

    public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);

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

/*        addPoint(new Satellite(1,1,12), new PointF(30f, 100f));
        addPoint(new Satellite(34,2,12), new PointF(134f, 80f));
        addPoint(new Satellite(21,3,12), new PointF(-50f, 100f));*/


        SatelliteParameters satellite = new SatelliteParameters(1, new Pseudorange(23000, 1));
        satellite.setSatellitePosition(new SatellitePosition(1,1, 'G', 18109.86, 1364.389, 23357.029));

        addPoint(satellite);
    }

    public RadarView(Context context) {
        this(context, null);
    }

    private int getPixels(float dipValue){
        Resources r = getResources();
        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,   r.getDisplayMetrics());
        return px;
    }

    public void addPoint(SatelliteParameters satellite){

        Log.e("RADARVIEW-SATPARAMS", satellite.getSatId() + ", " + satellite.getSatellitePosition() + ", " + satellite.getPseudorange()
                + ", " + + satellite.getSatellitePosition().getGeodeticLatitude() + ", " + satellite.getSatellitePosition().getGeodeticLongitude()
                + "," + satellite.getSatellitePosition().getGeodeticHeight() + ", " + satellite.getSatellitePosition().getX() + ", "
                + satellite.getSatellitePosition().getY() + ", " + satellite.getSatellitePosition().getZ());

                mViewH = mView.getHeight();
        mViewW = mView.getWidth();

        //PointF position = new PointF((float) satellite.getSatellitePosition().getX(), (float) satellite.getSatellitePosition().getY());
        RadarSatelliteTick satPoint = new RadarSatelliteTick(mContext);
        satPoint.setTick(satellite);

        Log.e("RADARVIEW", "Sat Location: Lat: " + satellite.getSatellitePosition().getGeodeticLatitude()
                + ", Lng:  " + satellite.getSatellitePosition().getGeodeticLongitude()
                + "; User Loc: Lat: " + lat + ", Lng: " + lng);


        double dy = satellite.getSatellitePosition().getGeodeticLatitude() - lat;
        double dx = Math.cos(Math.PI/180*lat)*(satellite.getSatellitePosition().getGeodeticLongitude() - lng);
        double angle = Math.atan2(dy, dx);

        float radius = convertPixelsToDp(findViewById(R.id.background).getHeight()/2, mContext);

        Log.e("RADARVIEW", "Input params angle: " + angle + " radius: " + radius + " IMGW: " + mViewW + " IMGH: " + mViewH);

        PointF pointPosition = circToCart((float) angle , radius);

        satPoint.setX(pointPosition.x);
        satPoint.setY(pointPosition.y);

        Log.e("RADARVIEW", "added point x: " + pointPosition.x + " y: " + pointPosition.y);

        mView.addView(satPoint);
        mView.invalidate();

    }

    public void updateSatellites(List<SatelliteParameters> satellites){
        mView.removeAllViews();
        for (SatelliteParameters satellite : satellites){
            Log.d("SAT_POSITION_CHECK", "updateSatellites:" + satellite.getSatellitePosition());
            //
            if (satellite != null) {
                if (satellite.getSatellitePosition() != null) {
                    Log.e("RadarView - ", String.valueOf(satellite.getSatId()) + "Satellite POSITION is NOT null");
                    addPoint(satellite);
                } else {
                    Log.e("RadarView - ", String.valueOf(satellite.getSatId()) + "Satellite POSITION is null");
                }
        	} else {
        		Log.e("RadarView", "Satellite is null");
        	}
        }
    }

    public PointF circToCart(float angle, float R){

        PointF coordinates = new PointF(0,0);

        float xo = mViewW/2;
        float yo = mViewH/2;

        coordinates.x = xo + (R * (float) Math.sin(angle)) - mSatTickW / 2;
        coordinates.y = yo - (R * (float) Math.cos(angle))- mSatTickH * 3f/2f;

        return coordinates;

    }

    public void setLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    private float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
}
