package com.mega.facing;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.mega.facing.view.CircleProgressBar;
import com.mega.facing.view.CircleSurfaceView;
import com.mega.facing.view.FaceRecognitionView;

import java.util.Timer;
import java.util.TimerTask;

import static com.mega.facing.FaceRecognitionHelper.ACTION_KEY;
import static com.mega.facing.FaceRecognitionHelper.FACE_ID;

public class FaceRecognitionActivity extends AppCompatActivity implements
        FaceRecognitionView.ButtonClickListener, SurfaceHolder.Callback {
    private static final String[] NEEDED_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int REQ_CODE = 0x001;
    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTask;
    private int mProgress = MAX_PROGRESS;
    private CircleProgressBar mProgressBar;
    private static final int MAX_PROGRESS = 300;
    protected FaceRecognitionView mRecognitionView;
    private RecognitionResultModel mModel;
    private RecognitionResultModel.Content mContent;
    private ImageView mAnimationView;
    private boolean mIsStopAnim = false;
    private AnimatorSet mAnimatorSet = new AnimatorSet();
    private CircleSurfaceView mPreviewView;
    private Camera2Manager mCameraManager;
    private int mFaceId;
    private int mActionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (ContextCompat.checkSelfPermission(this, NEEDED_PERMISSION[0])
                != PackageManager.PERMISSION_GRANTED) {
            // 没有相机权限则停止运行
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSION, REQ_CODE);
            finish();
            return;
        }
        getDataFromIntent(getIntent());
        mPreviewView = findViewById(R.id.preview_view);
        mPreviewView.getHolder().addCallback(this);
        mCameraManager = new Camera2Manager(getApplicationContext());
        mAnimationView = findViewById(R.id.animation_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setMaxProgress(MAX_PROGRESS);
        mProgressBar.setProgress(mProgress);
        mRecognitionView = findViewById(R.id.recognition_view);
        mRecognitionView.setButtonClickListener(this);
        mRecognitionView.setAttrForToastText(getString(R.string.recognizing_toast),
                getColor(R.color.white), getResources().getDimensionPixelSize(R.dimen.sp48));
        mRecognitionView.setAttrForResultText(getString(R.string.recognizing),
                getColor(R.color.progress_bar_success_color),
                        getResources().getDimensionPixelSize(R.dimen.sp36), -1);
        mRecognitionView.setVisibleForRetryButton(View.GONE);
        mRecognitionView.updateCancelButton(getString(R.string.cancel),
                getResources().getDimension(R.dimen.dp_done));
        mContent = new RecognitionResultModel.Content();
        MessageHelper.setMode(mModel);
        startTimer();
        startAnim();
        Button button = findViewById(R.id.start_anim);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               startAnim();
                mContent.recognitionResultCode = 1;
                mModel.setContent(mContent);
            }
        });

        Button stop = findViewById(R.id.stop_anim);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              stopAnim();
                mContent.recognitionResultCode = 2;
                mModel.setContent(mContent);
            }
        });
        // TODO: 21-11-4 send request to facing recognition after 2s
        mModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory())
                .get(RecognitionResultModel.class);
        mModel.getContent().observe(this, content -> {
            switch (content.actionType) {
                case DELETE_FACE:
                    if (content.recognitionResultCode == 1) {
                        FaceRecognitionHelper.getInstance().registerNewFace(mFaceId);
                    } else if (content.recognitionResultCode == 0) {
                        FaceRecognitionHelper.getInstance().deleteFace(mFaceId);
                    }
                    break;
                case REGISTER_FACE:
                    if (content.recognitionResultCode == 1) {
                        // recognition success
                        stopTimer();
                        mProgressBar.setProgress(0);
                        mProgressBar.setBackgroundColor(getColor(R.color.progress_bar_success_color));
                        mRecognitionView.setVisibleForToastView(View.GONE);
                        mRecognitionView.setVisibleForRetryButton(View.GONE);
                        mRecognitionView.setAttrForResultText(getString(R.string.success_toast),
                                getColor(R.color.progress_bar_success_color),
                                getResources().getDimensionPixelSize(R.dimen
                                        .recognition_success_text_size),
                                R.drawable.ic_recognition_success_icon);
                        mRecognitionView.updateCancelButton(getString(R.string.done),
                                getResources().getDimension(R.dimen.dp_done));
                        stopAnim();
                    } else if (content.recognitionResultCode == 2) {
                        stopTimer();
                        setRecognitionFailView();
                        stopAnim();
                    } else {
                        // recognition fail
                        if (mProgress > 0) {
                            // TODO: 21-11-4  retry, update toast with error code
                            Toast.makeText(getApplicationContext(), "faile", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                default:
                    break;
            }
        });
    }

    private void getDataFromIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        mActionType = intent.getIntExtra(ACTION_KEY,
                FaceRecognitionHelper.ActionType.REGISTER.value);
        mFaceId = intent.getIntExtra(FACE_ID, -1);
        if (mActionType == FaceRecognitionHelper.ActionType.REGISTER.value) {
            FaceRecognitionHelper.getInstance().registerNewFace(mFaceId);
        } else if (mActionType == FaceRecognitionHelper.ActionType.UPDATE.value) {
            FaceRecognitionHelper.getInstance().deleteFace(mFaceId);
        }
    }

    private void startTimer() {
        mUpdateProgressTimer = new Timer();
        mUpdateProgressTask = new TimerTask() {
            @Override
            public void run() {
                mProgress--;
                if (mProgress <= 0) {
                    stopTimer();
                    setRecognitionFailView();
                    stopAnim();
                }
                mProgressBar.setProgress(mProgress);
            }
        };
        mUpdateProgressTimer.schedule(mUpdateProgressTask, 1000, 100);
    }

    public void setRecognitionFailView() {
        mRecognitionView.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setProgress(0);
                mProgressBar.setBackgroundColor(getColor(R.color.progress_bar_fail_color));
                mRecognitionView.setAttrForResultText(getString(R.string.fail_toast),
                        getColor(R.color.progress_bar_fail_color),
                        getResources().getDimensionPixelSize(R.dimen.recognition_fail_text_size),
                        R.drawable.ic_recognition_fail_icon);
                mRecognitionView.setVisibleForToastView(View.GONE);
                mRecognitionView.setVisibleForRetryButton(View.VISIBLE);
                mRecognitionView.updateCancelButton(getString(R.string.cancel),
                        getResources().getDimension(R.dimen.dp_retry));
                mRecognitionView.updateRetryButton(getString(R.string.retry),
                        getResources().getDimension(R.dimen.dp_retry));
            }
        });
    }

    private void stopTimer() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer.cancel();
            mUpdateProgressTimer = null;
        }

        if (mUpdateProgressTask != null) {
            mUpdateProgressTask.cancel();
            mUpdateProgressTask = null;
        }
    }

    @Override
    public void cancelClick() {
        stopAnim();
        finish();
    }

    @Override
    public void doneClick() {
        startAnim();
        finish();
    }

    @Override
    public void retryClick() {
        mProgress = MAX_PROGRESS;
        mProgressBar.setProgress(mProgress);
        mProgressBar.setBackgroundColor(getColor(R.color.progress_bar_bg_color));
        mRecognitionView.setVisibleForToastView(View.VISIBLE);
        mRecognitionView.setAttrForToastText(getString(R.string.recognizing_toast),
                getColor(R.color.white), getResources().getDimensionPixelSize(R.dimen.sp48));
        mRecognitionView.setAttrForResultText(getString(R.string.recognizing),
                getColor(R.color.progress_bar_success_color),
                getResources().getDimensionPixelSize(R.dimen.sp36), -1);
        mRecognitionView.setVisibleForRetryButton(View.GONE);
        mRecognitionView.updateCancelButton(getString(R.string.cancel),
                getResources().getDimension(R.dimen.dp_done));
        startTimer();
        startAnim();
    }

    private void initAnimation() {
        //Y轴平移
        ObjectAnimator tran1 = ObjectAnimator.ofFloat(mAnimationView, "translationY", 120);
        tran1.setDuration(1000);
        ObjectAnimator downScale0T1 = ObjectAnimator.ofFloat(mAnimationView, "ScaleX", 0.7f, 1.0f);
        downScale0T1.setDuration(1000);
        ObjectAnimator tran2 = ObjectAnimator.ofFloat(mAnimationView, "translationY", 120, 240);
        tran2.setDuration(1000);
        ObjectAnimator downScale1T0 = ObjectAnimator.ofFloat(mAnimationView, "ScaleX", 1.0f, 0.7f);
        downScale1T0.setDuration(1000);
        ObjectAnimator tran3 = ObjectAnimator.ofFloat(mAnimationView, "translationY", 240, 120);
        tran3.setDuration(1000);
        ObjectAnimator upScale0T1 = ObjectAnimator.ofFloat(mAnimationView, "ScaleX", 0.7f, 1.0f);
        upScale0T1.setDuration(1000);
        ObjectAnimator tran4 = ObjectAnimator.ofFloat(mAnimationView, "translationY", 120, 0);
        tran4.setDuration(1000);
        ObjectAnimator upScale1T0 = ObjectAnimator.ofFloat(mAnimationView, "ScaleX", 1.0f, 0.7f);
        upScale1T0.setDuration(1000);

        mAnimatorSet.playTogether(tran1, downScale0T1);
        mAnimatorSet.play(tran2).after(tran1);
        mAnimatorSet.play(downScale1T0).after(tran1);
        mAnimatorSet.play(tran3).after(tran2);
        mAnimatorSet.play(upScale0T1).after(tran2);
        mAnimatorSet.play(tran4).after(tran3);
        mAnimatorSet.play(upScale1T0).after(tran3);
        mAnimatorSet.start();
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mIsStopAnim) {
                    mAnimatorSet.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void startAnim() {
        if (mIsStopAnim) {
            mAnimatorSet.reverse();
            mAnimationView.setVisibility(View.VISIBLE);
            mIsStopAnim = false;
        } else {
            initAnimation();
        }
    }

    private void stopAnim() {
        mAnimationView.post(new Runnable() {
            @Override
            public void run() {
                mIsStopAnim = true;
                mAnimatorSet.cancel();
                mAnimationView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        stopAnim();
        if (mCameraManager != null) {
            mCameraManager.stopPreview();
            mCameraManager.releaseCamera();
        }
        MessageHelper.setMode(null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCameraManager.setPreviewTexture(holder.getSurface());
        mCameraManager.open(0, null, 960, 1280);
        mCameraManager.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}
