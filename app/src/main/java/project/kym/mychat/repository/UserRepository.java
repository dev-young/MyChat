package project.kym.mychat.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.kym.mychat.model.UserModel;
import project.kym.mychat.util.RLog;
import project.kym.mychat.util.RxUtil;
import project.kym.mychat.util.TaskListener;
import project.kym.mychat.util.UserUtil;

public class UserRepository {
    private static UserRepository instance;

    private UserRepository() {
    }

    public static UserRepository getInstance() {
        if(instance == null)
            instance = new UserRepository();
        return instance;
    }

    Map<String, UserModel> userModelMap = new HashMap<>();
    Map<String, Boolean> userLoadingMap = new HashMap<>(); //<uid, 로딩중인지 여부> 해당 유저의 uid가 서버에 요청중인지 확인.

    public void getPeopleList(final OnUserModelListListener onUserModelListListener){
        getPeopleListFromFireStore(onUserModelListListener);
    }

    private List<UserModel> getList(){
        List<UserModel> userModels = new ArrayList<>();
        for(UserModel model : userModelMap.values()){
            userModels.add(model);
        }

        return userModels;
    }

    public void loadUserModel(String userUid, TaskListener.LoadCompleteListener<UserModel> listener){

        UserModel targetModel = userModelMap.get(userUid);
        if(targetModel == null){
            Boolean isLoading = userLoadingMap.get(userUid);
            isLoading = isLoading != null? isLoading : false;
            if(isLoading){
                RxUtil.simpleTask(isLoading, new RxUtil.SimpleTask<Boolean>() {
                    @Override
                    public Boolean backTask() {
                        int tryCount;
                        for (tryCount = 0; tryCount < 50; tryCount++) {
                            Boolean isLoading = userLoadingMap.get(userUid);
                            if(isLoading != null && isLoading){
                                RLog.i("유저 불러오기 대기(로컬) " + userUid);
                                try {
                                    Thread.sleep(180);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else break;
                        }
                        if (userModelMap.get(userUid) != null)
                            return true;
                        else
                            return false;
                    }

                    @Override
                    public void uiTask(Boolean result) {
                        UserModel userModel = userModelMap.get(userUid);
                        listener.onComplete(result, userModel);
                        if(userModel != null)
                            RLog.i("유저 불러오기 완료(로컬): " + userModel.getUserName());
                    }
                });
            } else {
                userLoadingMap.put(userUid, true);
                UserUtil.loadUserInfo(userUid, new TaskListener.LoadCompleteListener<UserModel>() {
                    @Override
                    public void onComplete(boolean isSuccess, UserModel resutl) {
                        if (isSuccess) {
                            RLog.i("유저 불러오기 완료: " + resutl.getUserName());
                            userModelMap.put(resutl.getUid(), resutl);
                            userLoadingMap.remove(userUid);
                        }
                        listener.onComplete(isSuccess, resutl);
                    }
                });
            }

        } else {
            listener.onComplete(true, targetModel);
        }
    }

    private void getPeopleListFromFireStore(final OnUserModelListListener onUserModelListListener){
        MyAccount.getInstance().getUserModel(new MyAccount.OnCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, UserModel userModel) {
                if (isSuccess) {
                    final String myUid = userModel.getUid();
                    if(userModelMap.isEmpty()){
                        FirebaseFirestore.getInstance().collection("users").get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                            UserModel userModel = snapshot.toObject(UserModel.class);
                                            if(userModel.getUid() != null && userModel.getUid().equals(myUid)){
                                                continue;
                                            }
                                            userModelMap.put(snapshot.getId(), userModel);
                                        }
                                        onUserModelListListener.onSuccess(getList());
                                    }

                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        RLog.e(e.getMessage());
                                        onUserModelListListener.onError(e.getMessage());
                                    }
                                });
                    } else {
                        onUserModelListListener.onSuccess(getList());
                    }
                } else {
                    onUserModelListListener.onError("");
                }
            }
        });
    }

    @Deprecated
    private void getPeopleListFromRealTimeDB(final OnUserModelListListener onUserModelListListener){
        final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<UserModel> userModels = new ArrayList<>();
                for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if(userModel.getUid() != null && userModel.getUid().equals(myUid)){
                        continue;
                    }
                    userModels.add(userModel);
                }
                onUserModelListListener.onSuccess(userModels);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                RLog.e(databaseError.getMessage());
                onUserModelListListener.onError(databaseError.getMessage());
            }
        });
    }

    public interface OnUserModelListListener{
        void onSuccess(List<UserModel> userModels);
        void onError(String errorMessage);
    }
}
