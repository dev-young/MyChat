package project.kym.mychat.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import androidx.annotation.NonNull;
import project.kym.mychat.R;
import project.kym.mychat.util.RLog;
import project.kym.mychat.views.main.MainActivity;

/** FirebaseAuth를 이용하여 로그인 로직을 수행한다. */
public class LoginActivity extends BaseActivity {

    private EditText id;
    private EditText password;

    private Button login;
    private Button signup;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        RLog.i();
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }
        id = (EditText) findViewById(R.id.loginActivity_edittext_id);
        password = (EditText) findViewById(R.id.loginActivity_edittext_password);

        login = (Button) findViewById(R.id.loginActivity_button_login);
        signup = (Button) findViewById(R.id.loginActivity_button_signup);
        login.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        signup.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//        login.setBackgroundColor(Color.parseColor(splash_background));
//        signup.setBackgroundColor(Color.parseColor(splash_background));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEvent();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        //로그인 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //로그인
                    RLog.i("로그인 성공");
                    RLog.i(user.getEmail());
                    RLog.i(user.getUid());
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //로그아웃
                }
            }
        };
    }

    void loginEvent() {
        RLog.i();
        if(id.getText().toString().isEmpty() || password.getText().toString().isEmpty())
            return;

        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            //로그인 실패한부분
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else{
                            RLog.i("로그인 성공");
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
