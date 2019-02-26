/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gfycat.framesequence;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.gfycat.common.ContextDetails;
import com.gfycat.common.EPS;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Utils;

import java.nio.ByteBuffer;

public class FrameSequenceDrawable extends Drawable implements Animatable {

    private static final String LOG_TAG = FrameSequenceDrawable.class.getSimpleName();

    /**
     * These constants are chosen to imitate common browser behavior for WebP/GIF.
     * If other decoders are added, this behavior should be moved into the WebP/GIF decoders.
     * <p>
     * Note that 0 delay is undefined behavior in the GIF standard.
     */
    private static final long MIN_DELAY_MS = 16;
    private static final long DEFAULT_DELAY_MS = 100;

    private static Handler uiHandler;


    private SerialExecutorService decodingExecutor;
    private LoopListener loopListener;

    public interface LoopListener {
        void onLoop(int count);
    }

    interface BitmapProvider {
        /**
         * Called by FrameSequenceDrawable to aquire an 8888 Bitmap with minimum dimensions.
         */
        Bitmap acquireBitmap(int minWidth, int minHeight);

        /**
         * Called by FrameSequenceDrawable to release a Bitmap it no longer needs. The Bitmap
         * will no longer be used at all by the drawable, so it is safe to reuse elsewhere.
         * <p>
         * This method may be called by FrameSequenceDrawable on any thread.
         */
        void releaseBitmap(Bitmap bitmap);
    }

    private static BitmapProvider sAllocatingBitmapProvider = new BitmapProvider() {
        @Override
        public Bitmap acquireBitmap(int minWidth, int minHeight) {
            return Bitmap.createBitmap(minWidth, minHeight, Bitmap.Config.ARGB_8888);
        }

        @Override
        public void releaseBitmap(Bitmap bitmap) {
        }
    };

    private Paint debugPaint;

    private final FrameSequence mFrameSequence;
    private final DropFramesStrategy mDropFramesStrategy;

    private final Paint mPaint;
    private BitmapShader mFrontBitmapShader;
    private BitmapShader mBackBitmapShader;
    private final Rect mSrcRect;
    private boolean mCircleMaskEnabled;

    //Protects the fields below
    private final Object mLock = new Object();

    private final BitmapProvider mBitmapProvider;
    private ContextDetails contextDetails;
    private volatile boolean mDestroyed = false;
    private Bitmap mFrontBitmap;
    private Bitmap mBackBitmap;
    private ByteBuffer bitmapBuffer;

    private static final int STATE_SCHEDULED = 1;
    private static final int STATE_DECODING = 2;
    private static final int STATE_WAITING_TO_SWAP = 3;
    private static final int STATE_READY_TO_SWAP = 4;

    private int mState;
    private long mCurrentLoop;

    private long mLastSwap;
    private long mNextSwap;
    private long mSwapDelay;
    private int mPreviousRenderedFrame;
    private int mNextFrameToDecode;

