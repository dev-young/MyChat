package project.kym.mychat.views.main.chat;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import project.kym.mychat.R;
import project.kym.mychat.model.ChatModel;
import project.kym.mychat.views.message.MessageActivity;
import project.kym.mychat.views.main.chat.adapter.ChatRecyclerAdapter;

public class ChatFragment extends Fragment implements ChatContract.View {

    ChatPresenter presenter;

    RecyclerView recyclerView;
    ChatRecyclerAdapter recyclerViewAdapter;
    TextView noChatText;

    ChatViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new ChatPresenter(this);
//        presenter.loadChatList();

        viewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        recyclerViewAdapter = new ChatRecyclerAdapter();
        recyclerViewAdapter.setOnItemClickListner(new ChatRecyclerAdapter.OnItemClickListner() {
            @Override
            public void onChatRoomClick(int position, String title) {
                ChatModel chatModel = recyclerViewAdapter.getItem(position);
                startMessageActivity(chatModel.getRoomUid(), title, chatModel.isGroup());
            }
        });
//        presenter.setAdapter(recyclerViewAdapter);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        noChatText = view.findViewById(R.id.noInfoMessage);
        recyclerView  = (RecyclerView) view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(recyclerViewAdapter);

        viewModel.getChatModelMap().observe(this, stringChatModelMap -> recyclerViewAdapter.addItems(false, new ArrayList<ChatModel>(stringChatModelMap.values())));
        viewModel.getIsListEmpty().observe(this, isEmpty -> showNoChatMessage(isEmpty));

        return view;
    }

    @Override
    public void onResume() {
//        presenter.onResume();
        String myUid = FirebaseAuth.getInstance().getUid();
        viewModel.load(myUid);
        super.onResume();
    }

    @Override
    public void onStop() {
//        presenter.onStop();
        viewModel.stop();
        super.onStop();
    }

    @Override
    public void startMessageActivity(String chatRoomUid, String title, boolean isGroup) {
        MessageActivity.start(getActivity(), chatRoomUid, null, isGroup, title);
    }

    @Override
    public void showNoChatMessage(boolean show) {
        if(show)
            noChatText.setVisibility(View.VISIBLE);
        else
            noChatText.setVisibility(View.GONE);
    }
}