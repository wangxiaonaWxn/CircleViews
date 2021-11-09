package com.mega.facing;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RecognitionResultModel extends ViewModel {
    public enum RecognitionErrorCode {
        NO_FACE(),
        NO_EYE(),
        NO_MOUTH()
    }

    public enum ActionType {
        REGISTER_FACE(),
        DELETE_FACE()
    }

    public static class Content {
        public int recognitionResultCode;
        public RecognitionErrorCode errorCode;
        public ActionType actionType;
    }

    MutableLiveData<Content> contentMutableLiveData = new MutableLiveData<>();

    public void setContent(Content content) {
        contentMutableLiveData.postValue(content);
    }

    public LiveData<Content> getContent() {
        return contentMutableLiveData;
    }
}
