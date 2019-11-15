package project.kym.mychat.views.main.chat;

import com.google.firebase.auth.FirebaseAuth;

import project.kym.mychat.OnItemClickListner;
import project.kym.mychat.model.ChatModel;
import project.kym.mychat.repository.ChatRepository;
import project.kym.mychat.util.RLog;
import project.kym.mychat.views.main.chat.adapter.ChatRecyclerAdapter;
import project.kym.mychat.views.main.chat.adapter.ChatRecyclerAdapterContract;

public class ChatPresenter implements ChatContract {
    View view;
    ChatRecyclerAdapterContract.Model adapterModel;
    ChatRecyclerAdapterContract.View adapterView;

    public ChatPresenter(View view) {
        this.view = view;
    }

    public void setAdapter(ChatRecyclerAdapter adapter) {
        this.adapterModel = adapter;
        this.adapterView = adapter;
        adapterView.setOnItemClickListner(new ChatRecyclerAdapter.OnItemClickListner() {
            @Override
            public void onChatRoomClick(int position, String title) {
                ChatModel chatModel = adapterModel.getItem(position);
                view.startMessageActivity(chatModel.getRoomUid(), title, chatModel.isGroup());
            }
        });
    }

    @Override
    public void loadChatList() {
//        ChatRepository.getInstance().initChatListListener(new ChatRepository.DataChangeListener() {
//            @Override
//            public void onChanged(List<ChatModel> chatModels) {
//                adapterModel.addItems(true, chatModels);
//                adapterView.notifyAdapter();
//            }
//        });
    }

    @Override
    public void onResume() {
        RLog.i();
//        adapterModel.clearItem();
        String myUid = FirebaseAuth.getInstance().getUid();
        ChatRepository.getInstance().startListen(myUid, new ChatRepository.OnEventListener() {
            @Override
            public void onAdded(String key, ChatModel chatModel) {
                adapterModel.addItem(key, chatModel);
//                adapterView.notifyAdapter();

                if(adapterModel.getItems().size() < 1){
                    view.showNoChatMessage(true);
                } else {
                    view.showNoChatMessage(false);
                }
            }

            @Override
            public void onChanged(String key, ChatModel chatModel) {
                adapterModel.updateItem(key, chatModel);
//                adapterView.notifyAdapter();
            }
        });
    }

    @Override
    public void onStop() {
        ChatRepository.getInstance().stopListen();
//        ChatRepository.getInstance().removeListener();
        RLog.i("valueEventListener removed!");
    }
}
