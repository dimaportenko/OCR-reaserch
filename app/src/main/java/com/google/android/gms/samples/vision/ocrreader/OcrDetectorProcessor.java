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

import android.graphics.RectF;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.ocrreader.data.ContainerModel;
import com.google.android.gms.samples.vision.ocrreader.data.Store;
import com.google.android.gms.samples.vision.ocrreader.helper.UniqID;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        mGraphicOverlay = ocrGraphicOverlay;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        int itemId;
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if(getNumberFromString(item.getValue()) != null) {
                itemId = UniqID.generateID();
                OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item, itemId);
                ContainerModel containerModel = new ContainerModel(itemId);
                Store.getInstance().setContainer(containerModel);
                mGraphicOverlay.add(graphic);
            }
        }


    }

    public String getNumberFromString(String string) {
        StringBuffer sBuffer = new StringBuffer();
        Pattern p = Pattern.compile("[0-9]+.[0-9]*|[0-9]*.[0-9]+|[0-9]+");
        Matcher m = p.matcher(string);
        boolean found = false;
        while (m.find()) {
            sBuffer.append(m.group());
            found = true;
        }
        if(found) {
            return sBuffer.toString();
        } else {
            return null;
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
