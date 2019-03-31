    package project.kym.mychat.views.message;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.kym.mychat.R;
import project.kym.mychat.databinding.ItemMessageLeftBinding;
import project.kym.mychat.databinding.ItemMessageRightBinding;
import project.kym.mychat.model.ChatModel;
import project.kym.mychat.model.UserModel;
import project.kym.mychat.util.BindingUtil;
import project.kym.mychat.util.RLog;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.MessageViewHodler> implements MessageRecyclerViewAdapterContract.Model, MessageRecyclerViewAdapterContract.View{
    private final int TYPE_MESSAGE_LEFT = 1;
    private final int TYPE_MESSAGE_RIGHT = 2;
    private final int TYPE_DATE = 10;

    List<ChatModel.Comment> comments = new ArrayList<>();
    Map<String, Integer> commentMap = new HashMap<>();    //리스트의 인덱스와 키값을 맵으로 저장 <Comment의 키값, 리스트에서 Comment의 인덱스>
    Map<String, UserModel> users;
    Map<String, String> lastRead;
    String chatRoomUid;
    String myUid;

    int peopleCount = 0;    // 채팅방 인원 수
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a h:mm");

    public MessageRecyclerViewAdapter() {
    }

    @Override
    public MessageViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageViewHodler hodler;
        switch (viewType){
            case TYPE_MESSAGE_LEFT:
                ItemMessageLeftBinding binding1 = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_message_left, parent, false);
                hodler = new LeftTextMessageViewHolder(binding1);
                break;

            case TYPE_MESSAGE_RIGHT:
                ItemMessageRightBinding binding2 = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_message_right, parent, false);
                hodler = new RightTextMessageViewHolder(binding2);
                break;

            default:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_divider,parent,false);
                hodler = new DateMessageViewHolder(view);
        }

        return hodler;

    }

    @Override
    public void onBindViewHolder(MessageViewHodler viewHolder, int position) {
        int itemType = getItemViewType(position);
        ChatModel.Comment comment = comments.get(position);
        String uid = comment.getUid();
        UserModel userModel = users.get(uid);
//        int count = peopleCount - comment.getReadUsers().size();
        int count = getUnReadUserCount(position);

        if(itemType == TYPE_DATE){
            ((DateMessageViewHolder)viewHolder).setData(comment);
            return;
        }

        String beforeTime = "";
        String beforeUid = "";
        String nextTime = "";
        String nextUid= "";

        if(position > 0){
            // 첫번째 아이템이 아닌경우
            beforeUid = comments.get(position-1).getUid();
            beforeTime = getTime(comments.get(position-1).getTimestamp());
        }
        if(position+1 < comments.size()){
            // 다음 아이템이 있는 경우
            nextUid = comments.get(position+1).getUid();
            nextTime = getTime(comments.get(position+1).getTimestamp());
        }

        String time = getTime(comment.getTimestamp());
//        String time = "";

        // 모든 아이템에 프로필 정보를 표시하지 않고 카톡처럼 연속된 시간의 첫번째 아이템에만 프로필정보를 표시하기 위한 조건문
        if(time.equals(beforeTime) && userModel.getUid().equals(beforeUid)){
            // 이전 아이템의 시간과 표시 시간이 같고, 이전 아이템과 작성자가 같은 경우 프로필 정보(사진, 이름)를 표시하지 않는다.
            userModel = null;
        }

        // 모든 아이템에 시간을 표시하지 않고 카톡처럼 연속된 시간의 마지막 아이템에만 시간을 표시하기 위한 조건문
        if(time.equals(nextTime) && comment.getUid().equals(nextUid)){
            // 다음 아이템과 표시 시간과 작성자가 같은 경우 시간을 표시하지 않는다.
            time = "";
        }


        switch (getItemViewType(position)){
            case TYPE_MESSAGE_LEFT:
                ((LeftTextMessageViewHolder)viewHolder).setData(userModel, comment.getMessage(), time, count);
                break;

            case TYPE_MESSAGE_RIGHT:
                ((RightTextMessageViewHolder)viewHolder).setData(comment.getMessage(), time, count);
                break;

            default:
        }
    }

    // TODO: 2019-03-29 뭔가 개선해야할 것 같다... 채팅방에 100명이 있다고 가정하면...
    private int getUnReadUserCount(int position) {
        int count = 0;
        // 안읽은 사람을 카운트하는 방식
        if(lastRead != null){
            for(String key : lastRead.values()){
                if(commentMap.get(key) == null || commentMap.get(key) < position)
                    count++;
            }
        }

        // 읽은 사람을 빼는 방식
//        if(lastRead != null){
//            count = users.size();
//            for(String key : lastRead.values()){
//                if(commentMap.get(key) != null && commentMap.get(key) >= position)
//                    count--;
//            }
//        }

        return count;
    }

    @Override
    public int getItemViewType(int position) {
//        RLog.d("position: " + position);
        ChatModel.Comment comment = comments.get(position);
        String uid = comment.getUid();
        UserModel userModel = users.get(uid);
//        RLog.e(users.toString());
//        RLog.e(uid);
        if(uid == null)
            return TYPE_DATE;
        else{
            if(userModel.getUid().equals(myUid))
                return TYPE_MESSAGE_RIGHT;
            else
                return TYPE_MESSAGE_LEFT;
        }

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
    /** 뷰에서 사용될 함수들 ************************************************************************/
    private String getTime(long unixTime) {
        Date date = new Date(unixTime);
        return simpleDateFormat.format(date);
    }

    private String getTime(Date date) {
        return simpleDateFormat.format(date);
    }

    /**********************************************************************************************/

    @Override
    public void notifyAdapter() {
        notifyDataSetChanged();
    }

    @Override
    public void notifyItemInserted() {
        this.notifyItemInserted(comments.size());
    }

    @Override
    public void notifyItemUpdated(int position) {
        this.notifyItemChanged(position);
    }

    @Override
    public void init(Map<String, UserModel> users, String chatRoomUid, String myUid) {
        this.users = users;
        this.chatRoomUid = chatRoomUid;
        this.myUid = myUid;
        peopleCount = users.size();
    }

    @Override
    public ChatModel.Comment getItem(int position) {
        return comments.get(position);
    }

    @Override
    public List<ChatModel.Comment> getItems() {
        return comments;
    }

    @Override
    public int getItemsCount() {
        return comments.size();
    }

    @Override
    public void addItem(ChatModel.Comment comment) {
        RLog.e("날짜: " + comment.getTimestamp().toString());
        comments.add(comment);
    }

    @Override
    public void addItem(String key, ChatModel.Comment comment) {
        comments.add(comment);
        commentMap.put(key, comments.size()-1);
    }

    @Override
    public int updateItem(String key, ChatModel.Comment c) {
        int targetPosition = commentMap.get(key);
        comments.set(targetPosition, c);
//        notifyItemChanged(targetPosition);
        notifyDataSetChanged();
        return targetPosition;
    }

    @Override /** Comment의 ReadUsers 수정 */
    public int updateReadUsers(String key, ChatModel.Comment c) {
        int targetPosition = commentMap.get(key);
//        comments.get(targetPosition).setReadUsers(c.getReadUsers());
        comments.get(targetPosition).setTimestamp(c.getTimestamp());
//        notifyItemChanged(targetPosition);
        notifyDataSetChanged();
//        if(targetPosition > 0)
//            notifyItemChanged(targetPosition - 1);
        return targetPosition;
    }

    @Override
    public void updateReadUsers(Map<String, String> lastRead) {
        this.lastRead = lastRead;
        notifyDataSetChanged();
    }

    static class MessageViewHodler extends RecyclerView.ViewHolder {

        public MessageViewHodler(View view) {
            super(view);
        }

    }

    /** 상대방이 보낸 메시지 뷰 홀더 */
    static class LeftTextMessageViewHolder extends MessageViewHodler {
        ItemMessageLeftBinding binding;
        public LeftTextMessageViewHolder(ItemMessageLeftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(UserModel userModel, String message, String time, int readUserCount){

            // 메시지 표시
            binding.textMessage.setText(message);
//            binding.textMessage.setBackgroundResource(R.drawable.leftbubble);

            // 메시지 보낸 시간 표시
            if(time.isEmpty())
                binding.time.setVisibility(View.GONE);
            else {
                binding.time.setText(time);
                binding.time.setVisibility(View.VISIBLE);
            }

            // 프로필 정보 표시
            if(userModel == null){
                binding.profileImg.setVisibility(View.GONE);
                binding.name.setVisibility(View.GONE);
            } else{
                binding.profileImg.setVisibility(View.VISIBLE);
                binding.name.setVisibility(View.VISIBLE);

                binding.name.setText(userModel.getUserName());
                BindingUtil.loadProfileImage(binding.profileImg, userModel.getProfileImageUrl());
            }

            // 안읽은 사람 수 표시
            if(readUserCount == 0)
                binding.readCounter.setText("");
            else
                binding.readCounter.setText(String.valueOf(readUserCount));
        }
    }

    /** 내가 보낸 메시지 뷰 홀더 */
    static class RightTextMessageViewHolder extends MessageViewHodler {
        ItemMessageRightBinding binding;

        public RightTextMessageViewHolder(ItemMessageRightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(String message, String time, int readUserCount){

            // 메시지
            binding.textMessage.setText(message);
//            binding.textMessage.setBackgroundResource(R.drawable.rightbubble);

            // 메시지 보낸 시간 표시
            if(time.isEmpty())
                binding.time.setVisibility(View.GONE);
            else {
                binding.time.setText(time);
                binding.time.setVisibility(View.VISIBLE);
            }


            // 안읽은 사람 수 표시
            if(readUserCount == 0)
                binding.readCounter.setText("");
            else
                binding.readCounter.setText(String.valueOf(readUserCount));
        }
    }

    /** 날짜를 표시하는 뷰홀더 */
    static class DateMessageViewHolder extends MessageViewHodler {
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy. MM. dd (E)");
        public TextView textView_date;

        public DateMessageViewHolder(View view) {
            super(view);
            textView_date = (TextView) view.findViewById(R.id.date_TextView);
        }

        public void setData(ChatModel.Comment comment) {
            String time = simpleDateFormat.format(comment.getTimestamp());
            textView_date.setText(time);
        }
    }



}