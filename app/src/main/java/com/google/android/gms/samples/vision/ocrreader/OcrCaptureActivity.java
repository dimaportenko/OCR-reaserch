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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.ocrreader.data.AmountModel;
import com.google.android.gms.samples.vision.ocrreader.data.ContainerModel;
import com.google.android.gms.samples.vision.ocrreader.data.Store;
import com.google.android.gms.samples.vision.ocrreader.ui.UIView;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.AmountPickerFragment;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Activity for the multi-tracker app.  This app detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public final class OcrCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OcrCaptureActivity";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    // Helper objects for detecting taps and pinches.
    private float mScaleFactor = 1.f;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // UI elements
    private TextView totalsView;
    private Button cameraButton;
    private Button focusButton;
    private ImageButton flashButton;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ocr_capture);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);


        this.totalsView = (TextView) findViewById(R.id.totals);
        this.cameraButton = (Button) findViewById(R.id.camera_button);
        this.focusButton = (Button) findViewById(R.id.focus_button);
        this.flashButton = (ImageButton) findViewById(R.id.flash_button);
        drawingView = (UIView) findViewById(R.id.uiview);
        drawingViewSet = true;

        // read parameters from the intent used to launch the activity.
        boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
        boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));

        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // Check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private CameraSource.PictureCallback mPicture = new CameraSource.PictureCallback() {

        private byte[] mPictureData = null;

        @Override
        public void onPictureTaken(byte[] data) {
            Log.d(TAG, "onPictureTaken");

            mCameraSource.stop();
            mPictureData = data;
            int width = getWindowManager().getDefaultDisplay().getWidth();
            int height = getWindowManager().getDefaultDisplay().getHeight();

            BitmapFactory.Options scalingOptions = new BitmapFactory.Options();
//            scalingOptions.inSampleSize = camera.getParameters().getPictureSize().width / imageView.getMeasuredWidth();
            final Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, scalingOptions);
            mGraphicOverlay.setBitmapPicture(bmp, width, height);
            mGraphicOverlay.invalidate();

