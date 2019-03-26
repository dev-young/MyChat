package project.kym.mychat.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import project.kym.mychat.BuildConfig;
import project.kym.mychat.R;
import project.kym.mychat.util.RLog;

/**파이어베이스 서버에서 서버 상태를 가져와 다음 엑티비티를 실행할지 여부를 결정한다. */
public class SplashActivity extends BaseActivity {

    private LinearLayout linearLayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        linearLayout = (LinearLayout) findViewById(R.id.splashactivity_linaerlayout);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.default_config);

        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        RLog.i();
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                        } else {

                        }
                        displayMessage();
                    }
                });


    }

    void displayMessage() {

        String splash_background = mFirebaseRemoteConfig.getString("splash_background");
        boolean caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
        String splash_message = mFirebaseRemoteConfig.getString("splash_message");

//        linearLayout.setBackgroundColor(Color.parseColor(splash_background));

        if(caps){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(splash_message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.create().show();
            RLog.e(splash_message);
        }else {
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }

    }


}
