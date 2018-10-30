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
    private int mOffset;
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
        Timber.d("onLayoutChildren state.isPreLayout: %b", state.isPreLayout() );
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

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    private void doLayoutChildren(RecyclerView.Recycler recycler) {
        if (getItemCount() != 0) {
            if (getChildCount() == 0) {
                initConstants(recycler);
            }
            calculateVisiblePositions();
            int anchorPos = getAnchorPos();
            initializeCache();
            // drawAnchorView(anchorView, recycler);
            fillDown(recycler);
            // fillUp(recycler, anchorView);
            recyclerCache(recycler);
//            if(mFirst) {
//                requestLayout();
//                mFirst = false;
//            }
        }
    }

    private void calculateVisiblePositions() {
        if (mBigViewHeight != 0) {
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
        mOffset = 0;
        try {
            View view = recycler.getViewForPosition(0);
            mTopAndBottomMargins = getTopAndBottomMargins(view);
            if (view instanceof LaunchItemView) {
                LaunchItemView launchItemView = (LaunchItemView) view;
                mBigViewHeight = launchItemView.getRootHeightWithMargins() + mTopAndBottomMargins;
                mSmallViewHeight = launchItemView.getTitleHeightWithMargins() + mTopAndBottomMargins;
            } else {
                mBigViewHeight = getViewHeightWithMargins(view) + mTopAndBottomMargins;
                mSmallViewHeight = mBigViewHeight;
            }

            mMaximumOffset = (getItemCount() - 1) * mBigViewHeight;

            Timber.d("initConstants mOffset:%d, " +
                            "mBigViewHeight:%d, " +
                            "mSmallViewHeight:%d, " +
                            "mTopAndBottomMargins:%d, " +
                            "mMaximumOffset:%d",
                    mOffset,
                    mBigViewHeight,
                    mSmallViewHeight,
                    mTopAndBottomMargins,
                    mMaximumOffset
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
                    view.setTop(topValue);
                    view.setBottom(bottomValue - mTopAndBottomMargins);
                    attachView(view);
                } else {
                    drawView(view, topValue, bottomValue);
                }
                topValue = bottomValue;
                if (view instanceof LaunchItemView) {
                    ((LaunchItemView) view).getIvMissionIcon().requestLayout();
                    ((LaunchItemView) view).getClTitle().requestLayout();

                    //view.requestLayout();
                 //   ((LaunchItemView) view).getClRoot().requestLayout();
                }
            }
        } catch (Throwable throwable) {
            Timber.d(throwable);
        }
    }
