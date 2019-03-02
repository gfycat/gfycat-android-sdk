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
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;

import java.util.Iterator;
import java.util.List;

/**
 * Adapter that will group other adapter.
 * <p/>
 * Created by dekalo on 03.09.15.
 */
public class GroupAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String LOG_TAG = "GroupAdapter";

    private static final int STEP = 500;

    private final Fifo<String> changeLog = new Fifo();

    private RecyclerView.Adapter<ViewHolder>[] adapters;
    private String[] adapterNames;
    private int[] sizes;

    private static String[] buildAdapterNames(RecyclerView.Adapter[] adapters) {
        String[] adapterNames = new String[adapters.length];
        for (int i = 0; i < adapterNames.length; i++) {
            adapterNames[i] = adapters[i].getClass().getSimpleName();
        }
        return adapterNames;
    }

    /**
     * Mentioned adapter should have view types not more than 100.
     */
    public GroupAdapter(RecyclerView.Adapter<ViewHolder>[] adapters, String[] adapterNames) {
        this.adapters = adapters;
        this.adapterNames = adapterNames;
        syncSizes();
        registerObserver();
        if (adapterNames.length != adapters.length)
            throw new IllegalStateException("adapterNames.length != adapters.length");
    }

    public GroupAdapter(RecyclerView.Adapter... adapters) {
        this(adapters, buildAdapterNames(adapters));
    }

    private void syncSizes() {
        sizes = new int[adapters.length];
        for (int i = 0; i < adapters.length; i++) {
            sizes[i] = adapters[i].getItemCount();
        }
    }

    private void registerObserver() {
        for (int i = 0; i < adapters.length; i++) {
            adapters[i].registerAdapterDataObserver(new LocalObserver(i));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int absolutePosition) {
        int[] indexAndPosition = getAdapterIndexAndRelativePosition(absolutePosition);
        adapters[indexAndPosition[0]].onBindViewHolder(holder, indexAndPosition[1]);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        // happens with recyclerview 25.4.0 and above
        // https://issuetracker.google.com/u/1/issues/64112268
        try {
            int[] indexAndPosition = getAdapterIndexAndRelativePosition(holder.getAdapterPosition());
            adapters[indexAndPosition[0]].onViewRecycled(holder);
        } catch (WrongGroupAdapterIndexAndRelativePositionException e) {
            int adapterPosition = getAdapterIndexFromViewType(holder.getItemViewType());

            if (adapterPosition >= adapters.length) {
                Assertions.fail(new IllegalStateException("Can not use viewType to get adapterPosition."));
            } else {
                adapters[adapterPosition].onViewRecycled(holder);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        // http://crashes.to/s/4fb2c035383

        try {
            int[] indexAndPosition = getAdapterIndexAndRelativePosition(holder.getAdapterPosition());
            adapters[indexAndPosition[0]].onViewDetachedFromWindow(holder);
        } catch (WrongGroupAdapterIndexAndRelativePositionException e) {
            int adapterPosition = getAdapterIndexFromViewType(holder.getItemViewType());

            if (adapterPosition >= adapters.length) {
                Assertions.fail(new IllegalStateException("Can not use viewType to get adapterPosition."));
            } else {
                adapters[adapterPosition].onViewDetachedFromWindow(holder);
            }
        }
    }

    @Override
    public boolean onFailedToRecycleView(ViewHolder holder) {
        int[] indexAndPosition = getAdapterIndexAndRelativePosition(holder.getAdapterPosition());
        return adapters[indexAndPosition[0]].onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        int[] indexAndPosition = getAdapterIndexAndRelativePosition(holder.getAdapterPosition());
        adapters[indexAndPosition[0]].onViewAttachedToWindow(holder);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        for (RecyclerView.Adapter adapter : adapters) {
            adapter.onAttachedToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        int[] indexAndPosition = getAdapterIndexAndRelativePosition(holder.getAdapterPosition());
        adapters[indexAndPosition[0]].onBindViewHolder(holder, indexAndPosition[1]);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        for (RecyclerView.Adapter adapter : adapters) {
            adapter.onDetachedFromRecyclerView(recyclerView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int relativeViewType = viewType % STEP;
        int adapterIndex = getAdapterIndexFromViewType(viewType);
        return adapters[adapterIndex].onCreateViewHolder(parent, relativeViewType);
    }

    @Override
    public int getItemViewType(int absolutePosition) {
        int[] indexAndPosition = getAdapterIndexAndRelativePosition(absolutePosition);
        return relativeViewTypeToAbsolute(indexAndPosition[0], adapters[indexAndPosition[0]].getItemViewType(indexAndPosition[1]));
    }

    public int getRelativeItemViewType(int absolutePosition) {
        int[] indexAndPosition = getAdapterIndexAndRelativePosition(absolutePosition);
        return adapters[indexAndPosition[0]].getItemViewType(indexAndPosition[1]);
    }

    private int relativeViewTypeToAbsolute(int adapterIndex, int relativeViewType) {
        if (relativeViewType >= STEP || relativeViewType < 0) {
            Assertions.fail(new IllegalStateException("GroupAdapter::relativeViewTypeToAbsolute(" + adapterIndex + ", " + relativeViewType + ")"));
        }
        return STEP * adapterIndex + relativeViewType;
    }

    private int getAdapterIndexFromViewType(int viewType) {
        return viewType / STEP;
    }

    private int[] getAdapterIndexAndRelativePosition(int absolutePosition) {
        int relativePosition = absolutePosition;
        for (int i = 0; i < adapters.length; i++) {
            RecyclerView.Adapter adapter = adapters[i];
            if (adapter.getItemCount() > relativePosition) {
                return new int[]{i, relativePosition};
            } else {
                relativePosition -= adapter.getItemCount();
            }
        }

        reportWrongIndexAndRelativePosition(absolutePosition);

        throw new WrongGroupAdapterIndexAndRelativePositionException();
    }

    private void reportWrongIndexAndRelativePosition(int absolutePosition) {
        if (changeLog.isEmpty()) {
            Logging.c(LOG_TAG, "changelog is empty");
        } else {

            int i = 1;
            Iterator<String> iterator = changeLog.iterator();

            while (iterator.hasNext()) {
                Logging.c(LOG_TAG, i++ + ":" + iterator.next());
            }
        }

        Logging.c(LOG_TAG, "Unreachable " + absolutePosition + " absolute position. " + collectAdaptersStateInfo("report"));
    }

    private int getAbsolutePosition(int adapterIndex, int relativePosition) {
        int absolutePosition = relativePosition;
        for (int i = 0; i < adapterIndex; i++) {
            absolutePosition += adapters[i].getItemCount();
        }
        return absolutePosition;
    }

    @Override
    public int getItemCount() {
        int result = 0;
        for (RecyclerView.Adapter adapter : adapters) {
            result += adapter.getItemCount();
        }
        return result;
    }

    private String getAdapterName(int index) {
        if (adapterNames == null)
            return adapters[index].getClass().getSimpleName();
        else
            return adapterNames[index];
    }

    public String collectAdaptersStateInfo(String source) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(source).append(" -> ");

        if (adapters.length == 0) {
            stringBuilder.append("No adapters provided.");
        }

        for (int i = 0; i < adapters.length; i++) {
            stringBuilder
                    .append(getAdapterName(i)).append(" ")
                    .append(adapters[i].getItemCount()).append(" ")
                    .append(sizes[i]);

            if (i < adapters.length - 1) stringBuilder.append(" | ");
        }

        return stringBuilder.toString();
    }

    private class LocalObserver extends RecyclerView.AdapterDataObserver {
        private int index;

        public LocalObserver(int index) {
            this.index = index;
        }

        @Override
        public void onChanged() {
            syncSizes();
            changeLog.push(collectAdaptersStateInfo(getAdapterName(index) + "::changed()"));
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            sizes[index] -= itemCount;
            changeLog.push(collectAdaptersStateInfo(getAdapterName(index) + "::rangeRemoved() " + positionStart + " " + itemCount));
            notifyItemRangeRemoved(getAbsolutePosition(index, positionStart), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemMoved(getAbsolutePosition(index, fromPosition), getAbsolutePosition(index, toPosition));
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            changeLog.push(collectAdaptersStateInfo(getAdapterName(index) + "::rangeChanged() " + positionStart + " " + itemCount));
            notifyItemRangeChanged(getAbsolutePosition(index, positionStart), itemCount, payload);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            changeLog.push(collectAdaptersStateInfo(getAdapterName(index) + "::rangeChanged() " + positionStart + " " + itemCount));
            notifyItemRangeChanged(getAbsolutePosition(index, positionStart), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            sizes[index] += itemCount;
            changeLog.push(collectAdaptersStateInfo(getAdapterName(index) + "::rangeInserted() " + positionStart + " " + itemCount));
            notifyItemRangeInserted(getAbsolutePosition(index, positionStart), itemCount);
        }
    }

    private class WrongGroupAdapterIndexAndRelativePositionException extends RuntimeException {
        public WrongGroupAdapterIndexAndRelativePositionException(String message) {
            super(message);
        }

        public WrongGroupAdapterIndexAndRelativePositionException() {
        }
    }
}
