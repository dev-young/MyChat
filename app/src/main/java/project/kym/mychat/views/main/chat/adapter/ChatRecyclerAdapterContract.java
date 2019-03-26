package project.kym.mychat.views.main.chat.adapter;

import java.util.List;

import project.kym.mychat.OnItemClickListner;
import project.kym.mychat.model.ChatModel;

public interface ChatRecyclerAdapterContract {

    interface View {

        void setOnItemClickListner(OnItemClickListner onItemClickListner);

        void notifyAdapter();
    }

    interface Model {

        ChatModel getItem(int position);

        List<ChatModel> getItems();

        void addItems(boolean isClear, List<ChatModel> models);

        void addItem(ChatModel chatModel);

        void addItem(String key, ChatModel chatModel);

        void updateItem(String key, ChatModel chatModel);

        void clearItem();
    }
}
