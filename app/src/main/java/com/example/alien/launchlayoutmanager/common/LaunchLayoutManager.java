package com.example.alien.launchlayoutmanager.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import timber.log.Timber;

public class LaunchLayoutManager extends RecyclerView.LayoutManager {

    private SparseArray<View> mViewCache = new SparseArray<>();
    private Lock mLock = new ReentrantLock();
    private int mOffset = 0;
    private int mMaximumOffset;
    private int mBigViewHeight;
    private int mSmallViewHeight;
    private int mFirstVisibleViewPosition;
    private int mLastVisibleViewPosition;
    private int mFirstVisibleViewTopValue;
    private int mTopAndBottomMargins;
    private boolean mFirst = true;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        Timber.d("onLayoutChildren state.isPreLayout: %b", state.isPreLayout());
        detachAndScrapAttachedViews(recycler);
        doLayoutChildren(recycler);
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
        Timber.d("onMeasure");
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    private void doLayoutChildren(RecyclerView.Recycler recycler) {
        if (getItemCount() != 0) {
            if (getChildCount() == 0 && mFirst) {
                initConstants(recycler);
                mFirst = false;
            }
            calculateVisiblePositions();
            initializeCache();
            fillDown(recycler);
            recyclerCache(recycler);
            childrenRequestLayout(recycler);
        }
    }

    private void childrenRequestLayout(RecyclerView.Recycler recycler) {
        for (int i = mFirstVisibleViewPosition; i <= mLastVisibleViewPosition; i++) {
            View view = recycler.getViewForPosition(i);
            ((LaunchItemView) view).onRequestLayout();
        }

    }

