package project.kym.mychat.views.main.people;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableInt;
import android.view.View;

import java.util.List;

import project.kym.mychat.model.UserModel;
import project.kym.mychat.repository.UserRepository;

public class PeopleListViewModel {
    public ObservableArrayList<UserModel> userModels = new ObservableArrayList<>();
    public final ObservableInt progressBarVisibility = new ObservableInt(View.VISIBLE);
    PeopleListViewContract view;

    public PeopleListViewModel(PeopleListViewContract view) {
        this.view = view;
        loadPeopleList();
    }

    public void loadPeopleList(){
        progressBarVisibility.set(View.VISIBLE);
        UserRepository.getInstance().getPeopleList(new UserRepository.OnUserModelListListener() {
            @Override
            public void onSuccess(List<UserModel> models) {
                userModels.clear();
                userModels.addAll(models);
                progressBarVisibility.set(View.GONE);
            }

            @Override
            public void onError(String errorMessage) {
                progressBarVisibility.set(View.GONE);
            }
        });
    }

    public void onGroupMakerClicked(View v){
        view.startSelectFriendActivity();
    }
}
