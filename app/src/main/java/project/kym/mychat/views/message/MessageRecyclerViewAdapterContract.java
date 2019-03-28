package project.kym.mychat.views.message;

import java.util.List;
import java.util.Map;

import project.kym.mychat.model.ChatModel;
import project.kym.mychat.model.UserModel;

public interface MessageRecyclerViewAdapterContract {

    interface View {

        void notifyAdapter();

        void notifyItemInserted();

        void notifyItemUpdated(int position);
    }

    interface Model {

        void init(Map<String, UserModel> users, String chatRoomUid, String myUid);

        ChatModel.Comment getItem(int position);

        List<ChatModel.Comment> getItems();

        int getItemsCount();

        void addItem(ChatModel.Comment comment);

        void addItem(String key, ChatModel.Comment comment);

        int updateReadUsers(String key, ChatModel.Comment c);

        void updateReadUsers(Map<String, String> lastRead);
    }
}
