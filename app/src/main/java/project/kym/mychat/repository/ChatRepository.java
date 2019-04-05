package project.kym.mychat.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import project.kym.mychat.model.ChatModel;
import project.kym.mychat.util.RLog;

public class ChatRepository {
    private static final ChatRepository ourInstance = new ChatRepository();

    public static ChatRepository getInstance() {
        return ourInstance;
    }

    private ChatRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

//    private final DocumentSnapshot.ServerTimestampBehavior behavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE;    // 시간이 늦게 적용되는 문제를 해결해준다.

    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;
    private final DocumentSnapshot.ServerTimestampBehavior behavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE;    // 시간이 늦게 적용되는 문제를 해결해준다.

    public void startListen(String myUid, final OnEventListener listener){
        listenerRegistration = firestore.collection("chatrooms").whereGreaterThanOrEqualTo("users."+myUid, 0).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    RLog.e(e.getMessage());
                    return;
                }

                for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                    String key = change.getDocument().getId();
                    ChatModel chatModel= change.getDocument().toObject(ChatModel.class);
                    chatModel.setRoomUid(key);
                    switch (change.getType()) {
                        case ADDED:
                            if(chatModel.getTimestamp() == null)
                                chatModel.setTimestamp(change.getDocument().getDate("timestamp", behavior));

                            listener.onAdded(key, chatModel);
                            RLog.i("데이터 추가! " + chatModel.toString());
                            break;

                        case MODIFIED:
                            RLog.i("데이터 변경!! " + chatModel.toString());
                            listener.onChanged(key, chatModel);
                            break;

                        case REMOVED:

                            break;
                    }
                }
            }
        });
    }

    public void stopListen(){
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Deprecated
    void initChatListListener(final DataChangeListener listener){
        RLog.i("리스너 초기화");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms");   // 37밀리초 소요
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RLog.i("채팅 리스트 수신");
                List<ChatModel> chatModels = new ArrayList<>();
                for (DataSnapshot item :dataSnapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    chatModel.setUserUid(item.getKey());
                    chatModels.add(chatModel);
                }
                Collections.sort(chatModels, new ChatModelSort());
                listener.onChanged(chatModels);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };  // 8밀리초 소요
    }
    @Deprecated
    public void addListener(){
        RLog.i("리스너 부착");
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.orderByChild("users/"+ myUid).equalTo(true).addValueEventListener(valueEventListener);
    }
    @Deprecated
    public void removeListener(){
        databaseReference.removeEventListener(valueEventListener);
        RLog.i("valueEventListener removed!");
    }

    // ChatModel 시간순서로 정렬하기 위한 클래스
    class ChatModelSort implements Comparator<ChatModel> {

        @Override
        public int compare(ChatModel o1, ChatModel o2) {
            if((long)((Date)o1.getTimestamp()).getTime()> (long)((Date)o2.getTimestamp()).getTime())
                return -1;
            else if((long)((Date)o1.getTimestamp()).getTime() < (long)((Date)o2.getTimestamp()).getTime())
                return 1;
            else
                return 0;
        }
    }

    public interface DataChangeListener{
        void onChanged(List<ChatModel> chatModels);
    }

    public interface OnEventListener{
        void onAdded(String key, ChatModel chatModel);
        void onChanged(String key, ChatModel chatModel);
    }
}
