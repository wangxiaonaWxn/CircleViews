package com.mega.facing.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

/**
 * 圆形摄像头预览控件
 */
public class CircleSurfaceView extends SurfaceView {
    private static final String TAG = "CircleCameraPreview";

    /**
     * 半径
     */
    private int mRadius;

    /**
     * 中心点坐标
     */
    private Point mCenterPoint;

    /**
     * 剪切路径
     */
    private Path mClipPath;

    public CircleSurfaceView(Context context) {
        super(context);
        init();
    }

    public CircleSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        mClipPath = new Path();
        mCenterPoint = new Point();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 坐标转换为实际像素
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("wxn", "=widthSize=" + widthSize);
        Log.d("wxn", "=heightSize=" + heightSize);
        // 计算出圆形的中心点
        mCenterPoint.x = widthSize >> 1;
        mCenterPoint.y = heightSize >> 1;
        // 计算出最短的边的一半作为半径
        mRadius = (mCenterPoint.x > mCenterPoint.y) ? mCenterPoint.y : mCenterPoint.x;
        Log.d("wxn", "radius=" + mRadius);
        Log.i(TAG, "onMeasure: " + mCenterPoint.toString());
        mClipPath.reset();
        mClipPath.addCircle(mCenterPoint.x, mCenterPoint.y, mRadius, Path.Direction.CCW);
        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * 绘制
     *
     * @param canvas 画布
     */
    @Override
    public void draw(Canvas canvas) {
        //裁剪画布，并设置其填充方式
        if (Build.VERSION.SDK_INT >= 26) {
            canvas.clipPath(mClipPath);
        } else {
            canvas.clipPath(mClipPath, Region.Op.REPLACE);
        }
        super.draw(canvas);
    }
}
