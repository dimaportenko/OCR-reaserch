package com.google.android.gms.samples.vision.ocrreader.data;

import com.google.android.gms.samples.vision.ocrreader.helper.UniqID;
import com.google.android.gms.vision.text.Text;

import java.util.HashMap;

/**
 * Created by Dmytro Portenko on 11/29/16.
 */

public class ContainerModel {

    private int mId;
    private HashMap<Text, AmountModel> mTextAmounts;

    ContainerModel() {
        this(UniqID.generateID());
    }

    public ContainerModel(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

//    private int mAmount = 1;
//    private int mTotalAmount = 1;
//
//    public int getmAmount() {
//        return mAmount;
//    }
//
//    public void setmAmount(int mAmount) {
//        this.mAmount = mAmount;
//    }
//
//    public int getmTotalAmount() {
//        return mTotalAmount;
//    }
//
//    public void setmTotalAmount(int mTotalAmount) {
//        this.mTotalAmount = mTotalAmount;
//    }
}
