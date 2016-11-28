package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;

/**
 * Created by Dmytro Portenko on 11/10/16.
 */

public class TextGraphic extends OcrGraphic {

    private static final int TEXT_COLOR = Color.GREEN;

    private static Paint sRectPaint;
    private static Paint sTextPaint;

    private String mText;
    private RectF mRect;

    TextGraphic(GraphicOverlay overlay, RectF rect, String text) {
        super(overlay, null);

        if(text != null) {
            mText = text;
        }

        if (rect == null) {
            mRect = new RectF();
        } else {
            mRect = rect;
        }

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(TEXT_COLOR);
            sRectPaint.setStyle(Paint.Style.STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(54.0f);
        }
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    @Override
    public void draw(Canvas canvas) {

        // Draws the bounding box around the TextBlock.
        canvas.drawRect(mRect, sRectPaint);

        // Break the text into multiple lines and draw each one according to its own bounding box.
        canvas.drawText(mText, mRect.left, mRect.bottom, sTextPaint);
    }

    @Override
    public boolean contains(float x, float y) {
        return (mRect.left < x && mRect.right > x && mRect.top < y && mRect.bottom > y);
    }
}
