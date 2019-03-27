package project.kym.mychat.views.main.account;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import project.kym.mychat.model.UserModel;
import project.kym.mychat.repository.MyAccount;
import project.kym.mychat.util.RLog;

public class UserModelViewModel {
    AccountViewContract view;

    public ObservableField<String> userName = new ObservableField<>();
    public ObservableField<String> profileImageUrl = new ObservableField<>();
    public ObservableField<String> comment = new ObservableField<>();

    public String uid;

    public UserModelViewModel(AccountViewContract view) {
        this.view = view;
    }

    public void loadUserInfo() {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        MyAccount.getInstance().getUserModel(new MyAccount.OnCompleteListener() {
            @Override
            public void onComplete(boolean isSuccess, UserModel userModel) {
                if (isSuccess) {
                    userName.set(userModel.getUserName());
                    profileImageUrl.set(userModel.getProfileImageUrl());
                    comment.set(userModel.getComment());
                }
            }
        });



    }


    public void onCommentClicked(View v){
        view.showDialog();
    }
}
