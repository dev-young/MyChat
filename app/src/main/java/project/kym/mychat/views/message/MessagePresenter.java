package project.kym.mychat.views.message;

import android.content.Intent;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;
import project.kym.mychat.model.ChatModel;
import project.kym.mychat.model.UserModel;
import project.kym.mychat.repository.MyAccount;
import project.kym.mychat.util.ChatUtil;
import project.kym.mychat.util.PushUtil;
import project.kym.mychat.util.RLog;

/**채팅방이 있을경우 해당 채팅방의 채팅 목록을 불러오고
 * 채팅방이 없을경우 코멘트를 전송시 받아온 유저의 uid 값을 바탕으로 채팅방을 생성한다.
 * */
public class MessagePresenter implements MessageContract{
    View view;
    private MessageRecyclerViewAdapterContract.View adapterView;
    private MessageRecyclerViewAdapterContract.Model adapterModel;

    private String title;
    private boolean isGroupMessage;
    private Map<String, UserModel> users = new HashMap<>();
    Map<String, Object> readUsersMap = new HashMap<>();
    String chatRoomUid;
    String myUid;
    private Map<String, Long> roomUsers; // 본인을 제외한 나머지 유저들의 key 맵 <유저들의 key, 유효 여부>
    long lastTimestamp; // 어뎁터가 만들어진 시점의 채팅방 lastTimestamp
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy. MM. dd (E)");
    private Date currentAddedDate = new Date();
    private String lastAddedDate;

    FirebaseFirestore firestore;

    public MessagePresenter(Object view) {
        this.view = (View) view;
        lastAddedDate = simpleDateFormat.format(new Date(10000000));
        firestore = FirebaseFirestore.getInstance();
    }

    public void setAdapter(MessageRecyclerViewAdapter adapter){
        adapterView = adapter;
        adapterModel = adapter;
    }

