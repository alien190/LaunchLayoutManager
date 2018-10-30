package com.example.alien.launchlayoutmanager.common;

import android.support.v7.widget.RecyclerView;
import android.view.View;


public class LaunchViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    private int mFlightNumber;
    private LaunchAdapter.OnItemClickListener mClickCallback = null;
    //private ImageView mImageView;

//    @BindView(R.id.iv_mission_icon)
//    ImageView mIvMissionIcon;


    public LaunchViewHolder(View view) {
        super(view);
        mView = view;
        //mImageView = mView.findViewById(R.id.iv_icon);

        //ButterKnife.bind(this, mView);
    }

    public void bind(DomainLaunch launch, LaunchAdapter.OnItemClickListener clickListener) {
        // mTvFlightNumber.setText(String.valueOf(launch.getFlight_number()));
        if (mView instanceof LaunchItemView) {
            LaunchItemView launchItemView = (LaunchItemView) mView;
            mFlightNumber = launch.getFlight_number();
            launchItemView.setMissionIconURL(launch.getMission_patch_small());
            //launchItemView.setMissionIconBitmap(DbBitmapUtility.getImage(launch.getImage()));
            launchItemView.setMissionName(launch.getMission_name());
            launchItemView.setDetails(launch.getDetails());
            launchItemView.setLaunchDate(launch.getLaunch_date_utc());
            launchItemView.setTransitionName(String.valueOf(launch.getFlight_number()));
            //launchItemView.setIconTransitionName(String.valueOf(launch.getFlight_number()));
            //mImageView.setImageBitmap(DbBitmapUtility.getImage(launch.getImage()));
            //mImageView.setTransitionName(String.valueOf(launch.getFlight_number()));
            mClickCallback = clickListener;
            launchItemView.setOnClickListener(mOnClickListener);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mClickCallback != null) {
                View sharedView = null;
                if (mView instanceof LaunchItemView) {
                    sharedView = ((LaunchItemView) mView).getIvMissionIcon();
                }
                mClickCallback.onItemClick(mFlightNumber, sharedView);
            }
        }
    };
}
