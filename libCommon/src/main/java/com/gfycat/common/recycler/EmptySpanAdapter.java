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

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import com.gfycat.common.utils.Assertions;

/**
 * Created by dekalo on 03/10/17.
 */

public class EmptySpanAdapter extends RecyclerView.Adapter<EmptySpanAdapter.EmptySpanViewHolder> {

    private final RecyclerView.LayoutManager layoutManager;
    private final int width;
    private final int height;

    public EmptySpanAdapter(RecyclerView.LayoutManager layoutManager) {
        this(layoutManager, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public EmptySpanAdapter(RecyclerView.LayoutManager layoutManager, int height) {
        this.layoutManager = layoutManager;
        this.height = height;
        this.width = ViewGroup.LayoutParams.WRAP_CONTENT;
    }


    public EmptySpanAdapter(RecyclerView.LayoutManager layoutManager, int width, int height) {
        this.layoutManager = layoutManager;
        this.width = width;
        this.height = height;
    }

    @Override
    public EmptySpanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Space view = new Space(parent.getContext());

        if (layoutManager == null) {
            Assertions.fail(new IllegalStateException("This layoutManager is null."));
        } else if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(width, height);
            view.setLayoutParams(params);
        } else if (layoutManager != null && layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager.LayoutParams params = new StaggeredGridLayoutManager.LayoutParams(width, height);
            params.setFullSpan(true);
            view.setLayoutParams(params);
        } else if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(width, height);
            view.setLayoutParams(params);
        } else {
            Assertions.fail(new IllegalStateException("This(" + layoutManager.getClass().getSimpleName() + ") layout manager is not supported."));
        }

        return new EmptySpanViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public void onBindViewHolder(EmptySpanViewHolder holder, int position) {
    }

    public class EmptySpanViewHolder extends RecyclerView.ViewHolder {

        public EmptySpanViewHolder(View itemView) {
            super(itemView);
        }
    }
}
