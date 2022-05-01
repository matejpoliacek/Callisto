package com.chocolateam.galileospaceship;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class RadarCompassNeedleView extends View {
    Paint paint = new Paint();
    int startX = 0;
    int startY = 0;
    int stopX_N = 0;
    int stopY_N = 0;

    int stopX_S = 0;
    int stopY_S = 0;

    private void init() {
        paint.setColor(Color.RED);
        paint.setStrokeWidth(25);
        paint.setAlpha(75);
    }

    public RadarCompassNeedleView(Context context) {
        super(context);
        init();
    }

    public RadarCompassNeedleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadarCompassNeedleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(Color.RED);
        paint.setAlpha(75);
        canvas.drawLine(startX, startY, stopX_N, stopY_N, paint);
        // TODO: draw N in the line as label

        paint.setColor(Color.WHITE);
        paint.setAlpha(75);
        canvas.drawLine(startX, startY, stopX_S, stopY_S, paint);
    }

    public void setPointsCenter(int startX, int startY) {
        this.startX = startX;
        this.startY = startY;
    }

    public void setPointsNorth(int stopX_N, int stopY_N) {
        this.stopX_N = stopX_N;
        this.stopY_N = stopY_N;
    }

    public void setPointsSouth(int stopX_S, int stopY_S) {
        this.stopX_S = stopX_S;
        this.stopY_S = stopY_S;
    }

    public void setAlpha(int alpha) {
        this.paint.setAlpha(alpha);
    }

    public void setPaint(int colour){
        this.paint.setColor(colour);
    }
}
