    package project.kym.mychat.views.message;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.kym.mychat.R;
import project.kym.mychat.databinding.ItemMessageBinding;
import project.kym.mychat.databinding.ItemMessageLeftBinding;
import project.kym.mychat.databinding.ItemMessageRightBinding;
import project.kym.mychat.model.ChatModel;
import project.kym.mychat.model.UserModel;
import project.kym.mychat.util.BindingUtil;
import project.kym.mychat.util.RLog;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.MessageViewHodler> implements MessageRecyclerViewAdapterContract.Model, MessageRecyclerViewAdapterContract.View{
    private final int TYPE_MESSAGE_LEFT = -1;
    private final int TYPE_MESSAGE_RIGHT = -2;
    private final int TYPE_MESSAGE = 1;
    private final int TYPE_DATE = 0;

    List<ChatModel.Comment> comments = new ArrayList<>();
    Map<String, Integer> commentMap = new HashMap<>();    //리스트의 인덱스와 키값을 맵으로 저장 <Comment의 키값, 리스트에서 Comment의 인덱스>
    Map<String, UserModel> users;
    Map<String, String> lastRead = new HashMap<>();
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
            case TYPE_DATE:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_divider,parent,false);
                hodler = new DateMessageViewHolder(view);
                break;

            default:
                ItemMessageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_message, parent, false);
                hodler = new TextMessageViewHolder(binding);
        }

        return hodler;

    }

    @Override
    public void onBindViewHolder(MessageViewHodler viewHolder, int position) {
        int itemType = getItemViewType(position);
        ChatModel.Comment comment = comments.get(position);
        String uid = comment.getUserUid();
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
            beforeUid = comments.get(position-1).getUserUid();
            beforeTime = getTime(comments.get(position-1).getTimestamp());
        }
        if(position+1 < comments.size()){
            // 다음 아이템이 있는 경우
            nextUid = comments.get(position+1).getUserUid();
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
        if(time.equals(nextTime) && comment.getUserUid().equals(nextUid)){
            // 다음 아이템과 표시 시간과 작성자가 같은 경우 시간을 표시하지 않는다.
            time = "";
        }

        boolean self = isMyMessage(position);
        viewHolder.setData(self, userModel, comment, time, count);
//        switch (getItemViewType(position)){
//            case TYPE_MESSAGE_LEFT:
//                ((LeftTextMessageViewHolder)viewHolder).setData(userModel, comment, time, count);
//                break;
//
//            case TYPE_MESSAGE_RIGHT:
//                ((RightTextMessageViewHolder)viewHolder).setData(comment, time, count);
//                break;
//
//            default:
//        }
    }

    // TODO: 2019-03-29 뭔가 개선해야할 것 같다... 채팅방에 100명이 있다고 가정하면...
    private int getUnReadUserCount(int position) {
        int count = 0;
        // 안읽은 사람을 카운트하는 방식
        if(lastRead != null){
            for(String userUid : lastRead.keySet()){
                if(!userUid.equals(myUid)){
                    String commentUid = lastRead.get(userUid);
                    if(commentMap.get(commentUid) == null || commentMap.get(commentUid) < position)
                        count++;
                }
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
        String uid = comment.getUserUid();
//        RLog.e(users.toString());
//        RLog.e(uid);
        if(uid == null)
            return TYPE_DATE;
        else{
            return TYPE_MESSAGE;

//            if(uid.equals(myUid))
//                return TYPE_MESSAGE_RIGHT;
//            else
//                return TYPE_MESSAGE_LEFT;
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

    private boolean isMyMessage(int position){
        ChatModel.Comment comment = comments.get(position);
        String userUid = comment.getUserUid();
        if(userUid != null && myUid.equals(userUid))
            return true;
        return false;
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
        this.notifyItemChanged(position, 0);
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
//        notifyDataSetChanged();
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

    // TODO: 2019-04-05 상대방의 채팅이 추가된 경우 서버로 부터 읽어오는 속도가 늦어서 애니메이션 효과가 짤린다  딜레이를 주면 해결될 것 같다 아니면..그냥 added 애니메이션을 없이하면.. 카톡처럼ㅎㅎ
    @Override
    public void updateReadUsers(Map<String, String> lastRead) {
        if(!lastRead.values().toString().equals(this.lastRead.values().toString())){
            this.lastRead = lastRead;
            notifyDataSetChanged();
        }
    }

    static class MessageViewHodler extends RecyclerView.ViewHolder {

        public MessageViewHodler(View view) {
            super(view);
        }
        public void setData(boolean self, UserModel userModel, ChatModel.Comment comment, String time, int readUserCount){
        }
    }

    /** 상대방이 보낸 메시지 뷰 홀더 */
    static class LeftTextMessageViewHolder extends MessageViewHodler {
        ItemMessageLeftBinding binding;
        public LeftTextMessageViewHolder(ItemMessageLeftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(UserModel userModel, ChatModel.Comment comment, String time, int readUserCount){

            // 메시지 표시
            if(comment.getType() == ChatModel.Comment.TYPE_TEXT){
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.photo.setVisibility(View.GONE);
                binding.textMessage.setText(comment.getMessage());
//            binding.textMessage.setBackgroundResource(R.drawable.rightbubble);
            } else if(comment.getType() == ChatModel.Comment.TYPE_PHOTO){
                binding.textMessage.setVisibility(View.GONE);
                binding.photo.setVisibility(View.VISIBLE);
                Object url = comment.getFileUrl();
                Glide.with(binding.photo.getContext()).load(url).into(binding.photo);

            }

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

        public void setData(ChatModel.Comment comment, String time, int readUserCount){

            // 메시지
            if(comment.getType() == ChatModel.Comment.TYPE_TEXT){
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.photo.setVisibility(View.GONE);
                binding.textMessage.setText(comment.getMessage());
//            binding.textMessage.setBackgroundResource(R.drawable.rightbubble);
            } else if(comment.getType() == ChatModel.Comment.TYPE_PHOTO){
                binding.textMessage.setVisibility(View.GONE);
                binding.photo.setVisibility(View.VISIBLE);
                String filePath = comment.getLocalFilePath();
                Object url;
                if(filePath != null)
                    url = Uri.fromFile(new File(filePath));
                else
                    url = comment.getFileUrl();
                Glide.with(binding.photo.getContext()).load(url).into(binding.photo);
            }


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

    static class TextMessageViewHolder extends MessageViewHodler {
        ItemMessageBinding binding;

        public TextMessageViewHolder(ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void setData(boolean self, UserModel userModel, ChatModel.Comment comment, String time, int readUserCount) {
            initLayout(self);

            if(!self){
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
            }

            // 메시지
            if(comment.getType() == ChatModel.Comment.TYPE_TEXT){
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.photo.setVisibility(View.GONE);
                binding.textMessage.setText(comment.getMessage());
            } else if(comment.getType() == ChatModel.Comment.TYPE_PHOTO){
                binding.textMessage.setVisibility(View.GONE);
                binding.photo.setVisibility(View.VISIBLE);
                String filePath = comment.getLocalFilePath();
                Object url = null;
                if(filePath != null){
                    File file = new File(filePath);
                    if(file.exists())
                        url = Uri.fromFile(file);
                }

                if(url == null)
                    url = comment.getFileUrl();
                Glide.with(binding.photo.getContext()).asBitmap().load(url).into(binding.photo);
            }

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

        private void initLayout(boolean self) {
            if(self){
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.dateLayout.getLayoutParams();
                layoutParams.removeRule(RelativeLayout.END_OF);
                binding.dateLayout.setLayoutParams(layoutParams);
                layoutParams = (RelativeLayout.LayoutParams) binding.messageFrame.getLayoutParams();
                layoutParams.removeRule(RelativeLayout.END_OF);
                layoutParams.addRule(RelativeLayout.END_OF, binding.dateLayout.getId());
                binding.messageFrame.setLayoutParams(layoutParams);
                binding.rootLayout.setGravity(Gravity.END);
                binding.dateLayout.setGravity(Gravity.END);
                binding.textMessage.setBackgroundResource(R.drawable.chat_bubble_right);
                binding.profileImg.setVisibility(View.GONE);
                binding.name.setVisibility(View.GONE);

            } else {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.dateLayout.getLayoutParams();
                layoutParams.removeRule(RelativeLayout.END_OF);
                layoutParams.addRule(RelativeLayout.END_OF, binding.messageFrame.getId());
                binding.dateLayout.setLayoutParams(layoutParams);
                layoutParams = (RelativeLayout.LayoutParams) binding.messageFrame.getLayoutParams();
                layoutParams.removeRule(RelativeLayout.END_OF);
                binding.messageFrame.setLayoutParams(layoutParams);
                binding.rootLayout.setGravity(Gravity.START);
                binding.dateLayout.setGravity(Gravity.START);
                binding.textMessage.setBackgroundResource(R.drawable.chat_bubble_left);
            }
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