    @Override
    public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
        if (positionStart < mFirstVisibleViewPosition) {
            mOffset += mBigViewHeight * itemCount;
        }
    }

    private void calculateVisiblePositions() {
        if (mBigViewHeight != 0) {
            mMaximumOffset = (getItemCount() - 1) * mBigViewHeight;
            mFirstVisibleViewPosition = mOffset / mBigViewHeight;
            mLastVisibleViewPosition = mFirstVisibleViewPosition;

            int emptyHeight = getHeight();
            mFirstVisibleViewTopValue = mBigViewHeight * mFirstVisibleViewPosition - mOffset;
            int firstVisibleViewBottomValue = mFirstVisibleViewTopValue + mBigViewHeight;
            emptyHeight -= firstVisibleViewBottomValue;
            int secondVisibleViewHeight = getViewHeightByTopValue(firstVisibleViewBottomValue);
            if (emptyHeight - secondVisibleViewHeight >= 0) {
                emptyHeight -= secondVisibleViewHeight;
                mLastVisibleViewPosition++;
                int smallViewPosCount = emptyHeight / mSmallViewHeight;
                mLastVisibleViewPosition += smallViewPosCount;
                emptyHeight -= smallViewPosCount * mSmallViewHeight;
                if (emptyHeight > 0) {
                    mLastVisibleViewPosition++;
                }
            }
            if (mLastVisibleViewPosition > getItemCount() - 1) {
                mLastVisibleViewPosition = getItemCount() - 1;
            }
            Timber.d("calculateVisiblePositions mFirstVisibleViewPosition:%d, mLastVisibleViewPosition:%d",
                    mFirstVisibleViewPosition, mLastVisibleViewPosition);
        }
    }

    private int getViewHeightByTopValue(int topValue) {
        topValue -= mTopAndBottomMargins;
        if (topValue > mBigViewHeight) {
            topValue = mBigViewHeight;
        } else if (topValue < 0) {
            topValue = 0;
        }
        float scale = 1 - (float) topValue / mBigViewHeight;
        int height = (int) (mSmallViewHeight + scale * (mBigViewHeight - mSmallViewHeight));
        Timber.d("getViewHeightByTopValue topValue:%d, scale:%f, height:%d",
                topValue, scale, height);
        return height;
    }

    private void initConstants(RecyclerView.Recycler recycler) {
        // mOffset = 0;
        try {
            View view = recycler.getViewForPosition(0);
            mTopAndBottomMargins = getTopAndBottomMargins(view);
            //mTopAndBottomMargins = 21;
            if (view instanceof LaunchItemView) {
                LaunchItemView launchItemView = (LaunchItemView) view;
                mBigViewHeight = launchItemView.getRootHeightWithMargins() + mTopAndBottomMargins;
                mSmallViewHeight = launchItemView.getTitleHeightWithMargins() + mTopAndBottomMargins;
            } else {
                mBigViewHeight = getViewHeightWithMargins(view) + mTopAndBottomMargins;
                mSmallViewHeight = mBigViewHeight;
            }

            Timber.d("initConstants mOffset:%d, " +
                            "mBigViewHeight:%d, " +
                            "mSmallViewHeight:%d, " +
                            "mTopAndBottomMargins:%d, ",
                    mOffset,
                    mBigViewHeight,
                    mSmallViewHeight,
                    mTopAndBottomMargins
            );

            recycler.recycleView(view);
        } catch (Throwable throwable) {
            Timber.d(throwable);
        }
    }

    private void recyclerCache(RecyclerView.Recycler recycler) {
        for (int i = 0; i < mViewCache.size(); i++) {
            recycler.recycleView(mViewCache.valueAt(i));
        }
    }

    private void fillDown(RecyclerView.Recycler recycler) {
        boolean isViewFromCache;
        int topValue = mFirstVisibleViewTopValue;
        int bottomValue;
        int viewHeight;
        try {
            for (int curPos = mFirstVisibleViewPosition; curPos <= mLastVisibleViewPosition; curPos++) {
                isViewFromCache = true;
                View view = mViewCache.get(curPos);
                if (view == null) {
                    isViewFromCache = false;
                    view = recycler.getViewForPosition(curPos);
                } else {
                    mViewCache.remove(curPos);
                }
                viewHeight = getViewHeightByTopValue(topValue);
                bottomValue = topValue + viewHeight;
                if (view instanceof LaunchItemView) {
                    ((LaunchItemView) view).updateContentSize(viewHeight);
                }
                if (isViewFromCache) {
                    if (view.getTop() != topValue) {
                        view.setTop(topValue);
                    }
                    if (view.getBottom() != bottomValue - mTopAndBottomMargins) {
                        view.setBottom(bottomValue - mTopAndBottomMargins);
                    }
                    attachView(view);
                } else {
                    layoutView(view, topValue, bottomValue);
                }
                topValue = bottomValue;
                if (view instanceof LaunchItemView) {
                    ((LaunchItemView) view).onRequestLayout();
                }
            }
        } catch (Throwable throwable) {
            Timber.d(throwable);
        }
    }


    private void initializeCache() {
        int pos;
        mViewCache.clear();
        for (int i = 0, cnt = getChildCount(); i < cnt; i++) {
            View view = getChildAt(i);
            pos = getPosition(view);
            mViewCache.put(pos, view);
        }

        for (int i = 0; i < mViewCache.size(); i++) {
            detachView(mViewCache.valueAt(i));
        }
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        requestLayout();
    }


    private void layoutView(View view, int top, int bottom) {
        addView(view);
        measureChildWithMargins(view, 0, 0);
        int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();

        layoutDecorated(view,
                layoutParams.leftMargin,
                top + layoutParams.topMargin,
                decoratedMeasuredWidth + layoutParams.rightMargin,
                bottom + layoutParams.bottomMargin);
    }



    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int delta = getScrollDelta(dy);
        mOffset += delta;
        doLayoutChildren(recycler);
        return delta;
    }

    private int getScrollDelta(int dy) {
        if (dy < 0) {
            if (mOffset + dy + mTopAndBottomMargins > 0) {
                return dy;
            } else {
                return -mOffset - mTopAndBottomMargins;
            }
        } else {
            if (mOffset + mTopAndBottomMargins + dy <= mMaximumOffset) {
                return dy;
            } else {
                return mMaximumOffset - mOffset - mTopAndBottomMargins;
            }
        }
    }

    private int getViewHeightWithMargins(View view) {
        measureChildWithMargins(view, 0, 0);
        return getDecoratedMeasuredHeight(view) + getTopAndBottomMargins(view);
    }

    private int getTopAndBottomMargins(View view) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        return layoutParams.bottomMargin + layoutParams.topMargin;
    }

}
