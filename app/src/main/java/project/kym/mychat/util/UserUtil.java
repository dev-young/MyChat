package project.kym.mychat.util;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.kym.mychat.model.UserModel;

public class UserUtil {

    public static void loadUserInfo(String uid, @NonNull final TaskListener.LoadCompleteListener<UserModel> listener) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getException() != null) {
                    RLog.e(task.getException().getMessage());
                    if (listener != null)
                        listener.onComplete(false, null);
                    return;
                }

                if(task.isSuccessful()){
                    UserModel userModel = task.getResult().toObject(UserModel.class);
                    if(userModel != null)
                        listener.onComplete(true, userModel);
                    else
                        listener.onComplete(false, null);
                } else {
                    listener.onComplete(false, null);
                }
            }
        });
    }

    /**UserModel 리스트 불러오기 */
    public static void loadUsersMap(@NonNull final TaskListener.LoadCompleteListener<Map<String, UserModel>> listener) {
        FirebaseFirestore.getInstance().collection("users").limit(100).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getException() != null){
                    RLog.e(task.getException().getMessage());
                    if(listener != null)
                        listener.onComplete(false, null);
                    return;
                }
                Map<String, UserModel> userModels = new HashMap<>();
                for(DocumentSnapshot document : task.getResult().getDocuments()){
                    UserModel userModel = document.toObject(UserModel.class);
                    userModels.put(document.getId(), userModel);
                }
                listener.onComplete(true, userModels);
            }
        });
    }


}
