package com.mega.facing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Camera2Manager {
    private static final String TAG = Camera2Manager.class.getSimpleName();
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private Surface mPreviewSurface;
    private Size mPreviewSize;
    private boolean mIsPreview;
    private Handler mWorkHandler;
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "camera opened");
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "camera disconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "camera openError");
        }
    };

    public void setWorkHandler(Handler handler) {
        mWorkHandler = handler;
    }

    public Camera2Manager(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public boolean open(int cameraId, Handler handler, int height, int width) {
        if (mCameraDevice != null) {
            releaseCamera();
        }
        try {
            Log.d(TAG, "height==" + height);
            Log.d(TAG, "width==" + width);
            CameraCharacteristics characteristics = mCameraManager
                    .getCameraCharacteristics(String.valueOf(cameraId));
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
           Size  videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, videoSize);
            mCameraManager.openCamera(String.valueOf(cameraId), mStateCallback, mWorkHandler);
            return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w
                    && option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight()
                    - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private final CameraCaptureSession.StateCallback mSessionCallback
            = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (mCameraDevice == null) {
                return;
            }
            mCaptureSession = session;
            try {
                startRequest(null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            repeatPreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Failed to configure capture session.");
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            if (mCaptureSession != null && mCaptureSession.equals(session)) {
                Log.e(TAG, "camera closed , set session as null");
                mCaptureSession = null;
            }
        }
    };

    public void startPreview() {
        Log.d(TAG, "start preview");
        if (mCameraDevice == null || mPreviewSurface == null) {
            Log.d(TAG, "mCameraDevice=" + mCameraDevice);
            Log.d(TAG, "mSurfaceTexture=" + mPreviewSurface);
            return;
        }
        Log.d(TAG, "start preview");
        mIsPreview = true;
        Log.d(TAG, "mPreviewSize==" + mPreviewSize.getWidth());
        Log.d(TAG, " mPreviewSize.getHeight()==" + mPreviewSize.getHeight());
        List<Surface> targets = new ArrayList<>();
       // mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//        mPreviewSurface = new Surface(mSurfaceTexture);
        targets.add(mPreviewSurface);
        try {
            mCameraDevice.createCaptureSession(targets, mSessionCallback, mWorkHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopPreview() {
        Log.d(TAG, "stop preview");
        if (!mIsPreview || mCaptureSession == null) {
            Log.e(TAG, "preview has already stopped");
            return;
        }
        mIsPreview = false;
        try {
            mCaptureSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void releaseCamera() {
        Log.d(TAG, "releaseCamera");
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    public void setPreviewTexture(Surface texture) {
        Log.d(TAG, "setPreviewTexture");
        mPreviewSurface = texture;
    }

    private void startRequest(Handler handler) throws CameraAccessException {
        mPreviewRequestBuilder =
                mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewRequestBuilder.addTarget(mPreviewSurface);
        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
    }

    private void repeatPreview() {
        //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(),
                    null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
