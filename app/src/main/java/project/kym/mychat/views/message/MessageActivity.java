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
import project.kym.mychat.views.main.MainActivity;

public class MessageActivity extends AppCompatActivity implements MessageContract.View {

    private Button button;
    private EditText editText;
    private TextView dateTextView;
    private RecyclerView recyclerView;
    private MessageRecyclerViewAdapter messageRecyclerViewAdapter;

    private MessagePresenter presenter;

    // 에디트 텍스트가 클릭될때 어뎁터에 표시된 마지막 아이템 포지션
    private int lastVisiblePostionWhenEditTextClicked = 0;  // 이 값을 통해 키보드가 올라올 당시 스크롤 이벤트를 발생시킬지 여부 결정

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

        button = (Button) findViewById(R.id.messageActivity_button);
        editText = (EditText) findViewById(R.id.messageActivity_editText);
        recyclerView = (RecyclerView) findViewById(R.id.messageActivity_reclclerview);
        dateTextView = findViewById(R.id.messageActivity_date);

        messageRecyclerViewAdapter = new MessageRecyclerViewAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageRecyclerViewAdapter);

        presenter = new MessagePresenter(this);
        presenter.setAdapter(messageRecyclerViewAdapter);
        presenter.init(getIntent());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onSendButtonClicked(editText.getText().toString());
            }
        });


        // 상단에 날짜 표시 애니메이션 설정
        final Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(newState == 0){
                    dateTextView.setAnimation(fadeout);
                    fadeout.start();
                    dateTextView.setVisibility(View.INVISIBLE);
                }
                else{
                    if(dateTextView.getVisibility() != View.VISIBLE){
                        dateTextView.setAnimation(fadein);
                        fadein.start();
                    }
                    dateTextView.setVisibility(View.VISIBLE);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy < 15 || dy%3 == 0 || dy%5 == 0){
//                    RLog.i(""+dy);
                    int firstCompletelyVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
//                    int lastCompletelyVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
//                    final int lastAdapterItem = recyclerView.getAdapter().getItemCount() - 1;
//                    RLog.i("first: "+firstCompletelyVisibleItemPosition + "  last: " + lastCompletelyVisibleItemPosition);
//                    RLog.i(lastAdapterItem+"");
                    presenter.listenFirstVisiblePosition(firstCompletelyVisibleItemPosition);
                }

            }
        });

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    lastVisiblePostionWhenEditTextClicked = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
//                    RLog.i(lastVisiblePostionWhenEditTextClicked + "");
                }
                return false;
            }
        });

        // 키보드가 올라올때 스크롤을 아래로 향하도록 설정
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {   // 바닥이 위로 올라왔을때 == 세로길이가 줄었을때
                    final int lastItemPosition = recyclerView.getAdapter().getItemCount() - 1;
                    if(lastItemPosition > -1 && lastItemPosition == lastVisiblePostionWhenEditTextClicked)
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.smoothScrollToPosition(lastItemPosition);
                            }
                        }, 10);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        presenter.onResume();
        super.onResume();
    }

    @Override
    protected void onStop() {
        presenter.onStop();
        super.onStop();
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

    @Override
    public void scrollToPosition(int position) {
        recyclerView.scrollToPosition(position);
    }

    @Override
    public void setSendButtonEnabled(boolean enabled) {
        button.setEnabled(enabled);
    }

    @Override
    public void clearEditText() {
        editText.setText("");
    }

    @Override
    public void setDateTextView(String text) {
        dateTextView.setText(text);

    }

    @Override
    public void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);
    }

    @Override
    public void showProgress(boolean b) {
        ProgressBar progressBar = findViewById(R.id.progress);
        if(b)
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.GONE);
    }
}
