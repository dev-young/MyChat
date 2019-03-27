package project.kym.mychat.views;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import project.kym.mychat.R;
import project.kym.mychat.model.ChatModel;
import project.kym.mychat.model.UserModel;
import project.kym.mychat.util.BindingUtil;
import project.kym.mychat.views.main.people.PeopleRepository;
import project.kym.mychat.views.message.MessageActivity;

public class SelectFriendActivity extends BaseActivity {
    ChatModel chatModel = new ChatModel();
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.selectFriendActivity_recyclerview);
        recyclerView.setAdapter(new SelectFriendRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        button = (Button) findViewById(R.id.selectFriendActivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> destinationUIDs = new ArrayList<>();
                destinationUIDs.addAll(chatModel.getUsers().keySet());
                Intent intent = new Intent(view.getContext(), MessageActivity.class);
                intent.putExtra("destinationUids", destinationUIDs);
                MessageActivity.start(SelectFriendActivity.this, null, destinationUIDs, true);
                finish();
            }
        });
    }


    class SelectFriendRecyclerViewAdapter extends RecyclerView.Adapter<SelectFriendRecyclerViewAdapter.CustomViewHolder> {

        List<UserModel> userModels;

        public SelectFriendRecyclerViewAdapter() {
            userModels = new ArrayList<>();
            PeopleRepository.getInstance().getPeopleList(new PeopleRepository.OnUserModelListListener() {
                @Override
                public void onSuccess(List<UserModel> models) {
                    userModels.clear();
                    userModels.addAll(models);
                    notifyDataSetChanged();
                }

                @Override
                public void onError(String errorMessage) {}
            });
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CustomViewHolder holder, final int position) {
            final UserModel userModel = userModels.get(position);
            BindingUtil.loadProfileImage(holder.imageView, userModel.getProfileImageUrl());
            holder.textView.setText(userModel.getUserName());


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                }
            });

            if(userModel.getComment() != null){
                holder.textView_comment.setText(userModel.getComment());
            }
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //체크 된상태
                    if(b){
                        chatModel.getUsers().put(userModel.getUid(),0);
                        //체크 취소 상태
                    }else{
                        chatModel.getUsers().remove(userModel.getUid());
                    }
                    if(chatModel.getUsers().isEmpty())
                        button.setEnabled(false);
                    else
                        button.setEnabled(true);
                }
            });

        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;
            public CheckBox checkBox;

            public CustomViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.frienditem_imageview);
                textView = (TextView) view.findViewById(R.id.frienditem_textview);
                textView_comment = (TextView)view.findViewById(R.id.frienditem_textview_comment);
                checkBox = (CheckBox)view.findViewById(R.id.friendItem_checkbox);
            }
        }
    }
}
