/*
 * Copyright (c) 2015-present, Gfycat, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gfycat.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.ViewPropertyAnimatorUpdateListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.UIUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dgoliy on 4/24/17.
 * <br>
 * Add two children into {@link VerticalDrawerLayout}. First child will be your main layout,
 * second one will be your drawer layout.
 */
public class VerticalDrawerLayout extends FrameLayout {

    private static final String TAG = "VerticalDrawerLayout";

    private static final String EXTRA_STATE = "EXTRA_STATE";
    private static final String EXTRA_OLD_STATE = "EXTRA_OLD_STATE";

    private final int DEFAULT_BG_COLOR = 0x99000000; // 80% gray
    private final int SCROLL_POINTER_ID = 0;

    /**
     * In case of a more complex Drawer view (rather then just a simple scrollable view), the Drawer view class should
     * implement this interface to pass scrolling state from an inner view your layout is containing.
     */
    public interface DrawerScrollable {
        View getScrollableView();
    }

    public interface DrawerSlideListener {
        void onDrawerSlide(View drawer, float offset);

        void onDrawerStateChanged(@State int state);
    }

    public static final int STATE_UNDEFINED = 0;
    public static final int STATE_CLOSED = 1;
    public static final int STATE_OPENED = 2;
    public static final int STATE_DRAGGING = 3;

