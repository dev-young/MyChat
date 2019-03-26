package project.kym.mychat.views.main.account;

import androidx.databinding.ObservableField;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import project.kym.mychat.model.UserModel;
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

        FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                if(userModel == null){
                    RLog.e("userModel == null");
                } else {
                    userName.set(userModel.getUserName());
                    profileImageUrl.set(userModel.getProfileImageUrl());
                    comment.set(userModel.getComment());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void onCommentClicked(View v){
        view.showDialog();
    }
}
