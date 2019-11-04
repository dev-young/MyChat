package project.kym.mychat.util;

public interface FirebaseListener {

    interface Complete<T>{
        void onCompelete(boolean isSuccess, T result);
    }

    interface LoadCompleteListener<T>{
        void onComplete(boolean isSuccess, T resutl);
    }

    interface UpdateCompleteListener {
        void onComplete(boolean isSuccess);
    }

    interface UploadCompleteListener<T> {
        void onComplete(boolean isSuccess, T result);
    }
}
