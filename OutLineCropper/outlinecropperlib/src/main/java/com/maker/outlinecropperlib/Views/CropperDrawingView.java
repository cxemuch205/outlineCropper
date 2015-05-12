package com.maker.outlinecropperlib.Views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.maker.outlinecropperlib.Models.CropPoint;

import java.util.ArrayList;

/**
 * Created by den4ik on 5/8/15.
 */
public class CropperDrawingView extends View {

    public static final String TAG = "CropperDrawingView";

    private static final float QUALITY_OUTLINE_POINTS = 0.5f; // value need > 0.1 && value < 2.0
    private static final float QUALITY_FILL_MATRIX_POINTS = 0.75f; // value need > 0.5 && value < 1.0

    private Bitmap mBitmap, imageCrop, cropResult;
    private Canvas mCanvas;
    private Path mPath, mPath2;
    private Paint mBitmapPaint, mBitmapCropPaint;
    private Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Paint mPaint;

    public int width;
    public int height;
    public int radiusLine = 20;

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 1;
    private ArrayList<CropPoint> matrixDraw;

    public CropperDrawingView(Context context) {
        super(context);
        init(context);
    }

    public CropperDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropperDrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CropperDrawingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    public void setImageCrop(Bitmap imageCrop) {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        int imgWidth = imageCrop.getWidth();
        int imgHeight = imageCrop.getHeight();

        Log.d(TAG, String.format("START ### w: %d | h: %d | imgW: %d | imgH: %d", width, height, imgWidth, imgHeight));

        if (isPortrait()) {
            double dif = (imgWidth > width) ? (((double) imgWidth) / ((double) width)) : (((double) width) / ((double) imgWidth));
            height = (int) (((double) imgHeight) / dif);
        } else {

        }

        Log.d(TAG, String.format("RESULT ### w: %d | h: %d | imgW: %d | imgH: %d", width, height, imgWidth, imgHeight));

        this.imageCrop = Bitmap.createScaledBitmap(imageCrop, width, height, true);
        invalidate();
    }

    private boolean isPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private void init(Context context) {
        this.context = context;
        mPath = new Path();
        mPath2 = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mBitmapCropPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        mPaint = new Paint();

        mPaint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5f);

        circlePaint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(8f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        dropCanvas(w, h);
    }

    private void dropCanvas(int w, int h) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (imageCrop != null) {
            canvas.drawBitmap(imageCrop, 0, 0, mBitmapPaint);
        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(mPath, mPaint);

        canvas.drawPath(circlePath, circlePaint);
    }

