package project.kym.mychat.util;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import project.kym.mychat.model.NotificationModel;

public class PushUtil {
    private static OkHttpClient okHttpClient = new OkHttpClient();

    public static String currentRoomUid = "";   // 현재 보여지고 있는 채팅방의 uid
    public static String lastNotifiedRoomUid = "";  // 가장 마지막으로 Notified 된 채팅방의 uid

    /** 클라우드 메시지 전송 (푸시) */
    public static void sendFCM_Message(List<String> pushTokens, String title, String body, String photoUrl, boolean isGroup) {
        NotificationModel.MessageData messageData = new NotificationModel.MessageData();
        messageData.title = title;
        messageData.text = body;
        messageData.photoUrl = photoUrl;
        messageData.roomUid = currentRoomUid;
        messageData.isGroup = isGroup;
        NotificationModel notificationModel = new NotificationModel(messageData);

        request(pushTokens, notificationModel);
    }


    /**토큰 리스트를 바탕으로 NotificationModel전송한다. */
    private static void request(List<String> pushTokens, NotificationModel notificationModel){
        Gson gson = new Gson();
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                RLog.e();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                RLog.i();
            }
        };
        RLog.e(pushTokens.toString());
        for(String token : pushTokens){
            if(token == null || token.isEmpty())
                continue;
            notificationModel.to = token;
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
            Request request = new Request.Builder()
                    .header("Content-Type", "application/json")
                    .addHeader("Authorization", "key=AIzaSyATpPFLNJCYqEv5IQI0Ax7DaH0EviLJtT8")
                    .post(requestBody)
                    .url("https://fcm.googleapis.com/fcm/send")
                    .build();
            okHttpClient.newCall(request).enqueue(callback);
        }

    }

    /** 서버에 내 기기의 토큰값을 저장한다. */
    public static void updatePushTokenToServer(){
        RLog.i();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String,Object> map = new HashMap<>();
        map.put("pushToken",token);

        FirebaseFirestore.getInstance().collection("users").document(uid).update(map);
    }

    /** 서버에 저장된 내 기기의 토큰값을 제거한다. */
    public static void  removeTokenfromServer(){
        RLog.i();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = "";
        Map<String,Object> map = new HashMap<>();
        map.put("pushToken",token);

        FirebaseFirestore.getInstance().collection("users").document(uid).update(map);
    }
}
