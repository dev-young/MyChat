package project.kym.mychat.repository;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import project.kym.mychat.SimpleCallback;
import project.kym.mychat.database.AppDatabase;
import project.kym.mychat.database.MessageDao;
import project.kym.mychat.model.ChatModel;
import project.kym.mychat.util.RLog;

public class MessageRepository {
    private static final MessageRepository ourInstance = new MessageRepository();

    public static MessageRepository getInstance() {
        return ourInstance;
    }

    private MessageRepository() {
        firestore = FirebaseFirestore.getInstance();
        messageDao = AppDatabase.getInstance(null).messageDao();
    }

    private MessageDao messageDao;

    private ChatModel currentChat;
    private Map<String, ChatModel.Comment> commentMap = new HashMap<>();
    private List<ChatModel.Comment> commentList;

    private FirebaseFirestore firestore;
    private ListenerRegistration messageLR;
    private ListenerRegistration lastReadLR;

    private final DocumentSnapshot.ServerTimestampBehavior behavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE;    // 시간이 늦게 적용되는 문제를 해결해준다.

    public void loadFromDB(String roomUid, SimpleCallback<List<ChatModel.Comment>> callback){
        RLog.i("요청한 챗방 uid: " + roomUid);
        Observable.just(roomUid).subscribeOn(Schedulers.io()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                commentList = messageDao.loadAllFrom(roomUid);
                callback.onComplete(commentList);
                RLog.i("loadFromDB");
            }
        });
    }

    public void insert(ChatModel.Comment comment){
        Observable.just(comment).subscribeOn(Schedulers.io()).subscribe(new Consumer<ChatModel.Comment>() {
            @Override
            public void accept(ChatModel.Comment c) throws Exception {
                long rowId = messageDao.insert(c);
                RLog.i("insert" + rowId);
            }
        });
    }

    public void insert(List<ChatModel.Comment> comments){
        RLog.i();
        Observable.just(comments).subscribeOn(Schedulers.io()).subscribe(new Consumer<List<ChatModel.Comment>>() {
            @Override
            public void accept(List<ChatModel.Comment> list) throws Exception {
                RLog.i("inserted row count: " + messageDao.insertAll(list).length);
            }
        });
    }

    public void clear(){
        RLog.i();
        Observable.just("").subscribeOn(Schedulers.io()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                messageDao.deleteAll();
            }
        });
    }

    /**실시간 채팅 목록과 채팅방 유저가 읽은 마지막 메시지를 실시간으로 수신한다. */
    public void startListen(String chatRoomUid, Date after, OnEventListener<ChatModel.Comment> onEventListener){
        CollectionReference roomRef = firestore.collection("chatrooms").document(chatRoomUid).collection("comments");
        Query query;
        if(after == null)
            query = roomRef.orderBy("timestamp");
        else
            query = roomRef.whereGreaterThan("timestamp", after).orderBy("timestamp");
        messageLR = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    RLog.e(e.getMessage());
                    return;
                }


                List<ChatModel.Comment> newComments = new ArrayList<>();
                for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                    String key = change.getDocument().getId();
                    ChatModel.Comment comment_origin = change.getDocument().toObject(ChatModel.Comment.class);
                    comment_origin.setUid(key);
                    comment_origin.setRoomUid(chatRoomUid);
                    switch (change.getType()) {
                        case ADDED:
                            if(comment_origin.getTimestamp() == null)
                                comment_origin.setTimestamp(change.getDocument().getDate("timestamp", behavior));

                            String source = change.getDocument() != null && change.getDocument().getMetadata().hasPendingWrites()? "Local" : "Server";
                            RLog.d(source + " 데이터 추가! " + comment_origin.toString() + comment_origin.getTimestamp().getTime());

                            commentMap.put(key, comment_origin);
                            commentList.add(comment_origin);
                            newComments.add(comment_origin);
                            onEventListener.onAdded(key, comment_origin);

                            if(comment_origin.getTimestamp() == null){
                                RLog.e("comment_origin.getTimestamp() == null");
                            }



                            break;
                        case MODIFIED:
                            String source2 = change.getDocument() != null && change.getDocument().getMetadata().hasPendingWrites()? "Local" : "Server";
                            RLog.d(source2 + " 데이터 변경! " + comment_origin.toString() + comment_origin.getTimestamp().getTime());
                            commentMap.put(key, comment_origin);
                            insert(comment_origin);
//                            RLog.d(comment_origin.readUsers.toString());
                            onEventListener.onChanged(key, comment_origin);
                            break;

                        case REMOVED:
                            commentMap.remove(key);
                            break;
                    }
                }
                insert(newComments);
                RLog.d("반복 횟수: " + queryDocumentSnapshots.getDocumentChanges().size());

            }
        });
    }

    public void startListenLastRead(String chatRoomUid, OnEventListener<Map<String, String> > listener){
        lastReadLR = firestore.collection("chatrooms").document(chatRoomUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override   // TODO: 2019-04-01 채팅방을 모니터링중인 사람이 5명이면 onEvent가 10번 호출된다.. 고쳐야한다..!
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

    public void setCurrentChat(ChatModel currentChat) {
        this.currentChat = currentChat;
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
