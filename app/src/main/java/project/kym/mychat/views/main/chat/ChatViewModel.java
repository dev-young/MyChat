package project.kym.mychat.views.main.chat;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.kym.mychat.model.ChatModel;
import project.kym.mychat.repository.ChatRepository;
import project.kym.mychat.repository.MyAccount;

public class ChatViewModel extends AndroidViewModel {
    MutableLiveData<Map<String, ChatModel>> chatModelMap = new MutableLiveData<>();
    MutableLiveData<Boolean> isListEmpty = new MutableLiveData<>();
    ChatRepository chatRepository;

    public ChatViewModel(Application application) {
        super(application);
        chatRepository = ChatRepository.getInstance();
        chatModelMap.setValue(new HashMap<>());
        isListEmpty.setValue(true);

    }

    public void load(String userUid){
        chatRepository.startListen(userUid, new ChatRepository.OnEventListener() {
            @Override
            public void onAdded(String key, ChatModel chatModel) {
                Map<String, ChatModel> modelMap = chatModelMap.getValue();
                modelMap.put(key, chatModel);
                chatModelMap.setValue(modelMap);
                isListEmpty.setValue(false);
            }

            @Override
            public void onChanged(String key, ChatModel chatModel) {
                Map<String, ChatModel> modelMap = chatModelMap.getValue();
                modelMap.put(key, chatModel);
                chatModelMap.setValue(modelMap);
            }
        });
    }

    public MutableLiveData<Map<String, ChatModel>> getChatModelMap() {
        return chatModelMap;
    }

    public MutableLiveData<Boolean> getIsListEmpty() {
        return isListEmpty;
    }

    public void stop() {
        chatRepository.stopListen();
    }
}
