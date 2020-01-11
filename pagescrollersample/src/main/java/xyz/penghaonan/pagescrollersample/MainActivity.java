package xyz.penghaonan.pagescrollersample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import xyz.penghaonan.pagescroller.utils.PSLogger;
import xyz.penghaonan.pagescrollersample.features.pulldownrefresh.PullDownRefreshActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PSLogger.setEnable(true);
        setContentView(R.layout.activity_main);
        findViewById(R.id.item_pulldown_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PullDownRefreshActivity.class));
            }
        });
    }
}
