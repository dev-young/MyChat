package project.kym.mychat.views.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import project.kym.mychat.R;
import project.kym.mychat.database.AppDatabase;
import project.kym.mychat.repository.MessageRepository;
import project.kym.mychat.repository.MyAccount;
import project.kym.mychat.views.BaseActivity;
import project.kym.mychat.views.LoginActivity;
import project.kym.mychat.views.main.account.AccountFragment;
import project.kym.mychat.views.main.chat.ChatFragment;
import project.kym.mychat.views.main.people.PeopleFragment;

import static project.kym.mychat.util.PushUtil.removeTokenfromServer;
import static project.kym.mychat.util.PushUtil.updatePushTokenToServer;

public class MainActivity extends BaseActivity {

    PeopleFragment peopleFragment;
    ChatFragment chatFragment;
    AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase.getInstance(getApplicationContext());
        MessageRepository.getInstance().clear();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.mainactivity_bottomnavigationview);

        peopleFragment = new PeopleFragment();
        chatFragment = new ChatFragment();
        accountFragment = new AccountFragment();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_people:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout,peopleFragment).commit();
                        return true;
                    case R.id.action_chat:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout,chatFragment).commit();
                        return true;
                    case R.id.action_account:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout,accountFragment).commit();
                        return true;
                }

                return false;
            }
        });
        updatePushTokenToServer();

        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout,chatFragment).commit();
    }


    // 로그아웃
    public void onClick(View view) {
        // 파이어베이스에 저장된 토큰 초기화.
        removeTokenfromServer();

        //로그아웃
        MyAccount.getInstance().logout();
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
