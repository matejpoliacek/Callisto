package com.chocolateam.galileospaceship;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

    float mRadarLightW;
    float mRadarLightH;

    double lat;
    double lng;
    double ECEF_X;
    double ECEF_Y;
    double ECEF_Z;

    double RAD_SCALE = 0.95;

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
    }

    public RadarView(Context context) {
        this(context, null);
    }


    public void addPoint(SatelliteParameters satellite){

        Log.e("RADARVIEW-SATPARAMS", satellite.getSatId() + ", " + satellite.getSatellitePosition() + ", " + satellite.getPseudorange()
                + ", " + satellite.getSatellitePosition().getGeodeticLatitude() + ", " + satellite.getSatellitePosition().getGeodeticLongitude()
                + "," + satellite.getSatellitePosition().getGeodeticHeight() + ", " + satellite.getSatellitePosition().getX() + ", "
                + satellite.getSatellitePosition().getY() + ", " + satellite.getSatellitePosition().getZ());

        mRadarLightH = mRadarLight.getHeight();
        mRadarLightW = mRadarLight.getWidth();

        //PointF position = new PointF((float) satellite.getSatellitePosition().getX(), (float) satellite.getSatellitePosition().getY());
        RadarSatelliteTick satPoint = new RadarSatelliteTick(mContext);
        satPoint.setTick(satellite);

        Log.e("RADARVIEW", "Sat Location: Lat: " + satellite.getSatellitePosition().getGeodeticLatitude()
                + ", Lng:  " + satellite.getSatellitePosition().getGeodeticLongitude() + ", X:  " + satellite.getSatellitePosition().getX()
                + ", Y:  " + satellite.getSatellitePosition().getY() + ", Z:  " + satellite.getSatellitePosition().getZ() + "\n; "
                + "User Loc: Lat: " + lat + ", Lng: " + lng + ", X: " + ECEF_X + ", Y: " + ECEF_Y + ", Z: " + ECEF_Z);

        double[] AzEl = calcAzEl(satellite.getSatellitePosition().getX(), satellite.getSatellitePosition().getY(), satellite.getSatellitePosition().getZ(),
                                 ECEF_X, ECEF_Y, ECEF_Z);

        //float radius = convertPixelsToDp(mRadarLightH, mContext);
        float radius = (float) ((((mRadarLightH * RAD_SCALE)/ 2) / 90.0) * (90.0 - AzEl[1])); // radius scaled by elevation

        Log.e("RADARVIEW", "Input params angle: " + AzEl[0] + " radius: " + AzEl[1] + " IMGW: " + mRadarLightW + " IMGH: " + mRadarLightH + "; Az,El: (" + AzEl[0] + ", " + AzEl[1] + ")");

        PointF pointPosition = circToCart((float)  AzEl[0] , radius);

        satPoint.setX(pointPosition.x);
        satPoint.setY(pointPosition.y);

        Log.e("RADARVIEW", "added point x: " + pointPosition.x + " y: " + pointPosition.y);

        mView.addView(satPoint);
        mView.invalidate();

    }

    public void updateSatellites(List<SatelliteParameters> satellites){
        mView.removeAllViews();
        int displayed = 0;
        int notDisplayed = 0;
        int nullSat = 0;
        for (SatelliteParameters satellite : satellites){
            Log.d("SAT_POSITION_CHECK", "updateSatellites:" + satellite.getSatellitePosition());
            //
            if (satellite != null) {
                if (satellite.getSatellitePosition() != null) {
                    Log.e("RadarView - ", String.valueOf(satellite.getSatId()) + "Satellite POSITION is NOT null");
                    addPoint(satellite);
                    displayed++;
                } else {
                    Log.e("RadarView - ", String.valueOf(satellite.getSatId()) + "Satellite POSITION is null");
                    notDisplayed++;
                }
        	} else {
        		Log.e("RadarView", "Satellite is null");
        	}
        }
        Log.e("RadarView", "Satellites updated, stats - displayed: " + displayed + " | not displayed: " + notDisplayed + " | null sats: " + nullSat);
    }

    private double[] calcAzEl(double sat_x, double sat_y, double sat_z, double user_x, double user_y, double user_z) {

        double distance = Math.sqrt(Math.pow(sat_x - user_x, 2) + Math.pow(sat_y - user_y, 2) + Math.pow(sat_z - user_z, 2));
        double elevation = Math.toDegrees(Math.asin((sat_z - user_z) / distance));
        double azimuth = Math.toDegrees(Math.atan((sat_x - user_x) / (sat_y - user_y)));

        double[] AzEl = {azimuth, elevation};

        return AzEl;
    }

    public PointF circToCart(float angle, float R){

        PointF coordinates = new PointF(0,0);

        float xo = (float) ((mRadarLightW * RAD_SCALE) / 2.0);
        float yo = (float) ((mRadarLightH * RAD_SCALE) / 2.0);

        coordinates.x = (int) (xo + (R * (float) Math.sin(angle)) - mSatTickW / 2);
        coordinates.y = (int) (yo - (R * (float) Math.cos(angle)) - mSatTickH / 2);

        return coordinates;

    }

    public void setLatLngXYZ(double lat, double lng, double ECEF_X, double ECEF_Y, double ECEF_Z) {
        this.lat = lat;
        this.lng = lng;
        this.ECEF_X = ECEF_X;
        this.ECEF_Y = ECEF_Y;
        this.ECEF_Z = ECEF_Z;
    }

    //TODO: Candidates for removal

    private float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }


    private int getPixels(float dipValue){
        Resources r = getResources();
        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,   r.getDisplayMetrics());
        return px;
    }
}