    @IntDef({STATE_UNDEFINED, STATE_CLOSED, STATE_OPENED, STATE_DRAGGING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    private final PointF mCurrentTouch = new PointF();
    private final PointF mLastTouch = new PointF();

    private boolean mDrawerAnimationInProgress = false;
    private boolean mVerticalScrollingInProgress = false;

    private VelocityTracker mVelocityTracker;

    private Runnable onGlobalLayoutDelayedRunnable;

    private DrawerSlideListener mDrawerSlideListener;

    @State
    private int mDrawerOldState = STATE_UNDEFINED;

    @State
    private int mDrawerState = STATE_UNDEFINED;

    private boolean doDimOnDrawerSlide = true;

    private boolean isDrawerEnabled = true;

    private boolean isDrawerOpenedOnStart = false;

    private int mFlingSlop, mTouchSlop;

    private float mOpenedPosition = 0.0f;
    private float mClosedPosition = 0.0f;

    private View mMainView, mDrawerView;

    public VerticalDrawerLayout(@NonNull Context context) {
        this(context, null);
    }

    public VerticalDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttrs(attrs);
        internalConstructor();
    }

    private void internalConstructor() {
        mFlingSlop = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mVelocityTracker = VelocityTracker.obtain();
        setBackgroundColor(DEFAULT_BG_COLOR);

        applyState(isDrawerOpenedOnStart ? STATE_OPENED : STATE_CLOSED);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mClosedPosition = getHeight();
                runOnGlobalLayoutDelayedRunnable();
            }
        });
    }

    private void readAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.VerticalDrawerLayout);
        mOpenedPosition = UIUtils.getStatusBarHeightAccordingToVersion(getResources());
        mOpenedPosition += a.getDimension(R.styleable.VerticalDrawerLayout_drawerOpenedPosition, 0.0f);
        isDrawerOpenedOnStart = a.getBoolean(R.styleable.VerticalDrawerLayout_drawerOpenedOnStart, false);
        a.recycle();

        Logging.d(TAG, "VerticalDrawerLayout attributes set to: openedPosition=", mOpenedPosition);
    }

    private void switchDrawerStateWithPosition(float yPosition) {
        if (yPosition == mOpenedPosition) {
            switchDrawerState(STATE_OPENED);
        } else if (yPosition == mClosedPosition) {
            switchDrawerState(STATE_CLOSED);
        } else {
            switchDrawerState(STATE_DRAGGING);
        }
    }

    private void switchDrawerState(@State int newState) {
        if (newState == mDrawerState) return;

        if (newState == STATE_CLOSED) {
            mDrawerView.setVisibility(View.INVISIBLE);
        } else {
            mDrawerView.setVisibility(View.VISIBLE);
        }

        mDrawerOldState = mDrawerState;
        mDrawerState = newState;
    }

    public void setDrawerSlideListener(DrawerSlideListener drawerSlideListener) {
        mDrawerSlideListener = drawerSlideListener;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt(EXTRA_STATE, mDrawerState);
        bundle.putInt(EXTRA_OLD_STATE, mDrawerOldState);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        if (parcel instanceof Bundle) {
            Bundle bundle = (Bundle) parcel;
            applyState(bundle.getInt(EXTRA_STATE) == STATE_OPENED ? STATE_OPENED : STATE_CLOSED);
            //noinspection WrongConstant
            mDrawerOldState = bundle.getInt(EXTRA_OLD_STATE);
            parcel = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(parcel);
    }

    private void applyState(@State int state) {
        if (state == mDrawerState) return;
        if (state == STATE_OPENED) {
            openDrawer(false);
        } else {
            closeDrawer(false);
        }
    }

    /**
     * Get <i>drawerOpenedOnStart</i> XML attribute value
     */
    public boolean isDrawerOpenedOnStart() {
        return isDrawerOpenedOnStart;
    }

    /**
     * Get current drawer {@link State}
     *
     * @return {@link #STATE_OPENED} or {@link #STATE_CLOSED}
     */
    @State
    public int getDrawerState() {
        return mDrawerState;
    }

    /**
     * Opens the drawer
     *
     * @param animate is opening should be animated
     */
    public void openDrawer(final boolean animate) {
        Logging.i(TAG, "openDrawer(" + animate + ")");
        ensureOnGlobalLayoutAndRun(new Runnable() {
            @Override
            public void run() {
                translateDrawerTo(mOpenedPosition, animate);
                if (mDrawerSlideListener != null)
                    mDrawerSlideListener.onDrawerStateChanged(STATE_OPENED);
            }
        });
    }

    /**
     * Closes the drawer
     *
     * @param animate is closing should be animated
     */
    public void closeDrawer(final boolean animate) {
        Logging.i(TAG, "closeDrawer(" + animate + ")");
        ensureOnGlobalLayoutAndRun(new Runnable() {
            @Override
            public void run() {
                translateDrawerTo(mClosedPosition, animate);
                if (mDrawerSlideListener != null)
                    mDrawerSlideListener.onDrawerStateChanged(STATE_CLOSED);
            }
        });
    }

    /**
     * Handles onBackPressed by closing the drawer if opened
     *
     * @return true if handled
     */
    public boolean onBackPressed() {
        if (mDrawerState != STATE_CLOSED) {
            closeDrawer(true);
            return true;
        }

        return false;
    }

    public void setDimOnDrawerSlide(boolean doDimOnDrawerSlide) {
        this.doDimOnDrawerSlide = doDimOnDrawerSlide;
    }

    public void setDrawerEnabled(boolean enabled) {
        isDrawerEnabled = enabled;
    }

    private void translateDrawerTo(float yPosition, boolean animate) {
        if (mDrawerAnimationInProgress) {
            return;
        }
        if (yPosition < mOpenedPosition) yPosition = mOpenedPosition;
        if (yPosition > mClosedPosition) yPosition = mClosedPosition;
        if (animate) {
            mDrawerAnimationInProgress = true;
            if (doDimOnDrawerSlide)
                ViewCompat.animate(mMainView).alpha(computeOverlayAlphaForPosition(yPosition));
            ViewCompat.animate(mDrawerView)
                    .translationY(yPosition)
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                            mDrawerAnimationInProgress = true;
                            switchDrawerState(STATE_DRAGGING);
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            mDrawerAnimationInProgress = false;
                            switchDrawerStateWithPosition(ViewCompat.getTranslationY(mDrawerView));
                        }

                        @Override
                        public void onAnimationCancel(View view) {
                            mDrawerAnimationInProgress = false;
                            switchDrawerStateWithPosition(ViewCompat.getTranslationY(mDrawerView));
                        }
                    })
                    .setUpdateListener(new ViewPropertyAnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(View view) {
                            if (mDrawerSlideListener != null) {
                                float translation = ViewCompat.getTranslationY(mDrawerView) - mOpenedPosition;
                                float endPosition = mClosedPosition - mOpenedPosition;
                                mDrawerSlideListener.onDrawerSlide(mDrawerView, translation / endPosition);
                            }
                        }
                    });
        } else {
            ViewCompat.setTranslationY(mDrawerView, yPosition);
            if (mDrawerSlideListener != null) {
                float translation = yPosition - mOpenedPosition;
                float endPosition = mClosedPosition - mOpenedPosition;
                mDrawerSlideListener.onDrawerSlide(mDrawerView, translation / endPosition);
            }

            // dim main view while dragging
            if (doDimOnDrawerSlide)
                ViewCompat.setAlpha(mMainView, computeOverlayAlphaForPosition(yPosition));

            switchDrawerStateWithPosition(yPosition);
        }
    }

    private float computeOverlayAlphaForPosition(float yPosition) {
        return yPosition / mClosedPosition;
    }

    private void runOnGlobalLayoutDelayedRunnable() {
        if (onGlobalLayoutDelayedRunnable != null) {
            onGlobalLayoutDelayedRunnable.run();
            onGlobalLayoutDelayedRunnable = null;
        }
    }

    private void ensureOnGlobalLayoutAndRun(Runnable runnable) {
        if (runnable == null) return;

        if (mClosedPosition == 0) {
            onGlobalLayoutDelayedRunnable = runnable;
        } else {
            runnable.run();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // check if we have a correct amount of children in this ViewGroup
        if (getChildCount() != 2) {
            throw new IllegalStateException("VerticalDrawerLayout should have 2 child views. Not more and not less.");
        }

        mMainView = getChildAt(0);
        mDrawerView = getChildAt(1);

        if (!isDrawerOpenedOnStart || mDrawerState == STATE_CLOSED) {
            mDrawerView.setVisibility(View.GONE);
        } else {
            ViewCompat.setTranslationY(mDrawerView, mOpenedPosition);
            mDrawerView.setVisibility(View.VISIBLE);
        }
    }

    // All touch events go through this method first
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean validScrollingEvent = event.getPointerId(event.getActionIndex()) == SCROLL_POINTER_ID;
        int action = event.getAction();

        if (validScrollingEvent) {
            mCurrentTouch.x = event.getX();
            mCurrentTouch.y = event.getY();
        }

        boolean superDispatch = super.dispatchTouchEvent(event);

        if (validScrollingEvent &&
                (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_MOVE)) {
            mLastTouch.x = mCurrentTouch.x;
            mLastTouch.y = mCurrentTouch.y;
        }

        return superDispatch;
    }

    // This method is called before passing touch events further to ViewGroup children
    // If returns true - children won't receive any touch events until ACTION_UP or ACTION_CANCEL occurs
    // It will not we called again until ACTION_UP or ACTION_CANCEL once returned true
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isDrawerEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                //Reset the velocity tracker
                mVelocityTracker.clear();
                mVelocityTracker.addMovement(event);
                mVerticalScrollingInProgress = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float touchOffset = mCurrentTouch.y - mLastTouch.y;
                if (mTouchSlop < Math.abs(touchOffset)) {
                    boolean isMovingUp = touchOffset < 0;
                    boolean wasDrawerOpened = mDrawerState == STATE_OPENED ? true : mDrawerState != STATE_CLOSED ? mDrawerOldState == STATE_OPENED : false;
                    if (isMovingUp) {
                        boolean canDrawerIntercept = wasDrawerOpened ? true : false;
                        mVerticalScrollingInProgress = isVerticalScrolling() && !canDrawerIntercept;
                    } else {
                        boolean canDrawerIntercept = wasDrawerOpened ? canDrawerScrollUp() : false;
                        mVerticalScrollingInProgress = isVerticalScrolling() && !canDrawerIntercept;
                    }
                }
                break;
        }

        boolean superIntercept = super.onInterceptTouchEvent(event);

        return mVerticalScrollingInProgress || superIntercept;
    }

    // This method is called if none of the children returned true in their onTouchEvent
    // or if touch was intercepted by this ViewGroup
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDrawerEnabled) {
            mVelocityTracker.addMovement(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // We've already stored the initial point,
                    // but if we got here a child view didn't capture
                    // the event, so we need to.
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (mVerticalScrollingInProgress) {
                        mVelocityTracker.addMovement(event);

                        float touchOffset = mCurrentTouch.y - mLastTouch.y;
                        float currentDrawerPosition = ViewCompat.getTranslationY(mDrawerView);
                        if (touchOffset < 0) {
                            touchOffset = currentDrawerPosition > mOpenedPosition ? touchOffset : 0;
                        } else {
                            touchOffset = currentDrawerPosition < mClosedPosition ? touchOffset : 0;
                        }
                        translateDrawerTo(currentDrawerPosition + touchOffset, false);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    finishDrag();
                    mVerticalScrollingInProgress = false;
                    break;
            }
        }

        return super.onTouchEvent(event);
    }

    private void finishDrag() {
        float actionUpPosition = ViewCompat.getTranslationY(mDrawerView);
        float middlePosition = (mClosedPosition - mOpenedPosition) * 0.75f;

        mVelocityTracker.computeCurrentVelocity(1000);
        float velocity = mVelocityTracker.getYVelocity();
        boolean isAboutToOpen = velocity < 0 && mDrawerOldState == STATE_CLOSED;
        boolean isAboutToClose = velocity > 0 && mDrawerOldState == STATE_OPENED;

        if (Math.abs(velocity) > mFlingSlop && (isAboutToOpen || isAboutToClose)) {
            if (mDrawerOldState == STATE_CLOSED) {
                openDrawer(true);
            } else {
                closeDrawer(true);
            }
        } else {
            if (actionUpPosition < middlePosition) {
                openDrawer(true);
            } else {
                closeDrawer(true);
            }
        }
    }

    private boolean isVerticalScrolling() {
        float deltaX = Math.abs(mLastTouch.x - mCurrentTouch.x);
        float deltaY = Math.abs(mLastTouch.y - mCurrentTouch.y);
        if (deltaY > deltaX) {
            return true;
        }
        return false;
    }

    private boolean canDrawerScrollUp() {
        return canDrawerScrollVertically(-1);
    }

    private boolean canDrawerScrollDown() {
        return canDrawerScrollVertically(1);
    }

    private boolean canDrawerScrollVertically(int direction) {
        if (mDrawerView instanceof DrawerScrollable) {
            DrawerScrollable scrollableView = (DrawerScrollable) mDrawerView;
            if (scrollableView.getScrollableView() != null) {
                return ViewCompat.canScrollVertically(scrollableView.getScrollableView(), direction);
            }
        }

        return ViewCompat.canScrollVertically(mDrawerView, direction);
    }
}