    @Override
    public void init(Intent intent) {
        //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID
        MyAccount.getInstance().getUserModel(new MyAccount.OnCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, UserModel userModel) {
                if(isSuccess){
                    myUid = userModel.getUid();
                    // 그룹 채팅방 여부
                    isGroupMessage = intent.getBooleanExtra("isGroupMessage", false);
                    // 유저들을 선택하여 새로 채팅방을 만들려고 시도할 경우 값이 넘어온다.
                    roomUsers = (Map<String, Long>) intent.getSerializableExtra("destinationUids");
                    // 채팅방 uid
                    chatRoomUid = intent.getStringExtra("chatRoomUid");
                    //채팅방 이름
                    title = intent.getStringExtra("title");

                    if(chatRoomUid == null && !isGroupMessage){
                        // 유저 목록에서 유저를 선택한 경우
                        findChatRoom(roomUsers.keySet().iterator().next());

                    } else if(chatRoomUid != null){
                        // 채팅방을 클릭해 들어온 경우
                        RLog.i("채팅방을 클릭해 들어온 경우");
                        view.showProgress(true);
//            getUsersAndLoadMessagesFromRealTimeDB(chatRoomUid);
                        getUsersAndLoadMessages(chatRoomUid);
                    } else{
                        RLog.i("그룹챗을 새로 만들어 들어온 경우");
                        // 그룹챗을 새로 만들어 들어온 경우, send 버튼을 클릭할 때 방을 생성하고 어뎁터를 만들고 데이터를 불러온다.
                    }
                } else {
                    RLog.e("채팅방 초기화중 에러 발생");
                }
            }
        });
    }

    @Override
    public void init(Fragment fragment) {
        //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID
        MyAccount.getInstance().getUserModel(new MyAccount.OnCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, UserModel userModel) {
                if(isSuccess){
                    myUid = userModel.getUid();
                    // 그룹 채팅방 여부
                    isGroupMessage = fragment.getArguments().getBoolean("isGroupMessage", false);
                    // 유저들을 선택하여 새로 채팅방을 만들려고 시도할 경우 값이 넘어온다.
                    roomUsers = (Map<String, Long>) fragment.getArguments().getSerializable("destinationUids");
                    // 채팅방 uid
                    chatRoomUid = fragment.getArguments().getString("chatRoomUid");
                    //채팅방 이름
                    title = fragment.getArguments().getString("title");

                    if(chatRoomUid == null && !isGroupMessage){
                        // 유저 목록에서 유저를 선택한 경우
                        findChatRoom(roomUsers.keySet().iterator().next());

                    } else if(chatRoomUid != null){
                        // 채팅방을 클릭해 들어온 경우
                        RLog.i("채팅방을 클릭해 들어온 경우");
                        view.showProgress(true);
                        getUsersAndLoadMessages(chatRoomUid);
                    } else{
                        // 그룹챗을 새로 만들어 들어온 경우, send 버튼을 클릭할 때 방을 생성하고 어뎁터를 만들고 데이터를 불러온다.
                    }
                }
            }
        });
    }

    @Override
    public void loadMessages() {
        MessageRepository.getInstance().startListen(chatRoomUid, new MessageRepository.OnEventListener() {
            @Override
            public void onAdded(String key, ChatModel.Comment comment_origin, CollectionReference reference) {
                currentAddedDate = comment_origin.getTimestamp();
                String addedDate = simpleDateFormat.format(currentAddedDate);
                if(!lastAddedDate.equals(addedDate)){
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.setTimestamp(comment_origin.getTimestamp());
                    lastAddedDate = addedDate;
                    adapterModel.addItem(comment);
                }

                adapterModel.addItem(key, comment_origin);

                // 추가된 comment에  내 uid가 없으면 readUsers에 put한다.
                if (!isContainMyUid(comment_origin)) {
                    RLog.e(comment_origin.getUid() + " 는 내uid를 가지고있지 않다.");
                    ChatModel.Comment comment_motify = comment_origin;
//                    comment_motify.getReadUsers().put(myUid, true);
                    readUsersMap.put(key, comment_motify);
                } else {
                    // 읽은 유저 목록에 내 uid 가 있으면 서버에 요청 없이 그냥 수정한다.
                }

                adapterView.notifyItemInserted();
//                adapterView.notifyAdapter();  // 이걸 하면 Inserted 애니메이션이 사라진다.
                adapterView.notifyItemUpdated(adapterModel.getItemsCount()-2);
                view.scrollToPosition(adapterModel.getItemsCount() -1);

                if (readUsersMap.isEmpty()) {
                    RLog.e("빈통!!");
                } else
                    RLog.e("여기!");
                // 채팅방에 들어온 시점의 시간과 같거나 그 이후 추가된 comment 이면 readUsersMap을 통해 서버의 comments를 업데이트한다.
                if(isLastMessage((comment_origin)) && !readUsersMap.isEmpty()){
                    RLog.i("서버에 readUsers 업데이트 시작! " + readUsersMap.toString());
                    WriteBatch batch = firestore.batch();
                    for(String k : readUsersMap.keySet()){
                        Object o = readUsersMap.get(k);
                        if(o == null)
                            RLog.e();
                        else
                            RLog.i(o.toString());
                        batch.set(reference.document(k), readUsersMap.get(k));
                    }
                    resetUnreadMessageCounter();
                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                RLog.i("readUsers 업데이트 성공!");
                                readUsersMap.clear();
                            }
                            else{
                                RLog.e("readUsers 업데이트 실패!  " + task.getException().getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onChanged(String key, ChatModel.Comment comment) {
//                if(myUid.equals(comment.getUid()) && comment.getReadUsers().size() == 1)
//                    return;

                adapterModel.updateReadUsers(key, comment);
            }
        });
    }

    private void resetUnreadMessageCounter() {
        // 읽지않은 메시지 수 0으로 초기화
        firestore.collection("chatrooms").document(chatRoomUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                Map<String, Long> users = (Map<String, Long>) document.get("users");
                users.put(myUid, 0L);
                document.getReference().update("users", users);
            }
        });
    }

    private boolean isContainMyUid(ChatModel.Comment comment) {
//        return comment.getReadUsers().containsKey(myUid);
        return false;
    }

    private boolean isLastMessage(ChatModel.Comment comment) {
//        RLog.i("O : " + lastTimestamp);
//        RLog.e("N : " + comment.timestamp.getTime());
        boolean b = comment.getTimestamp().getTime() + 750 >= lastTimestamp;
        if(b)
            view.showProgress(false);
        return b;
    }

    @Override
    public void onSendButtonClicked(String message) {
        view.clearEditText();
        final ChatModel.Comment comment = makeComment(message);

        if (chatRoomUid == null) {
            makeChatRoomAndSendMessage(comment);
        } else {
            sendMessage(chatRoomUid, comment);
        }
    }

    @Override
    public void sendMessage(String chatRoomUid, final ChatModel.Comment comment) {
        RLog.i("chatRoomUid = " + chatRoomUid);

        ChatUtil.sendMessage(chatRoomUid, comment, new ChatUtil.SendMessageListener() {
            @Override
            public void onSuccess() {
                ArrayList<String> tokens = new ArrayList<>();
                for(UserModel item : users.values()){
                    if(!item.getUid().equals(myUid) && (roomUsers.get(item.getUid()) > -1)){
                        tokens.add(item.getPushToken());
                    }
                }
                String chatTitle;
                if(isGroupMessage)
                    chatTitle = title;
                else
                    chatTitle = MyAccount.getInstance().getUserModel().getUserName();
                PushUtil.sendFCM_Message(tokens,
                        chatTitle,
                        comment.getMessage(),
                        users.get(myUid).getProfileImageUrl(),
                        isGroupMessage);


//                updateLastTimestamp();
            }
        });

    }

    @Override /**가장 마지막에 추가된 comments의 timestamp 값을 lastTimestamp에 저장한다. (파이어베이스 내부에서 동작)*/
    public void updateLastTimestamp() {
        //????
    }

    @Override /** 첫번째로 표시되는 뷰의 날짜와 상단에 표시될 날짜를 동기화 시킨다. */
    public void listenFirstVisiblePosition(int firstCompletelyVisibleItemPosition) {
        if(firstCompletelyVisibleItemPosition > -1){
            Date date = adapterModel.getItem(firstCompletelyVisibleItemPosition).getTimestamp();
            if(date != null){
                String time = simpleDateFormat.format(date);
                view.setDateTextView(time);
            }
        } else RLog.e(firstCompletelyVisibleItemPosition +"");
    }

    @Override
    public void onResume() {
        PushUtil.currentRoomUid = chatRoomUid;
        if(PushUtil.currentRoomUid == null)
            PushUtil.currentRoomUid = "";
        if(PushUtil.currentRoomUid.equals(PushUtil.lastNotifiedRoomUid)){
            // 현재 방과 같은 곳의 notification이 있으면 제거한다.
            view.removeNotification();
        }
    }

    @Override
    public void onStop() {

        MessageRepository.getInstance().stopListen();

        // 실제로 보여지고 있던 프레젠터가 사라질 경우만  PushUtil.currentRoomUid 값을 초기화 한다.
        // 노티를 클릭해서 열린 창창
        if(chatRoomUid != null && chatRoomUid.equals(PushUtil.currentRoomUid)){
            PushUtil.currentRoomUid = "";
            RLog.i("PushUtil.currentRoomUid clear");
        }

    }


    /**********************************************************************************************/
    private ChatModel.Comment makeComment(String message){
        ChatModel.Comment comment = new ChatModel.Comment();
        comment.setUid(myUid);
        comment.setMessage(message);
        Map<String,Object> readUsers = new HashMap<>();  //읽은 유저 목록
        readUsers.put(myUid, true);
//        comment.setReadUsers(readUsers);
        return comment;
    }


    /** 채팅방을 만들고 메시지를 전송한다.*/
    private void makeChatRoomAndSendMessage(final ChatModel.Comment comment){
        view.showProgress(true);

        RLog.i("chatRoomUid == null");
        view.setSendButtonEnabled(false);
        final ChatModel chatModel = new ChatModel();
        chatModel.getUsers().put(myUid, 0);
        for(String s : roomUsers.keySet())
            chatModel.getUsers().put(s, 0);
        chatModel.setGroup(isGroupMessage);
        for(String key : chatModel.getUsers().keySet()){
            firestore.collection("users").document(key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    UserModel userModel = snapshot.toObject(UserModel.class);
                    users.put(snapshot.getId(), userModel);
                    if(title == null){
                        // 타이틀값이 없을 경우 유저 이름들로 타이틀 생성
                        if(chatModel.getTitle() == null)
                            chatModel.setTitle(userModel.getUserName());
                        else{
                            String title = chatModel.getTitle();
                            title += ", " + userModel.getUserName();
                            chatModel.setTitle(title);
                        }
                    } else {
                        //타이틀이 있을경우
                        chatModel.setTitle(title);
                    }
                    if(users.size() == chatModel.getUsers().size()){
                        ChatUtil.makeChatRoom(chatModel, new ChatUtil.AddValueListener() {
                            @Override
                            public void onSuccess(String key) {
                                chatRoomUid = key;
                                PushUtil.currentRoomUid = key;
                                getUsersAndLoadMessages(chatRoomUid);
                                if(!users.isEmpty())
                                    sendMessage(chatRoomUid, comment);

                                view.setSendButtonEnabled(true);
                                view.showProgress(false);
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    view.showProgress(false);
                }
            });
        }
    }

    /** 채팅목록이 아니라 유저 리스트를 클릭하여 채팅방에 들어온 경우 해당 유저와 대화하던 방이 있는지 확인한다.
     * 방이 있을 경우 채팅 기록을 가져온다. */
    void findChatRoom(final String destinationUid) {
        RLog.i();
        view.showProgress(true);
        firestore.collection("chatrooms").whereGreaterThanOrEqualTo("users."+myUid, 0).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot item : queryDocumentSnapshots) {
                    ChatModel chatModel = item.toObject(ChatModel.class);
                    if (chatModel.getUsers().containsKey(destinationUid) && !chatModel.isGroup()) {
                        chatRoomUid = item.getId();
                        PushUtil.currentRoomUid = chatRoomUid;
                        break;
                    }
                }
                if(chatRoomUid != null){
                    // 대화하던 방이 있는경우.
                    RLog.i("채팅방 있음");
                    getUsersAndLoadMessages(chatRoomUid);
                } else{
                    RLog.e("채팅방 없음");
                    view.showProgress(false);
                }
            }
        });

    }

    /** chatRoomUid 를 바탕으로 채팅방의 lastTimestamp를 가져온다. */
    private void getLastTimestamp(String chatRoomUid) {
        RLog.i(chatRoomUid);
        firestore.collection("chatrooms").document(chatRoomUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ChatModel chatModel = documentSnapshot.toObject(ChatModel.class);
                RLog.e(documentSnapshot.toString());
                lastTimestamp = chatModel.getTimestamp().getTime();
                RLog.i(chatModel.getTimestamp().toString() + "  UnixTime: " + lastTimestamp);
            }
        });

    }


    /** 파이어 스토어 사용 */
    private void getUsersAndLoadMessages(final String chatRoomUid) {
        getLastTimestamp(chatRoomUid);
        if(users.isEmpty()){
            firestore.collection("chatrooms").document(chatRoomUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    roomUsers = (Map<String, Long>) documentSnapshot.get("users");
                    for(String key : roomUsers.keySet()){
                        firestore.collection("users").document(key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {
                                users.put(snapshot.getId(), snapshot.toObject(UserModel.class));
                                if(users.size() == roomUsers.size()){
                                    RLog.i("유저 수: " + users.size());
//                                RLog.i("유저 : " + users.toString());
                                    adapterModel.init(users, chatRoomUid, myUid);
                                    loadMessages();
                                }
                            }
                        });
                    }

                }
            });
        } else {
            RLog.i("유저 수: " + users.size());
            adapterModel.init(users, chatRoomUid, myUid);
            loadMessages();
        }





    }
}