    private Runnable invalidateRequest;
    /**
     * Runs on decoding thread, only modifies mBackBitmap's pixels
     */
    private Runnable mDecodeRunnable = new Runnable() {
        @Override
        public void run() {
            int nextFrame;
            Bitmap bitmap;
            synchronized (mLock) {
                if (mDestroyed) return;

                nextFrame = mNextFrameToDecode;
                if (nextFrame < 0) {
                    return;
                }
                bitmap = mBackBitmap;
                mState = STATE_DECODING;
            }


            long timeToNextFrame = 0;

            /**
             * Check do we need to skip some frames.
             *
             * On first draw we should not skip any frames.
             * mLastSwap > 0 means that it is not first draw.
             */
            if (mLastSwap > 0) {
                int newNextFrame = computeNextFrame(nextFrame);

                timeToNextFrame = cumulativeFrameDuration(nextFrame, newNextFrame);
                nextFrame = newNextFrame;
            }

            mFrameSequence.drawFrame(nextFrame, bitmap);

            if (timeToNextFrame < MIN_DELAY_MS) {
                timeToNextFrame = DEFAULT_DELAY_MS;
            }

            boolean schedule = false;
            Bitmap bitmapToRelease = null;
            synchronized (mLock) {
                if (mDestroyed) {
                    bitmapToRelease = mBackBitmap;
                    mBackBitmap = null;
                } else if (mNextFrameToDecode >= 0 && mState == STATE_DECODING) {
                    schedule = true;

                    mNextSwap = timeToNextFrame + mLastSwap;
                    mNextFrameToDecode = nextFrame; // could be different if we decided to skip some
                    if (FrameSequenceConfiguration.loggingEnabled())
                        Log.d(LOG_TAG, "decode() " +
                                "state:" + stateName(mState) + " " +
                                "nextSwap at " + Utils.humanReadableTimeSmall(mNextSwap) + " " +
                                "nextFrame:" + mNextFrameToDecode + " " +
                                "timeToNextFrame:" + Utils.humanReadableTimeInterval(timeToNextFrame) + " " +
                                " callback = " + getCallback() +
                                "" + contextDetails);

                    mState = STATE_WAITING_TO_SWAP;
                }
            }
            if (schedule) {
                if (FrameSequenceConfiguration.loggingEnabled())
                    Log.d(LOG_TAG, "   nextSwap " + Utils.humanReadableTimeSmall(mNextSwap) + " in " + Utils.humanReadableTimeInterval(mNextSwap - SystemClock.uptimeMillis()) + " " + contextDetails);
                requestInvalidateThreadSafe();
            }
            if (bitmapToRelease != null) {
                // destroy the bitmap here, since there's no safe way to get back to
                // drawable thread - drawable is likely detached, so schedule is noop.
                mBitmapProvider.releaseBitmap(bitmapToRelease);
            }
        }
    };

    /**
     * Returns nextFrame
     */
    private int computeNextFrame(int nextFrame) {

        // Frame drop not allowed, just render next frame.
        if (DropFramesStrategy.DropNotAllowed.equals(mDropFramesStrategy)) return nextFrame;

        // Find desired frame with frame drop.
        int desiredFrame = findDesiredNextFrame(nextFrame);

        // If desired frame is same as next no framedrop is necessary.
        if (desiredFrame == nextFrame) return nextFrame;
        // If we jump to next loop we can skip frames at the end.
        if (desiredFrame < nextFrame) return desiredFrame;

        // Drop frame is allowed, os we should just pick desired frame.
        if (DropFramesStrategy.DropAllowed.equals(mDropFramesStrategy)) return desiredFrame;

        // Trying to find keyframes in droprange
        int lastKeyFrameInRange = mFrameSequence.lastKeyFrameInRange(nextFrame, desiredFrame);

        if (DropFramesStrategy.KeyFrameOrDropNotAllowed.equals(mDropFramesStrategy)) {
            if (lastKeyFrameInRange == -1) {
                // no key frames -> rendering next one
                return nextFrame;
            } else {
                // jump to keyframe
                return lastKeyFrameInRange;
            }
        }

        if (DropFramesStrategy.KeyFrameOrDropAllowed.equals(mDropFramesStrategy)) {
            if (lastKeyFrameInRange == -1) {
                // no key frames -> rendering desired one
                return desiredFrame;
            } else {
                // jump to keyframe
                return lastKeyFrameInRange;
            }
        }

        throw new IllegalStateException("Not reachable.");
    }

    private long cumulativeFrameDuration(int start, int end) {

        long result = mFrameSequence.getFrameDuration(start);

        while (start != end) {
            start = (start + 1) % mFrameSequence.getFrameCount();
            result += mFrameSequence.getFrameDuration(start);
        }

        return result;
    }

    private int findDesiredNextFrame(int nextFrame) {

        long nextFrameDuration = mFrameSequence.getFrameDuration(nextFrame);

        long predictedNextFrameTime = mLastSwap + nextFrameDuration - SystemClock.uptimeMillis();
        int droppedFrames = 0;

        while ((nextFrame != 0 || canDropFirstFrame()) && predictedNextFrameTime < FrameSequenceConfiguration.get().getMinTimeToRenderNextFrame()) {
            // drop one frame
            droppedFrames++;
            nextFrame = (nextFrame + 1) % mFrameSequence.getFrameCount();
            nextFrameDuration += mFrameSequence.getFrameDuration(nextFrame);
            predictedNextFrameTime = mLastSwap + nextFrameDuration - SystemClock.uptimeMillis();
            if (mDestroyed) return nextFrame;
        }
        if (FrameSequenceConfiguration.loggingEnabled() && droppedFrames > 0)
            Log.d(LOG_TAG, "drop " + droppedFrames + " frames " + contextDetails);

        return nextFrame;
    }

