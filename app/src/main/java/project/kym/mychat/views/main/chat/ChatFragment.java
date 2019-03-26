package project.kym.mychat.views.main.chat;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import project.kym.mychat.R;
import project.kym.mychat.views.message.MessageActivity;
import project.kym.mychat.views.main.chat.adapter.ChatRecyclerAdapter;

public class ChatFragment extends Fragment implements ChatContract.View {

    ChatPresenter presenter;

    RecyclerView recyclerView;
    ChatRecyclerAdapter recyclerViewAdapter;
    TextView noChatText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new ChatPresenter(this);
//        presenter.loadChatList();

        recyclerViewAdapter = new ChatRecyclerAdapter();
        presenter.setAdapter(recyclerViewAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        noChatText = view.findViewById(R.id.noInfoMessage);
        recyclerView  = (RecyclerView) view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(recyclerViewAdapter);

        return view;
    }

    @Override
    public void onResume() {
        presenter.onResume();
        super.onResume();
    }

    @Override
    public void onStop() {
        presenter.onStop();
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