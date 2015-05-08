package com.maker.outlinecropperlib.Interfaces;

import android.graphics.Bitmap;

/**
 * Created by den4ik on 5/8/15.
 */
public interface CropperCallback {

    void onCropStart();

    void onCropEnd(Bitmap cropResultBitmap);
}
