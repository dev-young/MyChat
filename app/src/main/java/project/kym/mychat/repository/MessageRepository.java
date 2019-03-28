package project.kym.mychat.repository;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import project.kym.mychat.model.ChatModel;
import project.kym.mychat.util.RLog;

public class MessageRepository {
    private static final MessageRepository ourInstance = new MessageRepository();

    public static MessageRepository getInstance() {
        return ourInstance;
    }

    private MessageRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    private ChatModel currentChat;
    private Map<String, ChatModel.Comment> commentMap = new HashMap<>();

    private FirebaseFirestore firestore;
    private ListenerRegistration messageLR;
    private ListenerRegistration lastReadLR;

    private final DocumentSnapshot.ServerTimestampBehavior behavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE;    // 시간이 늦게 적용되는 문제를 해결해준다.

    /**실시간 채팅 목록과 채팅방 유저가 읽은 마지막 메시지를 실시간으로 수신한다. */
    public void startListen(String chatRoomUid, OnEventListener<ChatModel.Comment> onEventListener){
        CollectionReference roomRef = firestore.collection("chatrooms").document(chatRoomUid).collection("comments");
        messageLR = roomRef.orderBy("timestamp").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    RLog.e(e.getMessage());
                    return;
                }
                int exCnt = 0;
                for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                    exCnt++;
                    String key = change.getDocument().getId();
                    ChatModel.Comment comment_origin = change.getDocument().toObject(ChatModel.Comment.class);
                    switch (change.getType()) {
                        case ADDED:
                            if(comment_origin.getTimestamp() == null)
                                comment_origin.setTimestamp(change.getDocument().getDate("timestamp", behavior));
                            commentMap.put(key, comment_origin);
                            onEventListener.onAdded(key, comment_origin);
//                            String source = change.getDocument() != null && change.getDocument().getMetadata().hasPendingWrites()? "Local" : "Server";
//                            RLog.d(source + " 데이터 추가! " + comment_origin.toString());

                            break;
                        case MODIFIED:
                            RLog.d("데이터 변경!! " + comment_origin.toString());
                            commentMap.put(key, comment_origin);
//                            RLog.d(comment_origin.readUsers.toString());
//                            onEventListener.onChanged(key, comment_origin);
                            break;
                        case REMOVED:
                            commentMap.remove(key);
                            break;
                    }
                }

//                RLog.d("반복 횟수: " + exCnt);

            }
        });
    }

    public void startListenLastRead(String chatRoomUid, OnEventListener<Map<String, String> > listener){
        lastReadLR = firestore.collection("chatrooms").document(chatRoomUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    RLog.e(e.getMessage());
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    RLog.i("Current data: " + snapshot.getData());
                    currentChat = snapshot.toObject(ChatModel.class);
                    currentChat.setRoomUid(snapshot.getId());
                    listener.onChanged(snapshot.getId(), currentChat.getLastRead());
                } else {
                    RLog.e("Current data: null");
                }
            }
        });
    }

    public ChatModel getCurrentChat() {
        return currentChat;
    }

    public void stopListen(){
        if (messageLR != null) {
            messageLR.remove();
            messageLR = null;
        }

        if (lastReadLR != null) {
            lastReadLR.remove();
            lastReadLR = null;
        }
    }



    public interface OnEventListener<T>{
        void onAdded(String key, T result);
        void onChanged(String key, T result);
    }
}
