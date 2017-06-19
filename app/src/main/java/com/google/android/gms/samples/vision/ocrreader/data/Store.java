package com.google.android.gms.samples.vision.ocrreader.data;

import android.graphics.Rect;
import android.graphics.RectF;

import com.google.android.gms.vision.text.Text;

import java.util.HashMap;

/**
 * Created by Dmytro Portenko on 11/16/16.
 */

public class Store implements DataSource {

    private static Store INSTANCE = null;

    private HashMap<Integer, ContainerModel> mContainers;
    private HashMap<Integer, Float> mTotals;
    private HashMap<Text, RectF> additionalRects;

    private Store() {
        mContainers = new HashMap<Integer, ContainerModel>();
        mTotals = new HashMap<Integer, Float>();
        additionalRects = new HashMap<Text, RectF>();
    }

    public static Store getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Store();
        }
        return INSTANCE;
    }

    public void setContainer(ContainerModel container) {
        mContainers.put(container.getId(), container);
    }

    public ContainerModel getContainer(int id) {
        return mContainers.get(id);
    }

    public void clearContainers() {
        mContainers.clear();
    }

    public void clear() {
        clearContainers();
        clearTotals();
        this.additionalRects.clear();
    }

    public void setTotal(Integer id, Float total) {
        mTotals.put(id, total);
    }

    public void clearTotals() {
        mTotals.clear();
    }

    public Float getTotals() {
        Float totals = new Float(0);
        for (Float value : mTotals.values()) {
            totals += value;
        }

        return totals;
    }

    public void addAdditionalRect(Text text, RectF rect) {
        this.additionalRects.put(text, rect);
    }

    public void removeAdditianalRect(Text text) {
        this.additionalRects.remove(text);
    }

    public boolean isIntersectAdditionalRect(RectF rect) {
        for (RectF additionalRect : this.additionalRects.values() ) {
            if (rect.intersect(additionalRect)) {
                return true;
            }
        }

        return false;
    }
}
