package com.maker.outlinecropperlib;

import com.maker.outlinecropperlib.Interfaces.CropperCallback;
import com.maker.outlinecropperlib.Views.CropperDrawingView;

/**
 * Created by den4ik on 5/8/15.
 */
public class OutlineCropper {

    private CropperDrawingView cropperDrawingView;
    private CropperCallback cropperCallback;
    private Thread thread;

    public OutlineCropper(CropperDrawingView cropperDrawingView, CropperCallback cropperCallback) {
        this.cropperDrawingView = cropperDrawingView;
        this.cropperCallback = cropperCallback;
    }

    public void startCrop() {
        if (cropperCallback != null) {
            cropperCallback.onCropStart();
        }
        if (cropperDrawingView != null) {
            if (thread != null) {
                cropThread.run();
            } else {
                thread = new Thread(cropThread);
                thread.start();
            }
        }
    }

    private Runnable cropThread = new Runnable() {
        @Override
        public void run() {
            if (cropperCallback != null) {
                cropperCallback.onCropEnd(
                        cropperDrawingView.crop());
            }
            thread.interrupt();
            thread = null;
        }
    };

    public boolean isReadyForCrop() {
        return cropperDrawingView != null
                && cropperDrawingView.hasAreaCropMatrix();
    }
}
