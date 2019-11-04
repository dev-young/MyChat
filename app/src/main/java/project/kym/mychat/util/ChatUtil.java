package project.kym.mychat.util;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
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
                    if (!comment.getUserUid().equals(key)) users.put(key, users.get(key)+1);
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
    public static void sendMessage(ChatModel chatModel, final ChatModel.Comment comment, final FirebaseListener.UploadCompleteListener<ChatModel.Comment> uploadCompleteListener) {
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
                        if (!comment.getUserUid().equals(key))
                            users.put(key, users.get(key)+1);   // 안읽은 메시지 카운팅
                    }
                    WriteBatch batch = firestore.batch();
//                    comment.setTimestamp(new Date());
                    batch.set(reference, comment, SetOptions.merge());
                    batch.update(reference, "users", users);
                    batch.update(reference, "lastRead."+comment.getUserUid(), addedUid);
                    batch.commit()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    RLog.d("성공적으로 수행!");
                                    comment.setUid(addedUid);
                                    uploadCompleteListener.onComplete(true, comment);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    RLog.e(e.getMessage());
                                    uploadCompleteListener.onComplete(false, comment);
                                }
                            });
                }
            }
        });

    }

    /** 사진 저장 작업 수행. */
    /**@param fileDatas <파일명, Bitmap> */
    public static void uploadPhotos(String userUid, Map<String, Bitmap> fileDatas, @NonNull FirebaseListener.UploadCompleteListener< /* <FileName, Photo Url> */ Map<String, String>> listener){
        Map<String, String> photoUrls = new HashMap<>();
        if(fileDatas.isEmpty()){
            listener.onComplete(true, photoUrls);
            return;
        }

        int completeCount[] = {0};
        int faileCount[] = {0};
        for (String fileName : fileDatas.keySet()){
            Bitmap bitmap = fileDatas.get(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference reference = FirebaseStorage.getInstance().getReference().child("photos").child(userUid).child(fileName);
            UploadTask uploadTask;
            uploadTask = reference.putBytes(data);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (task.isSuccessful()) {
                        return task.getResult().getStorage().getDownloadUrl();
                    } else
                        throw task.getException();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override /** 이미지 저장 완료 */
                public void onComplete(@NonNull Task<Uri> task) {
                    completeCount[0]++;
                    if(task.getException() != null){
                        RLog.e(task.getException().getMessage());
//                        listener.onComplete(false, null);
                        faileCount[0]++;
                    }
                    if(task.isSuccessful()){
                        photoUrls.put(fileName, task.getResult().toString());
                    } else {
                        RLog.e(fileName + " 저장 실패");
                        faileCount[0]++;
                    }

                    //업로드 작업을 완료
                    if (completeCount[0] == fileDatas.size()){
                        listener.onComplete(true, photoUrls);
                    }
                }
            });

        }
    }

    public static void updateLastRead(String chatRoomUid, String userUid, String readedUid, FirebaseListener.Complete<Void> listener){
        FirebaseFirestore.getInstance().collection("chatrooms").document(chatRoomUid).update("lastRead." + userUid, readedUid).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.getException() != null){
                    task.getException().printStackTrace();
                    listener.onCompelete(false, null);
                    return;
                }

                if (task.isSuccessful()) {
                    RLog.d("updateLastRead 성공");
                    listener.onCompelete(true, null);
                }  else {
                    RLog.d("updateLastRead 실패");
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
