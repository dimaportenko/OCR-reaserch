package com.google.android.gms.samples.vision.ocrreader.data;

import com.google.android.gms.samples.vision.ocrreader.helper.UniqID;

/**
 * Created by Dmytro Portenko on 11/29/16.
 */

public class ContainerModel {

    private int mId;

    ContainerModel() {
        this(UniqID.generateID());
    }

    public ContainerModel(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }
}
