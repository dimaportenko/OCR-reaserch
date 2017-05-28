/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import com.google.android.gms.samples.vision.ocrreader.data.AmountModel;
import com.google.android.gms.samples.vision.ocrreader.data.ContainerModel;
import com.google.android.gms.samples.vision.ocrreader.helper.DataConvertor;
import com.google.android.gms.samples.vision.ocrreader.helper.UniqID;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class OcrGraphic extends GraphicOverlay.Graphic {

    private int mId;

    private static final int TEXT_COLOR = 0x7FFFFFFF;
    private static final int TEXT_COLOR_ACTIVE = 0x7FFFFFFF;
    private static final int BACKGROUND_COLOR = 0x7F000000;
    private static final int BACKGROUND_COLOR_ACTIVE = 0x7F0000FF;

    private static Paint sRectPaint;
    private static Paint sTextPaint;
    private final TextBlock mText;

    private List<Text> mElements = new ArrayList<Text>();
    private List<Text> mActiveElements = new ArrayList<Text>();

    private ContainerModel mContainer;

    public static final int OCR_GRAPHIC_SELECT_RESULT_SELECT = 1;
    public static final int OCR_GRAPHIC_SELECT_RESULT_UNSELECT = 2;
    public static final int OCR_GRAPHIC_SELECT_RESULT_SECONDARY_ACTION = 3;
    public static final int OCR_GRAPHIC_SELECT_RESULT_NOT_FOUND = 4;

    OcrGraphic(GraphicOverlay overlay, ContainerModel container) {
        this(overlay, container, UniqID.generateID());
    }

    OcrGraphic(GraphicOverlay overlay, ContainerModel container, int id) {
        super(overlay);

        mId = id;
        mContainer = container;
        mText = container.getText();
        mElements = container.getElements();

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(TEXT_COLOR);
            sRectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(36.0f);
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public TextBlock getTextBlock() {
        return mText;
    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    public boolean contains(float x, float y) {
        TextBlock text = mText;
        if (text == null) {
            return false;
        }
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        rect.left -= rect.width();
        return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
    }

    private RectF traslateBox(RectF rect) {
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);

        return rect;
    }

    public int selectIn(float x, float y) {
        for(Text text : mElements) {
            Rect box = text.getBoundingBox();
            RectF rect = traslateBox(new RectF(box));
            if (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y) {
                mElements.remove(text);
                mActiveElements.add(text);
                return OCR_GRAPHIC_SELECT_RESULT_SELECT;
            }
        }

        for(Text text : mActiveElements) {
            RectF rect = traslateBox(new RectF(text.getBoundingBox()));
            if (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y) {
                mActiveElements.remove(text);
                mElements.add(text);
                return OCR_GRAPHIC_SELECT_RESULT_UNSELECT;
            }

            float width = rect.width();
            rect.left -= width;
            rect.right -= width;
            if (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y) {
                mContainer.setSelectedText(text);
                mOverlay.invalidate();
                return OCR_GRAPHIC_SELECT_RESULT_SECONDARY_ACTION;
            }
        }
        return OCR_GRAPHIC_SELECT_RESULT_NOT_FOUND;
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        TextBlock text = mText;
        if (text == null) {
            return;
        }

        // Draws the bounding box around the TextBlock.

        // Break the text into multiple lines and draw each one according to its own bounding box.
        for(Text currentText : mElements) {
            sRectPaint.setColor(TEXT_COLOR);
            sTextPaint.setColor(TEXT_COLOR);
            drawElement(canvas, currentText, BACKGROUND_COLOR, false);
            updateTotals();
        }

        for(Text currentText : mActiveElements) {
            sRectPaint.setColor(TEXT_COLOR_ACTIVE);
            sTextPaint.setColor(TEXT_COLOR_ACTIVE);
            drawElement(canvas, currentText, BACKGROUND_COLOR_ACTIVE, true);
            updateTotals();
        }
    }

    protected float totals = 0;
    public void updateTotals() {
        totals = 0;
        for(Text currentText : mActiveElements) {
            AmountModel amount = mContainer.getAmount(currentText);
            String number = DataConvertor.getNumberFromString(currentText.getValue());
            totals += Float.valueOf(number) * amount.ratio();
        }
    }

    public float getTotals() {
        return totals;
    }

    private void drawElement(Canvas canvas, Text currentText, int centerColor, boolean isActive) {
        RectF rect = new RectF(currentText.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        sRectPaint.setShader(new RadialGradient(rect.centerX(), rect.centerY(), (float)(rect.width() * 0.5), centerColor, Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawOval(rect, sRectPaint);

        String value = DataConvertor.getNumberFromString(currentText.getValue());
        if(value != null) {
            sTextPaint.setTextAlign(Paint.Align.CENTER);
            AmountModel amount = mContainer.getAmount(currentText);
            String number = DataConvertor.getNumberFromString(value);
            float calculated = Float.valueOf(number) * amount.ratio();
            canvas.drawText(String.valueOf(calculated), rect.centerX(), rect.centerY() + sTextPaint.getTextSize() * 0.5f, sTextPaint);
        }

        if(isActive) {
            float width = rect.width();
            rect.left -= width;
            rect.right -= width;
            sRectPaint.setShader(new RadialGradient(rect.centerX(), rect.centerY(), (float) (rect.width() * 0.5), centerColor, Color.TRANSPARENT, Shader.TileMode.CLAMP));
            canvas.drawOval(rect, sRectPaint);
            AmountModel amount = mContainer.getAmount(currentText);
            canvas.drawText(amount.amount + "/" + amount.total, rect.centerX(), rect.centerY() + sTextPaint.getTextSize() * 0.5f, sTextPaint);
        }
    }
}
