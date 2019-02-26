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

package com.gfycat.picker.feed;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import com.gfycat.common.utils.Algorithms;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.util.List;

/**
 * Created by dekalo on 09.02.17.
 */

public abstract class GfycatDiffSupportAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    private static final String LOG_TAG = "GfycatDSAdapter";
    private List<Gfycat> gfycats;

    public GfycatDiffSupportAdapter(List<Gfycat> gfycats) {
        Assertions.assertNotNull(gfycats, NullPointerException::new);
        this.gfycats = gfycats;
    }

    /**
     * @return Returns true if range change happens, false otherwise.
     */
    public boolean changeGfycats(List<Gfycat> newGfycats) {
        Log.d(LOG_TAG, "changeGfycats(" + newGfycats.size() + ")");
        Algorithms.IsOrderedSubsetResult result = Algorithms.isOrderedSubset(gfycats, newGfycats);

        int countAtStart = result.subsetStartIndex;
        int countAtEnd = newGfycats.size() - result.subsetEndIndex;

        List<Gfycat> oldGfycats = gfycats;
        gfycats = newGfycats;

        if (result.isSubset) {
            if (countAtStart > 0) {
                Logging.d(LOG_TAG, "changeGfycats() ", oldGfycats.size(), " ", newGfycats.size(), " notifyItemRangeInserted(", 0, ", ", countAtStart, ")");
                notifyItemRangeInserted(0, countAtStart);
                return true;
            }
            if (countAtEnd > 0) {
                Logging.d(LOG_TAG, "changeGfycats() ", oldGfycats.size(), " ", newGfycats.size(), " notifyItemRangeInserted(", result.subsetEndIndex, ", ", countAtEnd, ")");
                notifyItemRangeInserted(result.subsetEndIndex, countAtEnd);
                return true;
            }
            if (countAtStart == 0 && countAtEnd == 0) {
                Logging.d(LOG_TAG, "changeGfycats() ", oldGfycats.size(), " ", newGfycats.size(), " NO CHANGES");
                return false;
            }
        } else {
            Logging.d(LOG_TAG, "changeGfycats() ", oldGfycats.size(), " ", newGfycats.size(), " notifyDataSetChanged()");
            notifyDataSetChanged();
            return false;
        }

        Assertions.fail(new IllegalAccessException("Unreachable"));
        return false;
    }

    public Gfycat getItem(int position) {
        return gfycats.get(position);
    }

    @Override
    public int getItemCount() {
        return gfycats.size();
    }

    @Override
    public void onBindViewHolder(T holder, int position) {
        onBindViewHolder(holder, gfycats.get(position));
    }

    public abstract void onBindViewHolder(T holder, Gfycat gfycat);

    public int getGfycatPosition(String gfyId) {
        if (TextUtils.isEmpty(gfyId)) {
            return 0;
        }
        for (int pos = 0; pos < gfycats.size(); pos++) {
            if (gfycats.get(pos).getGfyId().equals(gfyId)) {
                return pos;
            }
        }
        return 0;
    }

}
