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
        message.put("uid", comment.getUid());
        message.put("type", comment.getType());
        message.put("message", comment.getMessage());
        message.put("fileName", comment.getFileName());
        message.put("fileUrl", comment.getFileUrl());
        message.put("timestamp", FieldValue.serverTimestamp());
        RLog.e("생성된 메시지: " +message.toString());
        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                WriteBatch batch = firestore.batch();
                batch.set(reference, message, SetOptions.merge());

//                message.put("readUsers", comment.getReadUsers());
                batch.set(reference.collection("comments").document(), message);

                Map<String, Long> users = (Map<String, Long>) document.get("users");
                for( String key : users.keySet() ){
                    if (!comment.getUid().equals(key)) users.put(key, users.get(key)+1);
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

    public interface SendMessageListener{
        void onSuccess();
    }

    public interface AddValueListener{
        void onSuccess(String key);
    }


}
