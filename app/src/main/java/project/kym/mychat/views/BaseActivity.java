package project.kym.mychat.views;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import project.kym.mychat.util.RLog;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        RLog.i();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        RLog.i();
        super.onResume();
    }

    @Override
    protected void onStart() {
        RLog.i();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        RLog.i();
        super.onDestroy();
    }
}
