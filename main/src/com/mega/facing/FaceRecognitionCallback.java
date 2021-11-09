package com.mega.facing;

public interface FaceRecognitionCallback {
    void registerResult(boolean result, int faceId);

    void deleteFaceResult(boolean result, int faceId);

    void updateFaceResult(boolean result, int faceId);

    void recognizeResult(boolean result, int facId);
}
