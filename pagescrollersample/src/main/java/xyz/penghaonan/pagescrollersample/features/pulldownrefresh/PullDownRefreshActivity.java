package xyz.penghaonan.pagescrollersample.features.pulldownrefresh;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import xyz.penghaonan.pagescrollersample.R;
import xyz.penghaonan.pagescrollersample.simulation.widget.SampleListViewAdapter;

public class PullDownRefreshActivity extends Activity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Pull Down Refresh");
        setContentView(R.layout.activity_pull_down_refresh);
        listView = findViewById(R.id.listView);
        listView.setAdapter(new SampleListViewAdapter());
    }

}