    private void touch_start(float x, float y) {
        if (matrixDraw != null) {
            matrixDraw.clear();
            matrixDraw = null;
        }

        dropCanvas(getWidth(), getHeight());

        addToMatrixCrop(x, y);
        mPath.reset();
        mPath.moveTo(x, y);
        mPath2.reset();
        mPath2.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private static float ACC = 0.1f;

    private void addToMatrixCrop(float x, float y) {
        if (imageCrop == null) {
            return;
        }
        if (matrixDraw == null) {
            matrixDraw = new ArrayList<>();
        }
        if (imageCrop.getWidth() > (int) x
                && imageCrop.getHeight() > (int) y
                && x >= 0
                && y >= 0) {
            int color = getColorByPixel(imageCrop.getPixel((int) x, (int) y));
            matrixDraw.add(new CropPoint(x, y, color));
        }

    }

    private void touch_move(float x, float y) {
        addToMatrixCrop(x, y);
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mPath2.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, radiusLine, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        mPath2.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public Bitmap crop() {
        int minX = getMinXCoordinates();
        int minY = getMinYCoordinates();
        int maxX = getMaxXCoordinates();
        int maxY = getMaxYCoordinates();

        cropResult = Bitmap.createBitmap(
                maxX - minX,
                maxY - minY,
                imageCrop.getConfig());

        Paint paintCrop = new Paint();
        paintCrop.setColor(Color.WHITE);
        paintCrop.setAntiAlias(true);
        Canvas canvas = new Canvas(cropResult);

        //TODO: need make full outline
        //matrixDraw = fillOutline();

        matrixDraw = updateByPath(mPath2);
        matrixDraw = fillMatrixArray();

        for (CropPoint point : matrixDraw) {
            paintCrop.setColor(point.getColor());
            canvas.drawPoint(point.getX() - (float) minX, point.getY() - (float) minY, paintCrop);
        }

        //TEST create bitmap create square

        /*cropResult = Bitmap.createBitmap(imageCrop,
                getMinXCoordinates(),
                getMinYCoordinates(),
                getMaxXCoordinates() - getMinXCoordinates(),
                getMaxYCoordinates() - getMinYCoordinates());*/

        return cropResult;
    }


    private ArrayList<CropPoint> updateByPath(Path path) {
        ArrayList<CropPoint> newPoints = new ArrayList<>();
        PathMeasure pm = new PathMeasure(path, false);
        float length = pm.getLength();
        float distance = 0f;
        float speed = QUALITY_OUTLINE_POINTS;
        float[] aCoordinates = new float[2];

        while ((distance < length)) {
            // get point from the path
            pm.getPosTan(distance, aCoordinates, null);

            int color = getColorByPixel(imageCrop.getPixel((int) aCoordinates[0], (int) aCoordinates[1]));

            newPoints.add(new CropPoint(
                    aCoordinates[0],
                    aCoordinates[1],
                    color));
            distance = distance + speed;
        }

        return newPoints;
    }

    /*private ArrayList<CropPoint> fillOutline() {
        ArrayList<CropPoint> newPoints = new ArrayList<>(matrixDraw);
        for (int i = 0; i < matrixDraw.size()-1; i++) {
            CropPoint lastPoint = matrixDraw.get(i);
            CropPoint newPoint = matrixDraw.get(i+1);
            float x1 = lastPoint.getX();
            float y1 = lastPoint.getY();
            float newX1 = Float.parseFloat(String.valueOf(newPoint.getX()).substring(0, String.valueOf(newPoint.getX()).indexOf(".") + 2));
            float newY1 = Float.parseFloat(String.valueOf(newPoint.getY()).substring(0, String.valueOf(newPoint.getY()).indexOf(".") + 2));
            while (x1 != newX1) {
                if (lastPoint.getX() <= newPoint.getX()) {
                    x1 += ACC;
                } else {
                    x1 -= ACC;
                }
                if (lastPoint.getY() <= newPoint.getY()) {
                    y1 += ACC;
                } else {
                    y1 -= ACC;
                }
                x1 = new BigDecimal(x1).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                y1 = new BigDecimal(y1).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                if (x1 > 0 && y1 > 0 && x1 < imageCrop.getWidth() && y1 < imageCrop.getHeight()) {
                    Log.d(TAG, "x1: " + x1 + " | " + newX1 + " \ty1: " + y1 + " | " + newY1 + " INDEX: " + i);
                    newPoints.add(
                            new CropPoint(x1, y1,
                                    getColorByPixel(imageCrop.getPixel((int) x1, (int) y1))));
                }
            }
        }
        return newPoints;
    }*/

    private ArrayList<CropPoint> fillMatrixArray() {
        ArrayList<CropPoint> newPoints = new ArrayList<>(matrixDraw);
        ArrayList<CropPoint> www = new ArrayList<>(matrixDraw);

        for (CropPoint p : matrixDraw) {
            boolean havePoint = false;
            int x = -1;
            for (CropPoint point : www) {
                if (p.getY() <= (point.getY() + 2.5f) && p.getY() >= (point.getY() - 2.5f)) {
                    havePoint = true;
                    if (x < (int) point.getX())
                        x = (int) point.getX();
                }
            }
            if (havePoint) {
                for (float i = p.getX(); i < x; i += QUALITY_FILL_MATRIX_POINTS) {
                    int color = getColorByPixel(imageCrop.getPixel((int) i, (int) p.getY()));
                    newPoints.add(new CropPoint(i, p.getY(), color));
                }
            }
        }
        www.clear();
        www = null;
        matrixDraw.clear();
        matrixDraw = null;

        return newPoints;
    }

    private int getColorByPixel(int pixel) {
        int alphaValue = Color.alpha(pixel);
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);

        return Color.argb(alphaValue, redValue, greenValue, blueValue);
    }

    private int getMinXCoordinates() {
        int minX = (int) matrixDraw.get(0).getX();
        for (CropPoint point : matrixDraw) {
            if (point.getX() < minX) {
                minX = (int) point.getX();
            }
        }
        return minX;
    }

    private int getMinYCoordinates() {
        int minY = (int) matrixDraw.get(0).getY();
        for (CropPoint point : matrixDraw) {
            if (point.getY() < minY) {
                minY = (int) point.getY();
            }
        }
        return minY;
    }

    private int getMaxXCoordinates() {
        int maxX = 0;
        for (CropPoint point : matrixDraw) {
            if (point.getX() > maxX) {
                maxX = (int) point.getX();
            }
        }
        return maxX + 10;
    }

    private int getMaxYCoordinates() {
        int maxY = 0;
        for (CropPoint point : matrixDraw) {
            if (point.getY() > maxY) {
                maxY = (int) point.getY();
            }
        }
        return maxY + 10;
    }

    public boolean hasAreaCropMatrix() {
        return matrixDraw != null && matrixDraw.size() > 0;
    }
}
