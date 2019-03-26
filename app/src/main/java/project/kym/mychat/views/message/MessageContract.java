package project.kym.mychat.views.message;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import project.kym.mychat.model.ChatModel;

public interface MessageContract {

    void init(Intent intent);

    void init(Fragment fragment);

    void loadMessages();

    void onSendButtonClicked(String message);

    void sendMessage(String chatRoomUid, ChatModel.Comment comment);

    void updateLastTimestamp();

    void listenFirstVisiblePosition(int firstCompletelyVisibleItemPosition);

    void onResume();

    void onStop();

    interface View{

        void scrollToPosition(int position);

        void setSendButtonEnabled(boolean enabled);

        void clearEditText();

        void setDateTextView(String text);

        void removeNotification();

        void showProgress(boolean b);
    }
}
