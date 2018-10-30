package com.example.alien.launchlayoutmanager.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alien.launchlayoutmanager.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LaunchAdapter extends RecyclerView.Adapter<LaunchViewHolder> {

    private List<DomainLaunch> mLaunches = new ArrayList<>();
    private Lock mLaunchesLock = new ReentrantLock();
    private OnItemClickListener mItemClickListener;


    @NonNull
    @Override
    public LaunchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.li_launch, parent, false);
        return new LaunchViewHolder(view);
//        LaunchListItemBinding launchListItemBinding = LaunchListItemBinding.inflate(layoutInflater, parent, false);
//
//        return new LaunchViewHolder(launchListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull LaunchViewHolder holder, int position) {
        holder.bind(mLaunches.get(position), mItemClickListener);
    }

    @Override
    public int getItemCount() {

        int size = 0;
        if (mLaunches != null) {
            mLaunchesLock.lock();
            try {
                size = mLaunches.size();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                mLaunchesLock.unlock();
            }
        }
        return size;
    }

    public void updateLaunches(List<DomainLaunch> launches) {
        mLaunches.clear();
        mLaunches.addAll(launches);
        notifyDataSetChanged();
    }

    public void addLaunch(DomainLaunch launch) {
        mLaunches.add(launch);
        //notifyItemInserted(mLaunches.size() - 1);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(int flightNumber, View sharedView);
    }

    public void setItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }
}
