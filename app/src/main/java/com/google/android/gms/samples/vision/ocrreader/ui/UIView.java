package com.google.android.gms.samples.vision.ocrreader.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.samples.vision.ocrreader.R;

/**
 * TODO: document your custom view class.
 */
public class UIView extends View {
    private boolean haveTouch = false;
    private Rect touchArea;
    private Paint paint;

    public UIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(0xeed7d7d7);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        haveTouch = false;
    }

    public void setHaveTouch(boolean val, Rect rect) {
        haveTouch = val;
        touchArea = rect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(haveTouch){
            //drawingPaint.setColor(Color.BLUE);
//            canvas.drawRect(
//                    touchArea.left, touchArea.top, touchArea.right, touchArea.bottom,
//                    paint);
            canvas.drawCircle(touchArea.centerX(), touchArea.centerY(), touchArea.width() * 0.5f, paint);
        }
    }
}
