package com.example.alien.launchlayoutmanager.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.example.alien.launchlayoutmanager.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class LaunchItemView extends CardView {
    public static final int MIN_SCALE = 0;
    public static final int MAX_SCALE = 100;
    private View mView;
    private LinearLayout mClTitle;
    private RelativeLayout mClRoot;
    private ImageView mIvMissionIcon;
    private TextView mTvMissionName;
    private TextView mTvDetails;
    private TextView mTvLaunchDate;
    private int mRootHeight;
    private int mRootHeightWithMargins;
    private int mTitleHeight;
    private int mTitleHeightWithMargins;
    private int mTopAndBottomMargins;
    private int mIconHeight;

    public LaunchItemView(Context context) {
        this(context, null);
    }

    public LaunchItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LaunchItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mView = inflate(context, R.layout.launch_item_view, this);
        mClRoot = mView.findViewById(R.id.cl_root);
        mClTitle = mView.findViewById(R.id.cl_title);
        mIvMissionIcon = mView.findViewById(R.id.iv_mission_icon);
        mTvMissionName = mView.findViewById(R.id.tv_mission_name);
        mTvDetails = mView.findViewById(R.id.tv_details);
        mTvLaunchDate = mView.findViewById(R.id.tv_launch_date);
        measureHeight();
    }

    private void measureHeight() {
        measureChild(mClRoot, 0, 0);
        CardView.LayoutParams rootLayoutParams = (FrameLayout.LayoutParams) mClRoot.getLayoutParams();
        LinearLayout.LayoutParams titleLayoutParams = (LinearLayout.LayoutParams) mClTitle.getLayoutParams();
        mTitleHeight = mClTitle.getMeasuredHeight();
        mTitleHeightWithMargins = mTitleHeight + titleLayoutParams.topMargin + titleLayoutParams.bottomMargin
                + rootLayoutParams.topMargin + rootLayoutParams.bottomMargin;
        mRootHeight = mClRoot.getMeasuredHeight();
        mRootHeightWithMargins = mRootHeight + rootLayoutParams.topMargin + rootLayoutParams.bottomMargin;
        mTopAndBottomMargins = rootLayoutParams.topMargin + rootLayoutParams.bottomMargin;
        //mGuideline.setGuidelineBegin(mRootHeightWithMargins + mTopAndBottomMargins);
        mTvMissionName.offsetLeftAndRight(mRootHeightWithMargins + mTopAndBottomMargins);
        mTvMissionName.setRight(mIvMissionIcon.getLeft() + mRootHeightWithMargins);
        mTvMissionName.setBottom(mIvMissionIcon.getTop() + mRootHeightWithMargins);
    }


    public void setCollapsed(boolean isCollapsed) {
        if (isCollapsed) {
            setViewSize(mView, mTitleHeightWithMargins, -1);
            setViewSize(mIvMissionIcon, mTitleHeight, mTitleHeight);
        } else {
            setViewSize(mView, mRootHeightWithMargins, -1);
        }
    }

    public void setScale(float percent) {
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
        int overallHeight = (int) (mTitleHeightWithMargins + (mRootHeightWithMargins - mTitleHeightWithMargins) * percent / 100);
        setViewSize(mView, overallHeight, -1);
        int iconHeight = (int) (mTitleHeight + (mRootHeight - mTitleHeight) * percent / 100);
        setViewSize(mIvMissionIcon, iconHeight, iconHeight);
    }

    public void updateContentSize(int value) {
        //    int iconHeight = getBottom() - getTop() - mTopAndBottomMargins;
        //setViewSize(mIvMissionIcon, height, height);
        // mIvMissionIcon.setBottom(mIvMissionIcon.getTop() + value);
        //mIvMissionIcon.setRight(mIvMissionIcon.getLeft() + value);
        // setViewSize(mIvMissionIcon, value + mTopAndBottomMargins, value +mTopAndBottomMargins);
//        ViewGroup.LayoutParams layoutParams = mIvMissionIcon.getLayoutParams();
//        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;


        value -= 2 * mTopAndBottomMargins;
        Drawable drawable = mIvMissionIcon.getDrawable();
        Rect bounds = drawable.getBounds();
        int height = Math.max(bounds.height(), bounds.width());
        float scale = (float) (value) / height;
        mIvMissionIcon.setPivotX(0f);
        mIvMissionIcon.setPivotY(0f);
        mIvMissionIcon.setScaleX(scale);
        mIvMissionIcon.setScaleY(scale);
        mIconHeight = value;

        //requestLayout();

        // forceLayout();
        //  mIvMissionIcon.forceLayout();
        //mIvMissionIcon.setRight(value);
        //mIvMissionIcon.setBottom(value);
    }

    private void setViewSize(View view, int height, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (height != -1) {
            layoutParams.height = height;
        }
        if (width != -1) {
            layoutParams.width = width;
        }
        view.setLayoutParams(layoutParams);
    }

    public int getRootHeightWithMargins() {
        measureHeight();
        return mRootHeightWithMargins;
    }

    public int getTitleHeightWithMargins() {
        measureHeight();
        return mTitleHeightWithMargins;
    }

    public void setMissionName(String name) {
        if (mTvMissionName != null && name != null) {
            mTvMissionName.setText(name);
        }
    }

    public void setDetails(String details) {
        if (mTvDetails != null && details != null) {
            mTvDetails.setText(details);
        }
    }

    public void setLaunchDate(String date) {
        if (mTvLaunchDate != null && date != null) {
            mTvLaunchDate.setText(date);
        }
    }

    public void setMissionIconURL(String url) {
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.ic_rocket_stub)
                .error(R.drawable.ic_rocket_stub)
                .into(mIvMissionIcon, new Callback() {
                    @Override
                    public void onSuccess() {
                        updateContentSize(mIconHeight);
                    }

                    @Override
                    public void onError(Exception e) {
                        updateContentSize(mIconHeight);
                    }
                });
    }

    public void setMissionIconBitmap(Bitmap bitmap) {
        if (mIvMissionIcon != null && bitmap != null) {
            try {
                mIvMissionIcon.setImageBitmap(bitmap);
                //invalidate();
                //requestLayout();
                //forceLayout();
            } catch (Throwable throwable) {
                Timber.d(throwable);
            }
        }
    }

    public void setIconTransitionName(String name) {
        if (mIvMissionIcon != null && name != null) {
            mIvMissionIcon.setTransitionName(name);
        }
    }

    public ImageView getIvMissionIcon() {
        return mIvMissionIcon;
    }

    public RelativeLayout getClRoot() {
        return mClRoot;
    }

    public LinearLayout getClTitle() {
        return mClTitle;
    }
}
