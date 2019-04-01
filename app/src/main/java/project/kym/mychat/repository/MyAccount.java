package project.kym.mychat.repository;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import androidx.annotation.NonNull;
import project.kym.mychat.model.UserModel;
import project.kym.mychat.util.RLog;
import project.kym.mychat.util.SharedPreferencesUtill;

public class MyAccount {
    private static final MyAccount ourInstance = new MyAccount();
    public static MyAccount getInstance() {
        return ourInstance;
    }
    private MyAccount() {
//        userModel = new UserModel();
    }

    private int accountProvider;
    private UserModel userModel;

    public void load(@NonNull final OnCompleteListener listener){
        RLog.i();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            loadUserInfo(user.getUid(), listener);
        } else {
            RLog.e("FirebaseAuth.getInstance().getCurrentUser().getUid()  is  null !!!");
            listener.onComplete(false, null);
        }

    }

    /**해당 uid를 통해 유저 정보를 가져온다. */
    private void loadUserInfo(String myUid,@NonNull final OnCompleteListener listener) {
        FirebaseFirestore.getInstance().collection("users").document(myUid).get().addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getException() != null){
                    RLog.e(task.getException().getMessage());
                    listener.onComplete(false, null);
                    return;
                }
                userModel = task.getResult().toObject(UserModel.class);
                if(userModel != null){
                    RLog.i(userModel.toString());
                    listener.onComplete(true, userModel);

                } else {
                    listener.onComplete(false, null);

                }

            }
        });
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void getUserModel(OnCompleteListener listener) {
        if(userModel != null){
            listener.onComplete(true, userModel);
        } else {
            load(listener);
        }
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }


    /**유저 정보를 변경시 파스에 업데이트 요청을 한다. */
    public void update(final UserModel newModel, final OnCompleteListener listener){
        FirebaseFirestore.getInstance().collection("users").document(newModel.getUid()).set(newModel).addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.getException() != null){
                    listener.onComplete(false, null);
                    RLog.e(task.getException().getMessage());
                    return;
                }
                if(task.isSuccessful()){
                    userModel = newModel;
                    if(listener != null)
                        listener.onComplete(true, userModel);
                    RLog.i("업데이트 성공!");
                } else {
                    RLog.e("userType 업데이트 실패!");
                    listener.onComplete(false, null);
                }
            }
        });
    }


    public int getAccountProvider() {
        return accountProvider;
    }

    /**SharedPreferences에 provider가 누구인지 저장한다. */
    public void saveAccountProvider(Context context, int provider){
        accountProvider = provider;
        RLog.i("SharedPreferences_accountProvider : " + accountProvider);
        SharedPreferencesUtill.saveAccountProvider(context, provider);
    }

    /**SharedPreferences에 저장된 accountProvider를 불러온다. */
    public int loadAccountProvider(Context context){
        accountProvider = SharedPreferencesUtill.loadAccountProvider(context);
        RLog.i("SharedPreferences_accountProvider : " + accountProvider);
        return accountProvider;
    }

    public void logout() {
        userModel = null;
        FirebaseAuth.getInstance().signOut();
    }


    public interface OnCompleteListener{
        void onComplete(boolean isSuccess, UserModel userModel);
    }

    public interface OnUpdateCompleteListener{
        void onComplete(boolean isSuccess, String key);
    }
}
