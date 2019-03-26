package project.kym.mychat.views.main.people.adapter;

import androidx.databinding.ObservableField;
import android.view.View;

import project.kym.mychat.model.UserModel;
import project.kym.mychat.util.RLog;
import project.kym.mychat.views.main.people.PeopleListViewContract;

public class PeopleViewModel {
    PeopleListViewContract view;

    public ObservableField<String> userName = new ObservableField<>();
    public ObservableField<String> profileImageUrl = new ObservableField<>();
    public ObservableField<String> comment = new ObservableField<>();
    public ObservableField<String> uid = new ObservableField<>();

    public PeopleViewModel(PeopleListViewContract view) {
        this.view = view;
    }

    public void loadItem(UserModel userModel) {
        uid.set(userModel.getUid());
        userName.set(userModel.getUserName());
        profileImageUrl.set(userModel.getProfileImageUrl());
        comment.set(userModel.getComment());
    }

    public void onItemClicked(View v){
        RLog.e(userName.get());
        view.startDoubleMessageActivity(uid.get(), userName.get());
    }






}