    private boolean canDropFirstFrame() {
        return false;
    }

    private void initUIHandler() {
        if (uiHandler == null) {
            uiHandler = new Handler();
        }
    }

    private void requestInvalidateThreadSafe() {
        Runnable runnable = () -> scheduleSelf(invalidateRequest = this::requestInvalidate, mNextSwap);
        if (uiHandler == null) runnable.run();
        else uiHandler.post(runnable);
    }

    private static Bitmap acquireAndValidateBitmap(BitmapProvider bitmapProvider,
                                                   int minWidth, int minHeight) {
        Bitmap bitmap = bitmapProvider.acquireBitmap(minWidth, minHeight);

        if (bitmap.getWidth() < minWidth
                || bitmap.getHeight() < minHeight
                || bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Invalid bitmap provided");
        }

        return bitmap;
    }

    public FrameSequenceDrawable(FrameSequence frameSequence, DropFramesStrategy dropFramesStrategy, ContextDetails contextDetails) {
        this(frameSequence, dropFramesStrategy, sAllocatingBitmapProvider, new Point(Integer.MAX_VALUE, Integer.MAX_VALUE), contextDetails);
    }

    public FrameSequenceDrawable(FrameSequence frameSequence, DropFramesStrategy dropFramesStrategy, Point renderSize, ContextDetails contextDetails) {
        this(frameSequence, dropFramesStrategy, sAllocatingBitmapProvider, renderSize, contextDetails);
    }

    public FrameSequenceDrawable(FrameSequence frameSequence, DropFramesStrategy dropFramesStrategy, BitmapProvider bitmapProvider, Point renderSize, ContextDetails contextDetails) {
        if (frameSequence == null || bitmapProvider == null) throw new IllegalArgumentException();

        if (FrameSequenceConfiguration.loggingEnabled())
            Log.d(LOG_TAG, "FrameSequenceDrawable::<init> " + contextDetails);

        this.contextDetails = contextDetails;
        mFrameSequence = frameSequence;
        mDropFramesStrategy = dropFramesStrategy;
        normalizeRendererSize(renderSize, frameSequence.getWidth(), frameSequence.getHeight());

        final int width = Math.min(frameSequence.getWidth(), renderSize.x);
        final int height = Math.min(frameSequence.getHeight(), renderSize.y);

        mBitmapProvider = bitmapProvider;
        mFrontBitmap = acquireAndValidateBitmap(bitmapProvider, width, height);
        mBackBitmap = acquireAndValidateBitmap(bitmapProvider, width, height);

        if (frameSequence.mayHaveBlending())
            bitmapBuffer = ByteBuffer.allocate(mFrontBitmap.getHeight() * mFrontBitmap.getRowBytes());

        mSrcRect = new Rect(0, 0, width, height);
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);

