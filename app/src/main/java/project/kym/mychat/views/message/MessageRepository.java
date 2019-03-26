package project.kym.mychat.views.message;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

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

    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;

    private final DocumentSnapshot.ServerTimestampBehavior behavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE;    // 시간이 늦게 적용되는 문제를 해결해준다.

    public void startListen(String chatRoomUid, final OnEventListener onEventListener){
        final CollectionReference roomRef = firestore.collection("chatrooms").document(chatRoomUid).collection("comments");
        listenerRegistration = roomRef.orderBy("timestamp").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    RLog.e(e.getMessage());
                    return;}
                int exCnt = 0;
                for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                    exCnt++;
                    String key = change.getDocument().getId();
                    ChatModel.Comment comment_origin = change.getDocument().toObject(ChatModel.Comment.class);
                    switch (change.getType()) {
                        case ADDED:
                            if(comment_origin.timestamp == null)
                                comment_origin.timestamp = change.getDocument().getDate("timestamp", behavior);
                            onEventListener.onAdded(key, comment_origin, roomRef);
                            RLog.i("데이터 추가! " + comment_origin.toString());

                            break;
                        case MODIFIED:
                            RLog.i("데이터 변경!! " + comment_origin.toString());
//                            RLog.i(comment_origin.readUsers.toString());
                            onEventListener.onChanged(key, comment_origin);
                            break;
                        case REMOVED:

                            break;
                    }
                }

                RLog.i("반복 횟수: " + exCnt);

            }
        });
    }


    public void stopListen(){
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }



    public interface OnEventListener{
        void onAdded(String key, ChatModel.Comment comment, CollectionReference reference);
        void onChanged(String key, ChatModel.Comment comment);
    }
}
