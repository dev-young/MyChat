package project.kym.mychat.views.message;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import project.kym.mychat.R;
import project.kym.mychat.util.RLog;
import project.kym.mychat.views.BaseActivity;
import project.kym.mychat.views.main.MainActivity;

public class MessageActivity extends BaseActivity{

    public static void start(Activity from, String chatRoomUid, ArrayList<String> destinationUIDs, boolean isGroupMessage){
        Intent intent = new Intent(from, MessageActivity.class);
        if(chatRoomUid != null)
            intent.putExtra("chatRoomUid", chatRoomUid);
        if(destinationUIDs != null)
            intent.putExtra("destinationUids", destinationUIDs);
        intent.putExtra("isGroupMessage", isGroupMessage);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(from, R.anim.fromright,R.anim.toleft);
            from.startActivity(intent,activityOptions.toBundle());
        } else {
            from.startActivity(intent);
        }
    }

    public static void start(Activity from, String chatRoomUid, HashMap<String, Long> roomUsers, boolean isGroupMessage, String title) {
        Intent intent = new Intent(from, MessageActivity.class);
        if (chatRoomUid != null)
            intent.putExtra("chatRoomUid", chatRoomUid);
        if (roomUsers != null)
            intent.putExtra("destinationUids", roomUsers);
        if (title != null)
            intent.putExtra("title", title);
        intent.putExtra("isGroupMessage", isGroupMessage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(from, R.anim.fromright, R.anim.toleft);
            from.startActivity(intent, activityOptions.toBundle());
        } else {
            from.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if(intent.hasExtra("title")){
            getSupportActionBar().setTitle(intent.getStringExtra("title"));
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.messageFrame2, new MessageFragment());
        transaction.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra("fromNotification")){
            setIntent(intent);
            getSupportActionBar().setTitle(intent.getStringExtra("title"));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.messageFrame2, new MessageFragment());
            transaction.commit();
            RLog.i();
        } else {
            RLog.e();
        }



    }

    @Override
    protected void onDestroy() {
        RLog.i();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }
}