        mFrontBitmapShader
                = new BitmapShader(mFrontBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBackBitmapShader
                = new BitmapShader(mBackBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mLastSwap = 0;

        mNextFrameToDecode = -1;
        mFrameSequence.drawFrame(0, mFrontBitmap);
        initializeDecodingThread();
    }

    private void normalizeRendererSize(Point renderSize, int frameWidth, int frameHeight) {
        if (renderSize.x == Integer.MAX_VALUE || renderSize.y == Integer.MAX_VALUE) {
            renderSize.x = frameWidth;
            renderSize.y = frameHeight;
            return;
        }

        final float frameAR = frameWidth / (float) frameHeight;
        final float renderAR = renderSize.x / (float) renderSize.y;
        if (!EPS.isSame(frameAR, renderAR, 0.1f)) {
            Assertions.fail(new Throwable("Aspect ratios are not equal: " + frameAR + "!=" + renderAR + "\nContext: " + contextDetails));

            renderSize.x = frameWidth;
            renderSize.y = frameHeight;
        }
    }

    private void initializeDecodingThread() {
        decodingExecutor = new SerialExecutorService(FrameSequenceConfiguration.get().getDecodingExecutor());
    }

    public void setLoopListener(LoopListener loopListener) {
        this.loopListener = loopListener;
    }

    /**
     * Pass true to mask the shape of the animated drawing content to a circle.
     * <p>
     * <p> The masking circle will be the largest circle contained in the Drawable's bounds.
     * Masking is done with BitmapShader, incurring minimal additional draw cost.
     */
    public final void setCircleMaskEnabled(boolean circleMaskEnabled) {
        mCircleMaskEnabled = circleMaskEnabled;
        // Anti alias only necessary when using circular mask
        mPaint.setAntiAlias(circleMaskEnabled);
    }

    private void checkDestroyedLocked() {
        if (mDestroyed) {
            throw new IllegalStateException("Cannot perform operation on recycled drawable");
        }
    }

    public boolean isDestroyed() {
        synchronized (mLock) {
            return mDestroyed;
        }
    }

    /**
     * Marks the drawable as permanently recycled (and thus unusable), and releases any owned
     * Bitmaps drawable to its BitmapProvider, if attached.
     * <p>
     * If no BitmapProvider is attached to the drawable, recycle() is called on the Bitmaps.
     */
    public void destroy() {
        if (mBitmapProvider == null) {
            throw new IllegalStateException("BitmapProvider must be non-null");
        }

        Bitmap bitmapToReleaseA;
        Bitmap bitmapToReleaseB = null;
        synchronized (mLock) {
            checkDestroyedLocked();

            bitmapToReleaseA = mFrontBitmap;
            mFrontBitmap = null;

            if (mState != STATE_DECODING) {
                bitmapToReleaseB = mBackBitmap;
                mBackBitmap = null;
            }

            mDestroyed = true;
        }

        // For simplicity and safety, we don't destroy the state object here
        mBitmapProvider.releaseBitmap(bitmapToReleaseA);
        if (bitmapToReleaseB != null) {
            mBitmapProvider.releaseBitmap(bitmapToReleaseB);
        }
        mFrameSequence.release();
        loopListener = null;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            mFrameSequence.release();
        } finally {
            super.finalize();
        }
    }

    private FPSDebugger fpsDebugger = new FPSDebugger(10);

    @Override
    public void draw(Canvas canvas) {

        initUIHandler();

        synchronized (mLock) {

            if (FrameSequenceConfiguration.loggingEnabled())
                Log.d(LOG_TAG, "draw(...) state:" + stateName(mState) + " " + contextDetails);

            checkDestroyedLocked();
            if (mState == STATE_WAITING_TO_SWAP) {
                // may have failed to schedule mark ready runnable,
                // so go ahead and swap if swapping is due
                if (mNextSwap - SystemClock.uptimeMillis() <= 0) {
                    mState = STATE_READY_TO_SWAP;
                }
            }

            if (isRunning() && mState == STATE_READY_TO_SWAP) {
                // Because draw has occurred, the view system is guaranteed to no longer hold a
                // reference to the old mFrontBitmap, so we now use it to produce the next frame
                if (mFrameSequence.mayHaveBlending()) {
                    // as we have 2 bitmaps one for presentation and one for decoding in background
                    // they may have different content and to blend content properly
                    // we should sync theirs content before sending to renderer
                    mBackBitmap.copyPixelsToBuffer(bitmapBuffer);
                    bitmapBuffer.rewind();
                    mFrontBitmap.copyPixelsFromBuffer(bitmapBuffer);
                    bitmapBuffer.rewind();
                } else {
                    Bitmap tmp = mBackBitmap;
                    mBackBitmap = mFrontBitmap;
                    mFrontBitmap = tmp;
                }

                BitmapShader tmpShader = mBackBitmapShader;
                mBackBitmapShader = mFrontBitmapShader;
                mFrontBitmapShader = tmpShader;

                if (mLastSwap > 0 && mNextSwap > 0) {
                    mSwapDelay = SystemClock.uptimeMillis() - mNextSwap;
                    if (fpsDebugger != null) fpsDebugger.addFrame(mSwapDelay);
                }

                mLastSwap = SystemClock.uptimeMillis();

                if (FrameSequenceConfiguration.loggingEnabled()) {
                    Log.d(LOG_TAG, "    swap at " + Utils.humanReadableTimeSmall(mLastSwap) + " " +
                            " state:" + stateName(mState) + " " +
                            "" + contextDetails);


                    if (mNextSwap > SystemClock.uptimeMillis() + DEFAULT_DELAY_MS) {
                        Log.d(LOG_TAG, "    swap happens to early " + Utils.humanReadableTimeInterval(mNextSwap - SystemClock.uptimeMillis()) + " " + contextDetails);
                    }
                }


                if (mPreviousRenderedFrame > mNextFrameToDecode) {
                    mCurrentLoop++;
                    notifyLoop();
                }

                mPreviousRenderedFrame = mNextFrameToDecode;

                scheduleDecodeLocked();
            }
        }

        if (mCircleMaskEnabled) {
            Rect bounds = getBounds();
            mPaint.setShader(mFrontBitmapShader);
            float width = bounds.width();
            float height = bounds.height();
            float circleRadius = (Math.min(width, height)) / 2f;
            canvas.drawCircle(width / 2f, height / 2f, circleRadius, mPaint);
        } else {
            mPaint.setShader(null);
            canvas.drawBitmap(mFrontBitmap, mSrcRect, getBounds(), mPaint);
        }


        if (debugPaint != null) {
            int padding = 10;
            String text = "" + fpsDebugger.last() + " / " + fpsDebugger.max() + " / " + fpsDebugger.avg();
            canvas.drawText(text, padding, getIntrinsicHeight() - padding, debugPaint);
        }
    }

