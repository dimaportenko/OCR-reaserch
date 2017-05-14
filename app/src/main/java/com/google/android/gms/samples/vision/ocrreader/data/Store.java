package com.google.android.gms.samples.vision.ocrreader.data;

import java.util.HashMap;

/**
 * Created by Dmytro Portenko on 11/16/16.
 */

public class Store implements DataSource {

    private static Store INSTANCE = null;

    private HashMap<Integer, ContainerModel> mContainers;
    private HashMap<Integer, Float> mTotals;

    private Store() {
        mContainers = new HashMap<Integer, ContainerModel>();
        mTotals = new HashMap<Integer, Float>();
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

    public void clearContainers() {
        mContainers.clear();
    }

    public void clear() {
        clearContainers();
        clearTotals();
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
}
