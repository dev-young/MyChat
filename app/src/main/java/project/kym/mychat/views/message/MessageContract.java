package project.kym.mychat.views.message;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import project.kym.mychat.model.ChatModel;

public interface MessageContract {

    void init(Bundle bundle);

    @Deprecated void init(Intent intent);

    @Deprecated void init(Fragment fragment);

    void loadMessagesAndListen();

    void onSendButtonClicked(String message);

    void sendMessage(String chatRoomUid, ChatModel.Comment comment);

    void updateLastTimestamp();

    void listenFirstVisiblePosition(int firstCompletelyVisibleItemPosition);

    void onResume();

    void onStop();

    void onDestroy();

    interface View{

        void setTitlebar(String title);

        void scrollToLastPosition(boolean forced);

        void scrollToPosition(int position);

        void setSendButtonEnabled(boolean enabled);

        void clearEditText();

        void setDateTextView(String text);

        void removeNotification();

        void showProgress(boolean b);
    }
}
