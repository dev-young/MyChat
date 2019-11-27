package project.kym.mychat.views.main.people;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import project.kym.mychat.model.UserModel;
import project.kym.mychat.repository.UserRepository;
import project.kym.mychat.util.RLog;

public class PeopleListViewModel extends ViewModel {
    public MutableLiveData<List<UserModel>> userModels = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public final ObservableInt progressBarVisibility = new ObservableInt(View.VISIBLE);
    PeopleListViewContract view;

    public PeopleListViewModel() {
        userModels.setValue(new ArrayList<>());
        isLoading.setValue(false);
    }

    public void setView(PeopleListViewContract view) {
        this.view = view;
    }

    public void loadPeopleList(){
        isLoading.setValue(true);
        UserRepository.getInstance().getPeopleList(new UserRepository.OnUserModelListListener() {
            @Override
            public void onSuccess(List<UserModel> models) {
                userModels.setValue(models);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.setValue(false);
            }
        });
    }

    public void onGroupMakerClicked(View v){
        view.startSelectFriendActivity();
    }

}
