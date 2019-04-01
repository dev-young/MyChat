package project.kym.mychat.views.main.chat.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.SortedList;
import project.kym.mychat.OnItemClickListner;
import project.kym.mychat.R;
import project.kym.mychat.model.ChatModel;
import project.kym.mychat.model.UserModel;
import project.kym.mychat.util.BindingUtil;
import project.kym.mychat.util.RLog;

public class ChatRecyclerAdapter extends  RecyclerView.Adapter<ChatRecyclerAdapter.CustomViewHolder> implements ChatRecyclerAdapterContract.View, ChatRecyclerAdapterContract.Model{
    Map<String, Integer> positionMap = new HashMap<>();    //리스트의 인덱스와 키값을 맵으로 저장 <ChatModel의 키값, 리스트에서 ChatModel의 인덱스>
    private SortedList<ChatModel> chatModels;
    private String myUid;
    private OnItemClickListner onItemClickListner;

    Calendar currentTime;
    int year, month, day;

    public ChatRecyclerAdapter() {
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        chatModels = new ArrayList<>();
        chatModels = new SortedList<>(ChatModel.class, new SortedList.Callback<ChatModel>() {
            @Override
            public int compare(ChatModel o1, ChatModel o2) {
                return -(o1.getTimestamp().compareTo(o2.getTimestamp()));
            }

            @Override
            public void onChanged(int position, int count) {
                RLog.i("position: " + position + " count: " + count);
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(ChatModel oldItem, ChatModel newItem) {
                RLog.i("oldItem: " + oldItem.getMessage() + " newItem: " + newItem.getMessage());
                return oldItem.getTimestamp().equals(newItem.getTimestamp());
            }

            @Override
            public boolean areItemsTheSame(ChatModel item1, ChatModel item2) {
                boolean b = item1.getTimestamp().equals(item2.getTimestamp());
                RLog.i("oldItem: " + item1.getMessage() + " newItem: " + item2.getMessage() + " result: " +b);
                return b;
            }

            @Override
            public void onInserted(int position, int count) {
                RLog.i("position: " + position + " count: " + count);
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                RLog.i("position: " + position + " count: " + count);
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                RLog.i("fromPosition: " + fromPosition + " toPosition: " + toPosition);
                notifyItemMoved(fromPosition, toPosition);
            }
        });

        currentTime = Calendar.getInstance();
        year = currentTime.get(Calendar.YEAR);
        month = currentTime.get(Calendar.MONTH);
        day = currentTime.get(Calendar.DATE);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room,parent,false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final ChatModel chatModel = chatModels.get(position);

        String destinationUid = "";
        // 챗방에 있는 유저들의 리스트를  체크
        for(String user: chatModel.getUsers().keySet()){
            if(!user.equals(myUid)){
                destinationUid = user;
                break;
            }
        }

        // 방 이름
        final boolean isGroup = chatModel.isGroup();
        FirebaseFirestore.getInstance().collection("users").document(destinationUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    UserModel userModel =  task.getResult().toObject(UserModel.class);
                    if(isGroup){
                        // 그룹챗인 경우
                        holder.imageView.setImageResource(R.drawable.group_24dp);
                        holder.textView_title.setText(chatModel.getTitle());
//                        holder.textView_title.setText(userModel.userName + "외 " + (chatModel.users.size()-1) + "명");
                    } else {
                        // 1:1 채팅인 경우
                        holder.textView_title.setText(userModel.getUserName());
                        BindingUtil.loadProfileImage(holder.imageView, userModel.getProfileImageUrl());
                    }
                } else {
                    RLog.e(task.getException().getMessage());
                }
            }
        });



        //메시지
        holder.textView_last_message.setText(chatModel.getMessage());

        //TimeStamp
        if(chatModel.getTimestamp() != null) // 가끔 서버 딜레이로 널이 들어갈 수 있음
            setDate(holder.textView_timestamp, chatModel.getTimestamp());

        // 내가 안읽은 메시지 갯수 확인
        int cnt = chatModel.getUsers().get(myUid);
        if(cnt == 0)
            holder.textView_new_msgcnt.setVisibility(View.GONE);
        else {
            holder.textView_new_msgcnt.setVisibility(View.VISIBLE);
            holder.textView_new_msgcnt.setText(cnt+"");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListner != null){
                    onItemClickListner.onItemClick(position);
                }
            }
        });

    }

    /** 현재 시간과 비교하여 날짜를 표시한다. */
    void setDate(TextView textView, long timestamp){

        Date date = new Date(timestamp);
        Calendar time = Calendar.getInstance();
        time.setTime(date);
        SimpleDateFormat dateFormat;
        if(time.get(Calendar.YEAR) == year){
            if(time.get(Calendar.DATE) == day && time.get(Calendar.MONTH) == month){
                dateFormat = new SimpleDateFormat("a h:mm");
            } else {
                dateFormat = new SimpleDateFormat("M월 d일");
            }
        } else {
            dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        }
        textView.setText(dateFormat.format(date));
    }

    void setDate(TextView textView, Date date){
        Calendar time = Calendar.getInstance();
        time.setTime(date);
        SimpleDateFormat dateFormat;
        if(time.get(Calendar.YEAR) == year){
            if(time.get(Calendar.DATE) == day && time.get(Calendar.MONTH) == month){
                dateFormat = new SimpleDateFormat("a h:mm");
            } else {
                dateFormat = new SimpleDateFormat("M월 d일");
            }
        } else {
            dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        }
        textView.setText(dateFormat.format(date));
    }

    @Override
    public int getItemCount() {
        return chatModels.size();
    }

    @Override
    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    @Override
    public void notifyAdapter() {
        notifyDataSetChanged();
    }

    @Override
    public ChatModel getItem(int position) {
        return chatModels.get(position);
    }

    @Override
    public SortedList<ChatModel> getItems() {
        return  chatModels;
    }

    @Override
    public void addItems(boolean isClear, List<ChatModel> models) {
        chatModels.beginBatchedUpdates();
        if(isClear)
            chatModels.clear();

        for(ChatModel chatModel : models){
            chatModels.add(chatModel);
        }
        chatModels.endBatchedUpdates();
    }

    @Override
    public void addItem(ChatModel chatModel) {
        chatModels.add(chatModel);
    }

    @Override
    public void addItem(String key, ChatModel chatModel) {
//        chatModels.add(chatModel);
//        positionMap.put(key, chatModels.size()-1);
        int addedInedx = chatModels.add(chatModel);
        positionMap.put(key, addedInedx);
        RLog.i("addedInedx = " + addedInedx);
    }

    @Override
    public void updateItem(String key, ChatModel chatModel) {
        int targetPosition = 0;
        for (int i = 0; i < chatModels.size(); i++) {
            if(chatModels.get(i).getRoomUid().equals(chatModel.getRoomUid())){
                targetPosition = i;
                break;
            }
        }
//        chatModels.add(0, chatModel);
        chatModels.updateItemAt(targetPosition, chatModel);
//        chatModels.set(targetPosition, chatModel);
    }

    @Override
    public void clearItem() {
        chatModels.clear();
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView textView_title;
        public TextView textView_last_message;
        public TextView textView_timestamp;
        public TextView textView_new_msgcnt;

        public CustomViewHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.chatitem_imageview);
            textView_title = (TextView)view.findViewById(R.id.chatitem_textview_title);
            textView_last_message = (TextView)view.findViewById(R.id.chatitem_textview_lastMessage);
            textView_timestamp = (TextView)view.findViewById(R.id.chatitem_textview_timestamp);
            textView_new_msgcnt = view.findViewById(R.id.chatitem_textview_NewMsgCount);
        }
    }
}