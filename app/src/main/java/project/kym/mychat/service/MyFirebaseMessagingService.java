package project.kym.mychat.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import project.kym.mychat.R;
import project.kym.mychat.util.PushUtil;
import project.kym.mychat.views.message.MessageActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        RLog.i();
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title").toString();
            String text = remoteMessage.getData().get("text").toString();
            String photo = remoteMessage.getData().get("photoUrl").toString();
            String roomUid = remoteMessage.getData().get("roomUid").toString();
            boolean isGroup = Boolean.parseBoolean(remoteMessage.getData().get("isGroup"));
            if(!roomUid.equals(PushUtil.currentRoomUid)){
                //수신받은 메시지가 현제 보고 있는 채팅방이 아닐 경우에만 수신한다.
                PushUtil.lastNotifiedRoomUid = roomUid;
                sendNotification(title,text,photo, roomUid, isGroup);
                //진동은 여기서 그냥 실행하는게 더 편하다.. 노티 잘 동작 안한다...
                startVibrate(true);
            }


        }


        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "WAKELOCK");

        wakeLock.acquire();
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

    }

    private void startVibrate(boolean vibrate){
        if(vibrate){
            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(800); // 1초간 진동
        }

    }

    private void sendNotification(String title, String text, String photoUrl, String roomUid, boolean isGroup) {
//        Intent intent = new Intent(this, MainActivity.class);
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("chatRoomUid", roomUid);
        intent.putExtra("isGroupMessage", isGroup);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id); // 사용하면 포그라운드에서도 메시지를 수신한다.
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        FutureTarget futureTarget = Glide.with(this)
                .asBitmap()
                .load(photoUrl)
                .apply(new RequestOptions().circleCrop())
                .submit();

        Bitmap bitmap = null;
        try {
            bitmap = (Bitmap) futureTarget.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_pushicon)
                        .setLargeIcon(bitmap)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
//            channel.setVibrationPattern(new long[]{1000, 0, 500, 0});
//            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
