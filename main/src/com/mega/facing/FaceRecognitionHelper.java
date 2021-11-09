package com.mega.facing;

import android.content.Context;
import android.content.Intent;

public class FaceRecognitionHelper {
    private FaceRecognitionCallback mCallback;
    private int mCurrentFaceId;
    public static final String ACTION_KEY = "action_key";
    public static final String FACE_ID = "face_id";
    public ActionType currentType;

    enum ActionType {
        REGISTER(0),
        UPDATE(1);
        public int value;
        ActionType(int  v) {
            value = v;
        }
    }

    public static FaceRecognitionHelper getInstance() {
        return ClassHolder.INSTANCE;
    }

    public static class ClassHolder {
        private static final FaceRecognitionHelper INSTANCE = new FaceRecognitionHelper();
    }

    public void registerFace(Context context, int faceId) {
        Intent intent = new Intent(context, FaceRecognitionActivity.class);
        intent.putExtra(ACTION_KEY, ActionType.REGISTER.value);
        intent.putExtra(FACE_ID, faceId);
        context.startActivity(intent);
        currentType = ActionType.REGISTER;
    }

    public void updateNewFace(Context context, int faceId) {
        Intent intent = new Intent(context, FaceRecognitionActivity.class);
        intent.putExtra(ACTION_KEY, ActionType.UPDATE.value);
        intent.putExtra(FACE_ID, faceId);
        context.startActivity(intent);
        currentType = ActionType.UPDATE;
    }

    public void registerNewFace(int faceId) {
        mCurrentFaceId = faceId;
    }

    public void deleteFace(int faceId) {
        mCurrentFaceId = faceId;
    }

    public void changeFace(int faceId) {
        mCurrentFaceId = faceId;
        //更换人脸，先删除旧的，在注册新的， 如果删除成功了，注册没成功，那旧的不能用了，也没有新的可以用了？？？
    }

    public void recognizeFace() {
        // TODO: 21-11-8 call delete and then call register
    }

    public void checkCertificate() {
        // TODO: 21-11-9 check if Certificate is invalidate
    }

    public void getCertificate() {
        // TODO: 21-11-9 getCertificate from network
    }

    public void updateCertificate(String certificate) {
        // TODO: 21-11-9 update Certificate after get new Certificate from network
    }

    public void registerCallback(FaceRecognitionCallback callback) {
        mCallback = callback;
    }

    public void unRegisterCallback() {
        mCallback = null;
    }

    public void registerResult(boolean result) {
        if (mCallback != null) {
            mCallback.registerResult(result, mCurrentFaceId);
        }
    }

    public void updateFaceResult(boolean result) {
        if (mCallback != null) {
            mCallback.updateFaceResult(result, mCurrentFaceId);
        }
    }

    public void deleteResult(boolean result) {
        if (mCallback != null && currentType != ActionType.UPDATE) {
            mCallback.deleteFaceResult(result, mCurrentFaceId);
        }
    }

    public void recognizeResult(boolean result, int faceId) {
        if (mCallback != null) {
            mCallback.recognizeResult(result, faceId);
        }
    }
}
