package project.kym.mychat.util;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;


import java.util.HashMap;
import java.util.Map;

import project.kym.mychat.model.ChatModel;

public class ChatUtil {

    public static void makeChatRoom(ChatModel chatModel, final AddValueListener addValueListener){
        FirebaseFirestore.getInstance().collection("chatrooms").add(chatModel)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        addValueListener.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void sendMessage(String chatRoomUid, final ChatModel.Comment comment, final SendMessageListener sendMessageListener) {
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final DocumentReference reference = firestore.collection("chatrooms").document(chatRoomUid);
        final Map<String, Object> message = new HashMap<>();
        message.put("uid", comment.uid);
        message.put("type", comment.type);
        message.put("message", comment.message);
        message.put("fileName", comment.fileName);
        message.put("fileUrl", comment.fileUrl);
        message.put("timestamp", FieldValue.serverTimestamp());
        RLog.e("생성된 메시지: " +message.toString());
        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                WriteBatch batch = firestore.batch();
                batch.set(reference, message, SetOptions.merge());

                message.put("readUsers", comment.readUsers);
                batch.set(reference.collection("comments").document(), message);

                Map<String, Long> users = (Map<String, Long>) document.get("users");
                for( String key : users.keySet() ){
                    if (!comment.uid.equals(key)) users.put(key, users.get(key)+1);
                }
                document.getReference().update("users", users);

                batch.commit()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                RLog.i("성공적으로 수행!");
                                sendMessageListener.onSuccess();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                RLog.e(e.getMessage());
                            }
                        });
            }
        });



    }

    @Deprecated
    public static String makeChatRoomInRealTimeDB(ChatModel chatModel, final SetValueListener setValueListener){
        DatabaseReference pushedRef = FirebaseDatabase.getInstance().getReference().child("chatrooms").push();
        String chatRoomUid = pushedRef.getKey();
        pushedRef.setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setValueListener.onSuccess();
            }
        });
        return chatRoomUid;
    }

    @Deprecated
    public static void sendMessageInRealTimeDB(String chatRoomUid, final ChatModel.Comment comment, final SendMessageListener sendMessageListener) {
        RLog.i("chatRoomUid = " + chatRoomUid);
        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                sendMessageListener.onSuccess();
            }
        });
    }

    public interface SetValueListener{
        void onSuccess();
    }

    public interface SendMessageListener{
        void onSuccess();
    }

    public interface AddValueListener{
        void onSuccess(String key);
    }


}
