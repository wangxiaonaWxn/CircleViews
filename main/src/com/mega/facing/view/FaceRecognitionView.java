package com.mega.facing.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mega.facing.R;

public class FaceRecognitionView extends RelativeLayout {
    private TextView mToastView;
    private TextView mRecognitionResultView;
    private Button mRetry;
    private Button mCancel;
    private Context mContext;
    private ButtonClickListener mButtonClickListener;

    public FaceRecognitionView(Context context) {
        super(context);
        init(context);
    }

    public FaceRecognitionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setButtonClickListener(ButtonClickListener clickListener) {
        mButtonClickListener = clickListener;
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.face_recognition_view, this,
                true);
        mToastView = findViewById(R.id.toast_view);
        mRecognitionResultView = findViewById(R.id.result);
        mRetry = findViewById(R.id.retry);
        mCancel = findViewById(R.id.cancel);
        mRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mButtonClickListener != null) {
                    mButtonClickListener.retryClick();
                }
            }
        });

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mButtonClickListener == null) {
                     return;
                }
                if (mCancel.getText().equals(getResources().getString(R.string.cancel))) {
                    mButtonClickListener.cancelClick();
                } else {
                    mButtonClickListener.doneClick();
                }
            }
        });
    }

    public void setAttrForResultText(String text, int textColor, float textSize, int imageId) {
        mRecognitionResultView.setText(text);
        mRecognitionResultView.setTextSize(textSize);
        mRecognitionResultView.setTextColor(textColor);
        if (imageId == -1) {
            mRecognitionResultView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                    null, null, null);
        } else {
            mRecognitionResultView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    mContext.getDrawable(imageId),
                    null, null, null);
        }
    }

    public void setAttrForToastText(String text, int textColor, float textSize) {
        mToastView.setText(text);
        mToastView.setTextColor(textColor);
        mToastView.setTextSize(textSize);
    }

    public void updateCancelButton(String text, float width) {
        mCancel.setText(text);
        LinearLayout.LayoutParams ps = (LinearLayout.LayoutParams) mCancel.getLayoutParams();
        ps.width = (int) width;
        mCancel.setLayoutParams(ps);
    }

    public void updateRetryButton(String text, float width) {
        mRetry.setText(text);
        LinearLayout.LayoutParams ps = (LinearLayout.LayoutParams) mRetry.getLayoutParams();
        ps.width = (int) width;
        mRetry.setLayoutParams(ps);
    }

    public void setVisibleForToastView(int visible) {
        mToastView.setVisibility(visible);
    }

    public void setVisibleForRetryButton(int visible) {
        mRetry.setVisibility(visible);
    }

   public interface ButtonClickListener {
        void cancelClick();

        void doneClick();

        void retryClick();
    }
}
