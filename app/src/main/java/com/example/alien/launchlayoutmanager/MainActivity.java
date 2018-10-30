package com.example.alien.launchlayoutmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.alien.launchlayoutmanager.common.DomainLaunch;
import com.example.alien.launchlayoutmanager.common.LaunchAdapter;
import com.example.alien.launchlayoutmanager.common.LaunchLayoutManager;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LaunchAdapter mLaunchAdapter;
    private LaunchLayoutManager mLaunchLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recycler);
        mLaunchAdapter = new LaunchAdapter();
        loadData();
        mLaunchLayoutManager = new LaunchLayoutManager();

        mRecyclerView.setAdapter(mLaunchAdapter);
        mRecyclerView.setLayoutManager(mLaunchLayoutManager);
       // mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        DomainLaunch launch = new DomainLaunch();
        launch.setFlight_number(1);
        launch.setMission_name("First mission");
        launch.setLaunch_date_utc("date");
        launch.setDetails(" bla -bla -bla");
        launch.setMission_patch_small("https://chernovik.net/sites/default/files/blog_image/kotik-v-shoke-1024x768.jpg");
        mLaunchAdapter.addLaunch(launch);

        launch = new DomainLaunch();
        launch.setFlight_number(2);
        launch.setMission_name("Second mission");
        launch.setLaunch_date_utc("date");
        launch.setDetails("bla-bla-bla");
        launch.setMission_patch_small("https://cache3.youla.io/files/images/720_720_out/5b/84/5b84e1b3074b3e9540415613.jpg");
        mLaunchAdapter.addLaunch(launch);
        mLaunchAdapter.addLaunch(launch);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_add:{
                DomainLaunch launch = new DomainLaunch();
                launch.setMission_name("Added mission");
                launch.setLaunch_date_utc("date");
                launch.setDetails(" bla -bla -bla");
                launch.setMission_patch_small("https://chernovik.net/sites/default/files/blog_image/kotik-v-shoke-1024x768.jpg");
                mLaunchAdapter.addLaunch(launch);
                return true;
            }
            case R.id.mi_insert:{
                DomainLaunch launch = new DomainLaunch();
                launch.setMission_name("Insert mission");
                launch.setLaunch_date_utc("date");
                launch.setDetails(" bla -bla -bla");
                launch.setMission_patch_small("https://chernovik.net/sites/default/files/blog_image/kotik-v-shoke-1024x768.jpg");
                mLaunchAdapter.insertLaunch(launch);
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}
