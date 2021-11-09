package com.mega.facing.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.mega.facing.R;

public class CircleProgressBar extends View {
    private static final String TAG = CircleProgressBar.class.getSimpleName();
    private Paint mBackgroundPaint;
    private Paint mProgressPaint;
    private float mBackgroundWidth;
    private int mBackgroundColor;
    private float mProgressWidth;
    private int mProgressColor;
    private int mProgress;
    private int mMaxProgress;
    private RectF mRectF;
    private int mProgressEndAngle;

    public CircleProgressBar(Context context) {
        super(context);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.CircleProgressBarStyle);

        mBackgroundWidth = typedArray
                .getDimension(R.styleable.CircleProgressBarStyle_background_width, 20f);
        mProgressWidth = typedArray
                .getDimension(R.styleable.CircleProgressBarStyle_progress_width, 20f);
        mBackgroundColor = typedArray
                .getColor(R.styleable.CircleProgressBarStyle_background_color, Color.GRAY);
        mProgressColor = typedArray
                .getColor(R.styleable.CircleProgressBarStyle_progress_color, Color.GREEN);
        mProgress = typedArray.getInt(R.styleable.CircleProgressBarStyle_progress, 0);
        mMaxProgress = typedArray
                .getInt(R.styleable.CircleProgressBarStyle_max_progress, 100);
        mProgressEndAngle = typedArray
                .getInt(R.styleable.CircleProgressBarStyle_progress_end_angel, 0);
        typedArray.recycle();
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.STROKE);    // 只描边，不填充
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);   // 设置圆角
        mBackgroundPaint.setAntiAlias(true);              // 设置抗锯齿
        mBackgroundPaint.setDither(true);                 // 设置抖动
        mBackgroundPaint.setStrokeWidth(mBackgroundWidth);
        mBackgroundPaint.setColor(mBackgroundColor);
        mProgressPaint = new Paint();
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setDither(true);
        mProgressPaint.setStrokeWidth(mProgressWidth);
        mProgressPaint.setColor(mProgressColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int viewWide = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int viewHigh = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int rectLength = (int) ((viewWide > viewHigh ? viewHigh : viewWide)
                - (mBackgroundPaint.getStrokeWidth() > mProgressPaint.getStrokeWidth()
                ? mBackgroundPaint.getStrokeWidth() : mProgressPaint.getStrokeWidth()));
        int rectL = getPaddingLeft() + (viewWide - rectLength) / 2;
        int rectT = getPaddingTop() + (viewHigh - rectLength) / 2;
        mRectF = new RectF(rectL, rectT, rectL + rectLength, rectT + rectLength);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mRectF, 0, 360, false, mBackgroundPaint);
        canvas.drawArc(mRectF, mProgressEndAngle, 360 * mProgress / mMaxProgress,
                false, mProgressPaint);
    }

    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        post(() -> invalidate());
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        mBackgroundPaint.setColor(color);
        post(() -> invalidate());
    }
}
