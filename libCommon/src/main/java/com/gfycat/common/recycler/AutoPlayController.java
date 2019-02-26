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

package com.gfycat.common.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gfycat.common.utils.Logging;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * Responsible to find items that should and should not play.
 * Calls item.autoPlay and item.autoPause
 * <p/>
 * Created by dekalo on 16.09.15.
 */
public class AutoPlayController extends RxScrollListener {

    private static final String LOG_TAG = AutoPlayController.class.getSimpleName();

    public static final float AUTO_PLAY_AREA_START_PADDING_RELATIVE = 0f;
    public static final float AUTO_PLAY_AREA_END_PADDING_RELATIVE = 0f;

    private Set<AutoPlayable> playingItems = new HashSet<>();
    private static final long SKIP_RECALCULATION_DURATION = 300;

    private long lastRecalculationTime;
    private boolean isEnabled = true;

    @Override
    protected Observable<RecyclerView> configure(Observable<RecyclerView> initial) {
        return initial.debounce(SKIP_RECALCULATION_DURATION, TimeUnit.MILLISECONDS);
    }

    public void setEnabled(RecyclerView recyclerView, boolean enabled) {
        if (isEnabled == enabled)
            return;

        isEnabled = enabled;
        forceUpdate(recyclerView);
    }

    public void onActionCall(RecyclerView recyclerView) {
        if (System.currentTimeMillis() < lastRecalculationTime + SKIP_RECALCULATION_DURATION)
            return;

        lastRecalculationTime = System.currentTimeMillis();

        Set<AutoPlayable> shouldPlayItems = collectShouldPlayItems(recyclerView);

        long part1 = System.currentTimeMillis() - lastRecalculationTime;

        Iterator<AutoPlayable> iterator = playingItems.iterator();
        while (iterator.hasNext()) {
            AutoPlayable next = iterator.next();
            if (!shouldPlayItems.contains(next)) {
                next.autoPause();
                iterator.remove();
            }
        }
        iterator = shouldPlayItems.iterator();
        while (iterator.hasNext()) {
            AutoPlayable next = iterator.next();
            if (!playingItems.contains(next)) {
                playingItems.add(next);
            }
        }
        for (AutoPlayable item : playingItems) {
            if (!item.isAutoPlay()) {
                item.autoPlay();
            }
        }

        long part2 = System.currentTimeMillis() - lastRecalculationTime - part1 - part1;

        playingItems = shouldPlayItems;

        Logging.d(LOG_TAG, "onScrolled() playingItems = ", playingItems.size());
        Logging.d(LOG_TAG, "onScrolled() duration = ", (System.currentTimeMillis() - lastRecalculationTime), " 1: ", part1, " 2: ", part2);
    }

    private Set<AutoPlayable> collectShouldPlayItems(RecyclerView recyclerView) {

        if (!isEnabled || !recyclerView.isAttachedToWindow())
            return new HashSet<>();

        Set<AutoPlayable> set = new HashSet<>();

        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

        int autoPlayAreaStart = (int) (recyclerView.getTop() + recyclerView.getHeight() * AUTO_PLAY_AREA_START_PADDING_RELATIVE);
        int autoPlayAreaEnd = (int) (recyclerView.getBottom() - recyclerView.getHeight() * AUTO_PLAY_AREA_END_PADDING_RELATIVE);

        int count = lm.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = lm.getChildAt(i);
            int viewStart = lm.getDecoratedTop(child);
            int viewEnd = lm.getDecoratedBottom(child);

            boolean shouldPlay = false;
            shouldPlay = shouldPlay || (recyclerView.getTop() <= viewStart && recyclerView.getBottom() >= viewEnd); // completely visible
            shouldPlay = shouldPlay || !(autoPlayAreaStart > viewEnd || autoPlayAreaEnd < viewStart); // near center;

            if (shouldPlay) {
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(child);
                if (viewHolder instanceof AutoPlayable) {
                    set.add((AutoPlayable) viewHolder);
                }
            }
        }
        return set;
    }
}
