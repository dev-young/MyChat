package project.kym.mychat.views;

import android.os.Bundle;
import android.view.MenuItem;

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
