package com.example.alien.launchlayoutmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        DomainLaunch launch = new DomainLaunch();
        launch.setFlight_number(1);
        launch.setMission_name("First mission");
        launch.setLaunch_date_utc("date");
        launch.setDetails(" bla -bla -bla");
        launch.setMission_patch_small("https://chernovik.net/sites/default/files/blog_image/kotik-v-shoke-1024x768.jpg");

        mLaunchAdapter.addLaunch(launch);
        mLaunchAdapter.addLaunch(launch);
        mLaunchAdapter.addLaunch(launch);
    }


}
