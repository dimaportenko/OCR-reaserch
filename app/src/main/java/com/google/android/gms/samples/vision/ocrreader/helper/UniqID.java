package com.google.android.gms.samples.vision.ocrreader.helper;


/**
 * Created by Dmytro Portenko on 11/17/16.
 */

public class UniqID {
    private static int lastId = 0;

    public static int generateID() {
        return lastId++;
    }
}