    private static String stateName(int state) {
        if (state == STATE_DECODING) return "STATE_DECODING";
        else if (state == STATE_READY_TO_SWAP) return "READY_TO_SWAP";
        else if (state == STATE_SCHEDULED) return "SCHEDULED";
        else if (state == STATE_WAITING_TO_SWAP) return "WAITING_TO_SWAP";
        return "UNKNOWN";
    }

    private void notifyLoop() {
        if (loopListener != null) {
            loopListener.onLoop((int) mCurrentLoop);
        }
    }

    private void scheduleDecodeLocked() {
        mState = STATE_SCHEDULED;
        mNextFrameToDecode = (mNextFrameToDecode + 1) % mFrameSequence.getFrameCount();
        decodingExecutor.submit(mDecodeRunnable);
    }

    public void requestInvalidate() {
        // set ready to swap as necessary
        boolean invalidate = false;
        synchronized (mLock) {

            if (FrameSequenceConfiguration.loggingEnabled())
                Log.d(LOG_TAG, "requestInvalidate() state:" + stateName(mState) + " " + contextDetails);

            if (mNextFrameToDecode >= 0 && mState == STATE_WAITING_TO_SWAP) {
                mState = STATE_READY_TO_SWAP;
                invalidate = true;
            }
        }
        if (invalidate) {
            invalidateSelf();
        }
    }

    @Override
    public void start() {
        if (FrameSequenceConfiguration.loggingEnabled())
            Log.d(LOG_TAG, "start() " + contextDetails);
        if (!isRunning()) {
            synchronized (mLock) {
                checkDestroyedLocked();
                if (mState == STATE_SCHEDULED) return; // already scheduled
                mCurrentLoop = 0;
                scheduleDecodeLocked();
            }
        }
    }

    @Override
    public void stop() {
        if (FrameSequenceConfiguration.loggingEnabled()) Log.d(LOG_TAG, "stop() " + contextDetails);
        if (isRunning()) {
            unscheduleSelf(invalidateRequest);
        }
    }

    @Override
    public boolean isRunning() {
        synchronized (mLock) {
            return mNextFrameToDecode > -1 && !mDestroyed;
        }
    }

    @Override
    public void unscheduleSelf(Runnable what) {
        synchronized (mLock) {
            mNextFrameToDecode = -1;
            mState = 0;
        }
        super.unscheduleSelf(what);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);

        if (!visible) {
            stop();
        } else if (restart || changed) {
            stop();
            start();
        }

        return changed;
    }

    public long getLoopsCount() {
        return mCurrentLoop;
    }

    // drawing properties

    @Override
    public void setFilterBitmap(boolean filter) {
        mPaint.setFilterBitmap(filter);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getIntrinsicWidth() {
        return mFrameSequence.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mFrameSequence.getHeight();
    }

    @Override
    public int getOpacity() {
        return mFrameSequence.isOpaque() ? PixelFormat.OPAQUE : PixelFormat.TRANSPARENT;
    }

    public void setDebugMode(boolean debugMode) {
        if (debugMode) {
            debugPaint = new Paint();
            debugPaint.setColor(Color.RED);
            debugPaint.setTextSize(24);
        } else {
            debugPaint = null;
        }
    }
}