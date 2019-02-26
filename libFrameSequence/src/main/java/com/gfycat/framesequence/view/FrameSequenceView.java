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

package com.gfycat.framesequence.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.gfycat.common.ContextDetails;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Sugar;
import com.gfycat.framesequence.FrameSequenceDrawable;
import com.gfycat.framesequence.R;

import java.io.InterruptedIOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Displays a Gfycat using animated image as a source.
 * <p>
 * This is an {@link android.widget.ImageView} that displays animated images frame-by-frame once {@link FrameSequenceView#setup(FrameSequenceSource)} is called.
 * <p>
 * Note that you MUST call {@link FrameSequenceView#release()} in order to release resources and avoid memory leak.
 */
public class FrameSequenceView extends android.support.v7.widget.AppCompatImageView {

    private static final String LOG_TAG = "FrameSequenceView";
    private static final int FADE_DURATION_MS = 200;
    private static final int MAX_START_LOADING_DELAY = 200;
    private static final int MIN_START_LOADING_DELAY = 50;
    private static final int MAX_COLOR_CHANNEL_VALUE = 255;

    private static final Random random = new Random();

    private FrameSequenceSource currentSource;

    private ContextDetails contextDetails = new ContextDetails();

    private GfycatTransitionDrawable gfycatTransitionDrawable;
    private FrameSequenceDrawable frameSequenceDrawable;

    private Disposable frameSequenceDisposable;

    private SingleNotificationListener onStartAnimationListener = new SingleNotificationListener();

    /**
     * Changed directly by call play / pause
     */
    private boolean shouldPlay;

    private boolean shouldLoadPreview;

    private Disposable previewSubscription;
    private boolean attached;

    public FrameSequenceView(Context context) {
        super(context);
    }

    public FrameSequenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        internalAttrsInit(context, attrs);
    }

    public FrameSequenceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        internalAttrsInit(context, attrs);
    }

    private void internalAttrsInit(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FrameSequenceView);
        this.shouldLoadPreview = a.getBoolean(R.styleable.FrameSequenceView_shouldLoadPreview, false);
        this.shouldPlay = a.getBoolean(R.styleable.FrameSequenceView_autoplay, false);
        a.recycle();
    }


    public void setShouldLoadPreview(boolean shouldLoadPreview) {
        this.shouldLoadPreview = shouldLoadPreview;
    }

    /**
     * Set a {@link FrameSequenceSource} object to display by {@link FrameSequenceView}.
     */
    protected void setup(@NonNull final FrameSequenceSource frameSequenceSource) {

        Assertions.assertUIThread(IllegalAccessException::new);
        this.contextDetails = frameSequenceSource.getContextDetails().copy();

        Logging.d(LOG_TAG, "setupGfycat() ", contextDetails);

        if (currentSource != null) internalRelease();
        this.currentSource = frameSequenceSource;

        if (attached) {
            startDataLoading();
        }
    }

    private void startDataLoading() {

        Logging.d(LOG_TAG, "startDataLoading() ", contextDetails);

        setImageDrawable(gfycatTransitionDrawable = new GfycatTransitionDrawable(new SizedColorDrawable(currentSource.getAverageColorInt(), currentSource.getWidth(), currentSource.getHeight())));
        gfycatTransitionDrawable.setContextDetails(contextDetails);

        FrameSequenceSource source = currentSource;

        if (shouldLoadPreview) {
            previewSubscription = source.loadPoster()
                    .delaySubscription(randomDelayTime(), TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            drawable -> onPreviewReceived(drawable, !hasTransparency()),
                            source::failedToGetPoster);
        }

        if (shouldPlay) {
            playInternal();
        }
    }

    private void restartIfPossible() {
        Logging.d(LOG_TAG, "restartIfPossible() ", currentSource != null, " ", contextDetails);
        if (currentSource != null) {
            startDataLoading();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        Logging.d(LOG_TAG, "onAttachedToWindow() ", contextDetails);
        attached = true;
        super.onAttachedToWindow();
        restartIfPossible();
    }

    @Override
    protected void onDetachedFromWindow() {
        Logging.d(LOG_TAG, "onDetachedFromWindow() ", contextDetails);
        attached = false;
        super.onDetachedFromWindow();
        internalRelease();
    }

    private void releasePreview() {
        Sugar.doIfNotNull(previewSubscription, Disposable::dispose);
        previewSubscription = null;
    }

    /**
     * You MUST call this method when @{link {@link FrameSequenceView} is no longer needed in order to release resources and avoid memory leak.
     * <p>
     * Note: We are working on automatic resource release encapsulation. This method will be marked as Deprecated when done.
     */
    public void release() {
        internalRelease();
    }

    private void internalRelease() {
        Logging.d(LOG_TAG, "internalRelease() ", contextDetails);
        releasePreview();
        Sugar.doIfNotNull(frameSequenceDisposable, Disposable::dispose);
        frameSequenceDisposable = null;
        if (frameSequenceDrawable != null) {
            frameSequenceDrawable.stop();
            frameSequenceDrawable.destroy();
            frameSequenceDrawable = null;
        }
        setImageDrawable(null);
    }

    /**
     * Set preview manually by calling this method.
     * <p>
     * Note: If {@link #setShouldLoadPreview(boolean)} is set to true then preview image will be replaced by the one loaded from {@link FrameSequenceSource#loadPoster()}.
     *
     * @param drawable to use as preview, until {@link FrameSequenceSource#loadFrameSequence()} is being loaded.
     * @param animate  set to true if transition animation needed.
     */
    public void setupPreview(Drawable drawable, boolean animate) {
        onPreviewReceived(drawable, animate);
    }

    private void changeDrawable(Drawable drawable, boolean animation) {
        if (animation) {
            gfycatTransitionDrawable.addDrawable(drawable, FADE_DURATION_MS);
        } else {
            gfycatTransitionDrawable.setDrawable(drawable);
        }
    }

    private void onFrameSequenceLoaded(FrameSequenceDrawable loadedFrameSequence) {
        Logging.d(LOG_TAG, "onFrameSequenceLoaded() webp load = ", FrameSequenceMemoryUsage.getWebPMemoryUsage(), " should play = ", shouldPlay, " ", contextDetails);

        releasePreview();
        frameSequenceDrawable = loadedFrameSequence;

        boolean animate = !hasTransparency();
        changeDrawable(frameSequenceDrawable, animate);

        frameSequenceDrawable.setLoopListener(FrameSequenceView.this::onLoop);
        if (shouldPlay()) {
            frameSequenceDrawable.start();
            onStartAnimationListener.onStart();
        }
    }

    private void onPreviewReceived(Drawable preview, boolean animation) {
        Logging.d(LOG_TAG, "onPreviewReceived() ", animation, " frameSequenceDrawable = ", frameSequenceDrawable, " ", contextDetails);
        if (frameSequenceDrawable == null) {
            changeDrawable(preview, animation);
        }
    }

    private void onLoop(int count) {
        // we may use it in future.
    }

    private boolean shouldPlay() {
        return shouldPlay;
    }

    /**
     * Subscribe for animation start. Will be called when frame sequence has loaded and animation starts.
     *
     * @param onStartAnimationListener
     */
    public void setOnStartAnimationListener(OnStartAnimationListener onStartAnimationListener) {
        this.onStartAnimationListener.setRealListener(onStartAnimationListener);
    }

    /**
     * Start WebP animation playback.
     * <p>
     * Will download and play animated {@link FrameSequenceSource#loadFrameSequence()}, or play it instantly if it is already downloaded.
     */
    public void play() {
        Logging.d(LOG_TAG, "play() ", contextDetails);
        shouldPlay = true;
        if (attached) {
            playInternal();
        }
    }

    /**
     * Pause animation playback.
     */
    public void pause() {
        Logging.d(LOG_TAG, "pause() ", contextDetails);
        shouldPlay = false;
        pauseInternal();
    }

    private void pauseInternal() {
        Sugar.doIfNotNull(frameSequenceDrawable, FrameSequenceDrawable::stop);
    }

    private void playInternal() {
        Logging.d(LOG_TAG, "playInternal() ", contextDetails);
        if (frameSequenceDrawable != null) {
            frameSequenceDrawable.start();
            onStartAnimationListener.onStart();
        }

        FrameSequenceSource source = currentSource;

        if (frameSequenceDisposable == null && currentSource != null) {
            frameSequenceDisposable = source.loadFrameSequence()
                    .delay(randomDelayTime(), TimeUnit.MILLISECONDS)
                    .map(sequence -> new FrameSequenceDrawable(sequence, source.getDropFramesStrategy(), source.getContextDetails()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .retry((integer, throwable) -> throwable instanceof InterruptedIOException && frameSequenceDisposable != null && !frameSequenceDisposable.isDisposed())
                    .subscribe(
                            this::onFrameSequenceLoaded,
                            source::failedToGetFrameSequence);
        }
    }

    private boolean hasTransparency() {
        return Color.alpha(currentSource.getAverageColorInt()) < MAX_COLOR_CHANNEL_VALUE;
    }

    private long randomDelayTime() {
        return MIN_START_LOADING_DELAY + random.nextInt(MAX_START_LOADING_DELAY);
    }

    private class SingleNotificationListener implements OnStartAnimationListener {

        private OnStartAnimationListener real;
        private boolean notified = false;

        public void setRealListener(OnStartAnimationListener real) {
            this.real = real;
        }

        @Override
        public void onStart() {
            if (real != null && !notified) {
                notified = true;
                real.onStart();
            }
        }
    }
}
