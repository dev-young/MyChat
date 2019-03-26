package project.kym.mychat.views.main.chat;

public interface ChatContract {

    void loadChatList();

    void onResume();

    void onStop();

    interface View{

        void startMessageActivity(String chatRoomUid, String title, boolean isGroup);

        void showNoChatMessage(boolean show);

    }
}
