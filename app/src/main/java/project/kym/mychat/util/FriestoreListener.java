package project.kym.mychat.util;

public interface FriestoreListener {

    interface Complete<T>{
        void onCompelete(boolean isSuccess, T result);
    }
}
