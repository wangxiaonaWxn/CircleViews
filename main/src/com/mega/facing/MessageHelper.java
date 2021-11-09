package com.mega.facing;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mega.car.CarPropertyManager;
import mega.car.MegaCarPropHelper;

import static mega.car.config.Driving.ID_DRV_INFO_SPEED_INFO;
import static mega.car.config.Lighting.ID_TELLTALE_STS_PASSENGER_SEAT_BELT_LAMP;
import static mega.car.config.Lighting.ID_TELLTALE_STS_SEAT_BELT_LAMP;

public class MessageHelper {
    private static final String TAG = MessageHelper.class.getSimpleName();

    private MegaCarPropHelper mMegaCarPropHelper;
    private Set<Integer> mCallbackIds = new HashSet<>(Arrays.asList(
            ID_DRV_INFO_SPEED_INFO,
            ID_TELLTALE_STS_SEAT_BELT_LAMP,
            ID_TELLTALE_STS_PASSENGER_SEAT_BELT_LAMP
    ));
    private CarPropertyManager.CarPropChangeCallback mCarPropChangeCallback;
    private static RecognitionResultModel sMode;
    private RecognitionResultModel.Content mContent = new RecognitionResultModel.Content();

    public MessageHelper(Activity activity) {
        mMegaCarPropHelper = MegaCarPropHelper.getInstance(activity,
                new Handler(activity.getMainLooper()));
    }

    public static void setMode(RecognitionResultModel mode) {
        sMode = mode;
    }

    public void registerCallback() {
        Log.d(TAG, "registerCallback: >>>");
        mCarPropChangeCallback = (int id, Object status, boolean isSetFailFallback) -> {
            switch (id) {
                case ID_DRV_INFO_SPEED_INFO:
                    if (sMode != null) {
                        mContent.recognitionResultCode = 1;
                        sMode.setContent(mContent);
                    }
                    // TODO: 21-11-9 if delete
                    FaceRecognitionHelper.getInstance().deleteResult(true);
                    break;
                default:
                    Log.d(TAG, "registerCallback: invalid cmd!");
                    break;
            }
        };
        mMegaCarPropHelper.registerCallback(mCarPropChangeCallback, mCallbackIds);
    }

    public void unregisterCallback() {
        Log.d(TAG, "unregisterCallback: >>>");
        mMegaCarPropHelper.unregisterCallback(mCarPropChangeCallback, mCallbackIds);
    }
}
