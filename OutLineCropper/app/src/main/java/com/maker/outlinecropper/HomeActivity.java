package com.maker.outlinecropper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.maker.outlinecropperlib.Interfaces.CropperCallback;
import com.maker.outlinecropperlib.OutlineCropper;
import com.maker.outlinecropperlib.Views.CropperDrawingView;

public class HomeActivity extends ActionBarActivity {

    private CropperDrawingView cropperDrawingView;
    private OutlineCropper outlineCropper;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        pd = ProgressDialog.show(this, null, "Cropping...");
        pd.dismiss();

        cropperDrawingView = (CropperDrawingView) findViewById(R.id.cropper_drawing_view);

        cropperDrawingView.setImageCrop(BitmapFactory.decodeResource(getResources(), R.drawable.test_clothes));

        outlineCropper = new OutlineCropper(cropperDrawingView, new CropperCallback() {
            @Override
            public void onCropStart() {
                pd.show();
            }

            @Override
            public void onCropEnd(final Bitmap cropResultBitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        if (cropResultBitmap != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);

                            ImageView iv = new ImageView(HomeActivity.this);
                            iv.setImageBitmap(cropResultBitmap);

                            builder.setView(iv);
                            builder.create().show();
                        }
                    }
                });
            }
        });

        Button btnCrop = (Button) findViewById(R.id.btn_crop);

        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (outlineCropper.isReadyForCrop()) {
                    outlineCropper.startCrop();
                }
            }
        });
    }
}
