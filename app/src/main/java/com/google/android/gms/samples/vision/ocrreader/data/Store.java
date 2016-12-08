package com.google.android.gms.samples.vision.ocrreader.data;

import java.util.HashMap;

/**
 * Created by Dmytro Portenko on 11/16/16.
 */

public class Store implements DataSource {

    private static Store INSTANCE = null;

    private HashMap<Integer, ContainerModel> mContainers;

    private Store() {
        mContainers = new HashMap<Integer, ContainerModel>();
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
}