//            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//            if (pictureFile == null){
//                Log.d(TAG, "Error creating media file, check storage permissions: ");
//                return;
//            }
//
//            try {
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                fos.close();
//            } catch (FileNotFoundException e) {
//                Log.d(TAG, "File not found: " + e.getMessage());
//            } catch (IOException e) {
//                Log.d(TAG, "Error accessing file: " + e.getMessage());
//            }
        }
    };
    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            File mediaDir = new File(mediaStorageDir.getPath());
            mediaDir.mkdirs();
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }


        return mediaFile;
    }
    /**
     * onTap is called to capture the first TextBlock under the tap location and return it to
     * the Initializing Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private HashMap<OcrGraphic, Float> totalsMap = null;
    private boolean isStopped = false;
    private boolean onTap(MotionEvent event) {

        if (!isStopped) {
//            mCameraSource.takePicture(null, mPicture);
//            totalsMap = new HashMap<>();

            onFocusTap(event);

            return false;
        }

        float rawX = event.getRawX();
        float rawY = event.getRawY();


        final OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        if (graphic != null) {
            int[] location = new int[2];
            mGraphicOverlay.getLocationOnScreen(location);
            int result = graphic.selectIn(rawX - location[0], rawY - location[1]);

            switch (result) {
                case OcrGraphic.OCR_GRAPHIC_SELECT_RESULT_SELECT:
                case OcrGraphic.OCR_GRAPHIC_SELECT_RESULT_UNSELECT: {
                    updateTotals(graphic);
                    break;
                }
                case OcrGraphic.OCR_GRAPHIC_SELECT_RESULT_SECONDARY_ACTION: {
                    AmountPickerFragment pickerFragment = new AmountPickerFragment();
                    ContainerModel model = Store.getInstance().getContainer(graphic.getId());
                    AmountModel amountModel = model.getSelectedTextAmount();
                    pickerFragment.setAmount(amountModel.amount);
                    pickerFragment.setTotalAmount(amountModel.total);
                    pickerFragment.setAmountPickerFragmentListener(new AmountPickerFragment.AmountPickerFragmentListener() {
                        @Override
                        public void onPositiveButtonClick(int amount, int totalAmount) {
                            ContainerModel model = Store.getInstance().getContainer(graphic.getId());
                            model.setSelectedTextAmount(amount, totalAmount);
                            model.setSelectedText(null);
                            updateTotals(graphic);
                            mGraphicOverlay.invalidate();
                        }
                    });
                    pickerFragment.show(getSupportFragmentManager(), "NoticeDialogFragment");
                    break;
                }
                case OcrGraphic.OCR_GRAPHIC_SELECT_RESULT_NOT_FOUND:
                default: {

                    break;
                }

            }

            mGraphicOverlay.invalidate();
        }
        TextBlock text = null;

        return text != null;
    }

    private void updateTotals(OcrGraphic graphic) {
        graphic.updateTotals();
        Store.getInstance().setTotal(graphic.getId(), graphic.getTotals());
        Float totals = Store.getInstance().getTotals();

        totalsView.setText(String.format("%.2f", totals));
    }

    private UIView drawingView;
    private boolean drawingViewSet = false;

    private boolean onFocusTap(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getX();
            float y = event.getY();

            Rect touchRect = new Rect(
                    (int)(x - 100),
                    (int)(y - 100),
                    (int)(x + 100),
                    (int)(y + 100));

            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000/mPreview.getWidth() - 1000,
                    touchRect.top * 2000/mPreview.getHeight() - 1000,
                    touchRect.right * 2000/mPreview.getWidth() - 1000,
                    touchRect.bottom * 2000/mPreview.getHeight() - 1000);

            mCameraSource.doTouchFocus(targetFocusRect);
            if (drawingViewSet) {
                drawingView.setHaveTouch(true, touchRect);
                drawingView.invalidate();

                // Remove the square after some time
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0));
                        drawingView.invalidate();
                    }
                }, 1000);
            }

        }
        return false;
    }

    private boolean isAutoFocus = true;
    public void focusButtonAction(View view) {
        if(isAutoFocus) {
            mCameraSource.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            focusButton.setText(R.string.fixedfocus_button);
            isAutoFocus = false;
        } else {
            mCameraSource.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            focusButton.setText(R.string.autofocus_button);
            isAutoFocus = true;
        }
    }

    private boolean isFlash = false;
    public void flashButtonAction(View view) {
        if(isFlash) {
            mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            this.flashButton.setImageDrawable(getDrawable(R.drawable.ic_flash_on_white_24dp));
            isFlash = false;
        } else {
            mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            this.flashButton.setImageDrawable(getDrawable(R.drawable.ic_flash_off_white_24dp));
            isFlash = true;
        }
    }


    public void photoButtonAction(View view) {
        if(isStopped) {
            try {
                startCameraSource();
            } catch (SecurityException exception) {
                Log.e(TAG, "Unable to start camera source.", exception);
            }
            Store.getInstance().clear();
            isStopped = false;
            resetUI();
        } else {
            mCameraSource.takePicture(null, mPicture);
//            mCameraSource.stop();
            isStopped = true;
            this.cameraButton.setText(getString(R.string.camera_button_alt));
        }
    }

    public void resetUI() {
        totalsView.setText("");
        this.cameraButton.setText(getString(R.string.camera_button_main));
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            Log.d(TAG, "Scale factor default - " + detector.getScaleFactor());
            Log.d(TAG, "Scale factor - " + mScaleFactor);

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 2.0f));
            mGraphicOverlay.setScaleFactor(mScaleFactor, detector.getFocusX(), detector.getFocusY());
            mGraphicOverlay.invalidate();

            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }
}