//    private void fillDown(RecyclerView.Recycler recycler, int anchorPos) {
//        int pos = 0;
//        int height = getHeight();
//        int itemCount = getItemCount();
//        boolean fillDown = true;
//        int top = 0;
//
////        if (anchorView != null) {
////            int anchorViewPosition = getPosition(anchorView);
////            View view = mViewCache.get(anchorViewPosition);
////            if (view != null) {
////                recycler.recycleView(view);
////                mViewCache.remove(anchorViewPosition);
////            }
////            pos = getPosition(anchorView) + 1;
////            top = getDecoratedBottom(anchorView);
////        }
//        pos = anchorPos > 0 ? anchorPos - 1 : anchorPos;
//
//        while (fillDown && pos < itemCount) {
//            View view = mViewCache.get(pos);
//            if (view == null) {
//                view = recycler.getViewForPosition(pos);
//                if (view instanceof LaunchItemView) {
//                    if (pos == anchorPos) {
//                        Timber.d("fillDown setScale(MAX_SCALE) to anchorView with pos:%d launchId:%s",
//                                anchorPos, ((LaunchItemView) view).getFlightNumber());
//                        ((LaunchItemView) view).setScale(LaunchItemView.MAX_SCALE);
//                    } else {
//                        ((LaunchItemView) view).setScale(LaunchItemView.MIN_SCALE);
//                    }
//                }
//                top = drawView(view, top, -1);
//            } else {
//                //if (pos == anchorPos) {
//                LaunchItemView launchItemView = (LaunchItemView) view;
//                //Timber.d("fillDown setScale(MAX_SCALE) to anchorView with pos:%d launchId:%s",
//                //      anchorPos, launchItemView.getFlightNumber());
//                launchItemView.setScale(LaunchItemView.MAX_SCALE);
//                int viewBottom = launchItemView.getBottom();
//                int bottomThresholdDown = launchItemView.getRootHeightWithMargins() + launchItemView.getTitleHeightWithMargins();
//                int bottomThresholdUp = launchItemView.getRootHeightWithMargins();
//                Timber.d("fillDown bottomThresholdDown:%d bottomThresholdUp:%d", bottomThresholdDown, bottomThresholdUp);
//
//                Timber.d("fillDown viewBottom:%d", viewBottom);
//                if (viewBottom <= bottomThresholdDown && viewBottom >= bottomThresholdUp) {
//                    float scale = (float) (bottomThresholdDown - viewBottom) / (bottomThresholdDown - bottomThresholdUp);
//                    int newTop = (int) (-scale * (launchItemView.getRootHeightWithMargins() - launchItemView.getTitleHeightWithMargins())
//                            + viewBottom - launchItemView.getTitleHeightWithMargins());
//                    Timber.d("fillDown flightId:%s scale:%f newTop:%d", launchItemView.getFlightNumber(), scale, newTop);
//                    view.setTop(newTop);
//                } else if (viewBottom > bottomThresholdDown) {
//                    int newTop = viewBottom - launchItemView.getTitleHeightWithMargins();
//                    Timber.d("fillDown flightId:%s newTop:%d", launchItemView.getFlightNumber(), newTop);
//                    view.setTop(newTop);
//                }
//                //}
//                attachView(view);
//                mViewCache.remove(pos);
//                top = getDecoratedBottom(view);
//            }
//
//            if (pos == anchorPos - 1 && view instanceof LaunchItemView) {
//                Timber.d("fillDown first view flightId:%s", ((LaunchItemView) view).getFlightNumber());
//            }
//            fillDown = top <= height;
//            pos++;
//        }
//    }

    private void fillUp(RecyclerView.Recycler recycler, View anchorView) {
        if (anchorView != null) {
            int pos = getPosition(anchorView) - 1;
            if (pos >= 0) {
                boolean isAttach = false;
                View view = mViewCache.get(pos);
                if (view != null) {
                    mViewCache.remove(pos);
                    isAttach = true;
                } else {
                    view = recycler.getViewForPosition(pos);
                    if (view instanceof LaunchItemView) {
                        ((LaunchItemView) view).setScale(LaunchItemView.MAX_SCALE);
                    }
                }
                int anchorTop = anchorView.getTop();


                if (isAttach) {
                    int top = anchorTop - getViewHeightWithMargins(view);
                    int viewTop = view.getTop();
                    int delta = top - viewTop;
                    Timber.d("fillUp isAttach:%b top:%d view.top:%d delta:%d", isAttach, top, viewTop, delta);
                    view.offsetTopAndBottom(delta);
                    attachView(view);
                } else {
                    measureChildWithMargins(view, 0, 0);
                    int top = anchorTop - getDecoratedMeasuredHeight(view)
                            - getTopAndBottomMargins(view) - getTopAndBottomMargins(anchorView);
                    drawView(view, top, 0);
                    Timber.d("fillUp isAttach:%b top:%d ", isAttach, top);
                }
            }
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


    private int drawAnchorView(View anchorView, RecyclerView.Recycler recycler) {
        int pos = 0;
        int top = 0;
        if (anchorView != null) {
            anchorView = recycler.getViewForPosition(pos);
            if (anchorView instanceof LaunchItemView) {
                ((LaunchItemView) anchorView).setScale(LaunchItemView.MAX_SCALE);
            }
            //top = drawView(anchorView, top, -1);
        } else {
            attachView(anchorView);
            mViewCache.remove(pos);
            top = getDecoratedBottom(anchorView);
        }

//                fillDown = top <= height;
//                pos++;
        return top;
    }

    // }
    //}

//    private int drawAnchorView(View anchorView, RecyclerView.Recycler recycler) {
//        if (anchorView != null) {
//            int anchorPos = getPosition(anchorView);
//            int newTop;
//            View view = recycler.getViewForPosition(anchorPos);
//
//            if (view instanceof LaunchItemView && anchorView instanceof LaunchItemView) {
//                LaunchItemView launchItemView = (LaunchItemView) view;
//                LaunchItemView launchItemAnchorView = (LaunchItemView) anchorView;
//
//                int anchorTop = anchorView.getTop();
//                float scale = ((float) launchItemView.getRootHeightWithMargins() +
//                        getTopAndBottomMargins(view) - anchorTop) /
//                        launchItemView.getRootHeightWithMargins() * 100;
//                // Log.d("TAG", "drawAnchorView: scale: " + scale);
//                launchItemView.setScale(scale);
//
//                measureChildWithMargins(launchItemView, 0, 0);
//                measureChildWithMargins(launchItemAnchorView, 0, 0);
//
//                int itemHeight = getDecoratedMeasuredHeight(launchItemView);
//                int anchorHeight = getDecoratedMeasuredHeight(launchItemAnchorView);
//
//                newTop = anchorTop - getTopAndBottomMargins(launchItemView) -
//                        (itemHeight - anchorHeight);
//            } else {
//                newTop = view.getTop();
//            }
//            return drawView(view, newTop, -1);
//        }
//        return 0;
//    }

    //    private int drawTopView(View view, int top) {
//        addView(view, 0);
//        return layoutView(view, top);
//    }
//    private int drawView(View view, int top) {
//        addView(view);
//        return layoutView(view, top);
//    }
    private void drawView(View view, int top, int bottom) {
        addView(view);
        measureChildWithMargins(view, 0, 0);
        int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();

        layoutDecorated(view,
                layoutParams.leftMargin,
                top + layoutParams.topMargin,
                decoratedMeasuredWidth + layoutParams.rightMargin,
                bottom);
    }


    private int getAnchorPos() {
        int childCount = getChildCount();
        //int topMin = getHeight();
        int top;
        //View retView = null;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
            top = getDecoratedTop(view) - layoutParams.topMargin;
            if (top > 0) {
                return i;
                //topMin = top;
                //retView = view;
            }
        }
        return 0;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.
            State state) {
//        //if (state.) {
//        //mLock.lock();
//        int delta = getScrollDelta(dy);
//        Log.d("TAGgetScrollDelta", "scrollVerticallyBy: delta: " + delta);
//        offsetChildrenVertical(-delta);
//        doLayoutChildren(recycler);
//        //mLock.unlock();
//        return delta;
//        //}
//        //return 0;

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
//        int childCount = getChildCount();
//        int itemCount = getItemCount();
//        if (childCount == 0) {
//            return 0;
//        }
//
//        final View topView = getChildAt(0);
//        final View bottomView = getChildAt(childCount - 1);
//
//        if (dy < 0) {
        //mLock.lock();
//            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) topView.getLayoutParams();
//            int topPosition = getPosition(topView);
//            Log.d("TAGgetScrollDelta", "topPosition: " + topPosition);
//            if (topPosition > 0) {
//                int top = getDecoratedTop(topView) - layoutParams.topMargin;
//                top = top - topPosition * getViewHeightWithMargins(topView);
//                return Math.max(top, dy);
//            } else {
//                int top = getDecoratedTop(topView) - layoutParams.topMargin;
//                int ret = Math.max(top < 0 ? top : 0, dy);
//                Log.d("TAGgetScrollDelta", "getScrollDelta: top: " + top + " ret:" + ret);
//                // mLock.unlock();
//                return Math.max(top < 0 ? top : 0, dy);
//
//            }
//            return dy;
//        } else {
//            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) bottomView.getLayoutParams();
//            int bottomPosition = getPosition(bottomView);
//            if (bottomPosition < itemCount - 1) {
//                return dy;
//            } else {
//                int bottom = getDecoratedBottom(bottomView) + layoutParams.bottomMargin - getHeight();
//                return Math.min(bottom > 0 ? bottom : 0, dy);
//            }
//        }
    }

    private int getViewHeightWithMargins(View view) {
        measureChildWithMargins(view, 0, 0);
        return getDecoratedMeasuredHeight(view) + getTopAndBottomMargins(view);
    }

    int getTopAndBottomMargins(View view) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        return layoutParams.bottomMargin + layoutParams.topMargin;
    }
//    private int getScrollDelta(int dy) {
//        int childCount = getChildCount();
//        int itemCount = getItemCount();
//        if (childCount == 0){
//            return 0;
//        }
//
//        final View topView = getChildAt(0);
//        final View bottomView = getChildAt(childCount - 1);
//
//        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
//        if (viewSpan <= getHeight()) {
//            return 0;
//        }
//
//        int delta = 0;
//        if (dy < 0){
//            View firstView = getChildAt(0);
//            int firstViewAdapterPos = getPosition(firstView);
//            if (firstViewAdapterPos > 0){
//                delta = dy;
//            } else {
//                int viewTop = getDecoratedTop(firstView);
//                delta = Math.max(viewTop, dy);
//            }
//        } else if (dy > 0){
//            View lastView = getChildAt(childCount - 1);
//            int lastViewAdapterPos = getPosition(lastView);
//            if (lastViewAdapterPos < itemCount - 1){
//                delta = dy;
//            } else {
//                int viewBottom = getDecoratedBottom(lastView);
//                int parentBottom = getHeight();
//                delta = Math.min(viewBottom - parentBottom, dy);
//            }
//        }
//        return delta;
//    }
}
