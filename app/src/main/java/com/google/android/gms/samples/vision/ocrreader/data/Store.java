package com.google.android.gms.samples.vision.ocrreader.data;

/**
 * Created by Dmytro Portenko on 11/16/16.
 */

public class Store implements DataSource {

    private static Store INSTANCE = null;

    private Store() {

    }

    public static Store getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Store();
        }
        return INSTANCE;
    }


}
