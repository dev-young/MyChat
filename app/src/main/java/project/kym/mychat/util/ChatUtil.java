package project.kym.mychat.util;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;


import java.util.Date;
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

    /** 서버에 Comment 추가 및 lastRead 갱싱 */
    public static void sendMessage(String chatRoomUid, final ChatModel.Comment comment, final SendMessageListener sendMessageListener) {
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final DocumentReference reference = firestore.collection("chatrooms").document(chatRoomUid);
        RLog.e("생성된 메시지: " +comment.toString());
        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                WriteBatch batch = firestore.batch();
                comment.setTimestamp(new Date());
                batch.set(reference, comment, SetOptions.merge());
                batch.set(reference.collection("comments").document(), comment);

                Map<String, Integer> users = (Map<String, Integer>) document.get("users");
                for( String key : users.keySet() ){
                    if (!comment.getUid().equals(key)) users.put(key, users.get(key)+1);
                }
                document.getReference().update("users", users);

                batch.commit()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                RLog.d("성공적으로 수행!");
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
    /** 서버에 Comment 추가 및 lastRead 갱싱 */
    public static void sendMessage(ChatModel chatModel, final ChatModel.Comment comment, final SendMessageListener sendMessageListener) {
//        RLog.d("생성된 메시지: " +comment.toString());
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final DocumentReference reference = firestore.collection("chatrooms").document(chatModel.getRoomUid());
        reference.collection("comments").add(comment).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.getException() != null){
                    task.getException().printStackTrace();
                    return;
                }

                String addedUid = task.getResult().getId();

                if (task.isSuccessful()) {
                    Map<String, Integer> users = chatModel.getUsers();
                    for( String key : users.keySet() ){
                        if (!comment.getUid().equals(key))
                            users.put(key, users.get(key)+1);   // 안읽은 메시지 카운팅
                    }
                    WriteBatch batch = firestore.batch();
//                    comment.setTimestamp(new Date());
                    batch.set(reference, comment, SetOptions.merge());
                    batch.update(reference, "users", users);
                    batch.update(reference, "lastRead."+comment.getUid(), addedUid);
                    batch.commit()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    RLog.d("성공적으로 수행!");
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
            }
        });

    }

    public static void updateLastRead(String chatRoomUid, String userUid, String readedUid, FriestoreListener.Complete<Void> listener){
        FirebaseFirestore.getInstance().collection("chatrooms").document(chatRoomUid).update("lastRead." + userUid, readedUid).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.getException() != null){
                    task.getException().printStackTrace();
                    listener.onCompelete(false, null);
                    return;
                }

                if (task.isSuccessful()) {
                    RLog.i("updateLastRead 성공");
                    listener.onCompelete(true, null);
                }  else {
                    RLog.i("updateLastRead 실패");
                    listener.onCompelete(false, null);
                }


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
