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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gfycat.common.ChainedException;
import com.gfycat.common.Recyclable;
import com.gfycat.common.lifecycledelegates.BaseFragment;
import com.gfycat.common.lifecycledelegates.FragmentContextResolver;
import com.gfycat.common.recycler.AutoPlayController;
import com.gfycat.common.recycler.EndlessScrollListener;
import com.gfycat.common.recycler.PlaybackManager;
import com.gfycat.common.recycler.decorations.OffsetPaddingItemDecoration;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.UIUtils;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.FeedIdentifierFactory;
import com.gfycat.core.PublicFeedIdentifier;
import com.gfycat.core.bi.BIContext;
import com.gfycat.core.downloading.FeedData;
import com.gfycat.core.downloading.FeedManager;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.network.ConnectionEstablishedDelegate;
import com.gfycat.picker.R;
import com.gfycat.picker.search.DataLoadProgressListener;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by anton on 11/3/16.
 */

public abstract class BaseColumnFeedFragment extends BaseFragment {

    private static final String LOG_TAG = "BaseColumnFeedFragment";

    private static final String FEED_IDENTIFIER_KEY = "FEED_IDENTIFIER_KEY";
    private static final long TIME_TO_NOT_RELOAD_SINCE_ON_CREATE = TimeUnit.SECONDS.toMillis(15);
    private static final long TIME_TO_RELOAD_MILLIS = TimeUnit.MINUTES.toMillis(3);

    private FeedIdentifier targetIdentifier;
    private final IFeedLoader feedLoader;

    private RecyclerView recyclerView;
    private GfycatDataAdapter adapter;

    private DataLoadProgressListener dataLoadProgressListener;

    private AutoPlayController autoPlayController = new AutoPlayController();
    private PlaybackManager playbackManager = new PlaybackManager(autoPlayController);

    private LoadMoreListener loadMoreController;
    private boolean feedReloadingNeeded = false;
    private long onCreateTime;

    private boolean feedForceReloadingNeeded = false;

    private LocalCellController localCellController;

    protected final Set<Recyclable> weakRecyclableItemsForRelease = Collections.newSetFromMap(new WeakHashMap<>());

    public static Bundle prepareArguments(Bundle bundle, FeedIdentifier feedIdentifier) {
        bundle.putString(FEED_IDENTIFIER_KEY, feedIdentifier.toUniqueIdentifier());
        return bundle;
    }

    public static Bundle prepareArguments(FeedIdentifier feedIdentifier) {
        return prepareArguments(new Bundle(), feedIdentifier);
    }

    public BaseColumnFeedFragment() {
        FeedLoadingDelegate feedLoadingDelegate = new FeedLoadingDelegate(new FragmentContextResolver(this));
        addDelegate(feedLoadingDelegate);
        feedLoader = feedLoadingDelegate;

        ConnectionEstablishedDelegate connectionEstablishedDelegate = new ConnectionEstablishedDelegate(new FragmentContextResolver(BaseColumnFeedFragment.this), this::onNetworkAvailable);
        addDelegate(connectionEstablishedDelegate);
    }

    private void onNetworkAvailable() {
        if (feedLoader.hasError()) {
            feedLoader.reLoad();
        }
    }

    /**
     * Count of columns to display gfycats.
     */
    protected abstract int getColumnCount();

    /**
     * If after adapter customization, Gfycats started not from first index.
     *
     * @return count of items thet was added prior GfycatDataAdapter items.
     */
    protected int getOffset() {
        return 0;
    }

    /**
     * @param gfycat - user has clicked on.
     */
    protected abstract void onClick(FeedIdentifier identifier, Gfycat gfycat, int positionInFeed);

    /**
     * @return context for pixel event "play gfycat"
     * event will no be sent if null
     */
    protected BIContext getBIContext() {
        return null;
    }

    /**
     * @return picker orientation {@link OrientationHelper#VERTICAL} or {@link OrientationHelper#HORIZONTAL}
     */
    protected int getOrientation() {
        return OrientationHelper.VERTICAL;
    }

    /**
     * @param recyclerView that is used to display items.
     */
    protected void onCustomizeRecycler(RecyclerView recyclerView) {
    }

    /**
     * @param adapter with gfycat items.
     * @return adapter that should be set in RecyclerView.
     */
    protected RecyclerView.Adapter customizeAdapter(GfycatDataAdapter adapter) {
        return adapter;
    }

    /**
     * @param enabled true if gfycats should autoplay.
     */
    public void setPlaybackEnabled(boolean enabled) {
        playbackManager.shouldPlay(enabled);
    }

    protected void setFeedReloadingNeeded(boolean feedReloadingNeeded) {
        this.feedReloadingNeeded = feedReloadingNeeded;
    }

    protected void setFeedForceReloadingNeeded(boolean feedForceReloadingNeeded) {
        if (feedForceReloadingNeeded) {
            Logging.d(LOG_TAG, "feedForceReloadingNeeded, feedLoader.reLoad()");
            feedLoader.setFeedForceReloadingNeeded(feedForceReloadingNeeded);
            feedLoader.reLoad();
        }
    }

    public void setDataLoadProgressListener(DataLoadProgressListener dataLoadProgressListener) {
        this.dataLoadProgressListener = dataLoadProgressListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        resolveFeedIdentifier(savedInstanceState);

        adapter = new GfycatDataAdapter(targetIdentifier, getOrientation(), getCornerRadius(), Collections.emptyList(), getCellController(), getBIContext(), weakRecyclableItemsForRelease);
        loadMoreController = new LoadMoreListener();
        feedLoader.initialize(targetIdentifier, new LocalListener());
        onCreateTime = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
    }

    protected float getCornerRadius() {
        return 0;
    }

    private void resolveFeedIdentifier(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(FEED_IDENTIFIER_KEY)) {
            targetIdentifier = FeedIdentifierFactory.fromUniqueIdentifier(savedInstanceState.getString(FEED_IDENTIFIER_KEY));
        } else if (getArguments() != null && getArguments().containsKey(FEED_IDENTIFIER_KEY)) {
            targetIdentifier = FeedIdentifierFactory.fromUniqueIdentifier(getArguments().getString(FEED_IDENTIFIER_KEY));
        }

        if (targetIdentifier == null) {
            Assertions.fail(new IllegalArgumentException("To start BaseColumnFeedFragment you should provide FEED_IDENTIFIER_KEY in fragment arguments. See prepareArguments(...) methods."));
            targetIdentifier = PublicFeedIdentifier.trending();
        }
    }

    protected void changeFeed(FeedIdentifier newIdentifier) {
        Logging.d(LOG_TAG, "changeFeed(", newIdentifier, ")");
        targetIdentifier = newIdentifier;
        feedLoader.changeFeed(newIdentifier);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gfycat_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = UIUtils.findView(view, R.id.recycler_view);

        playbackManager.setRecyclerView(recyclerView);

        customizeRecycler(recyclerView);
        recyclerView.setAdapter(customizeAdapter(adapter));
    }

    @Override
    public void onStart() {
        super.onStart();
        playbackManager.started();
        reloadIfNeeded();
    }

    @Override
    public void onStop() {
        super.onStop();
        playbackManager.stopped();
    }

    protected void reloadIfNeeded() {
        if (feedReloadingNeeded && System.currentTimeMillis() > TIME_TO_RELOAD_MILLIS + feedLoader.lastSuccessRequestMs() && System.currentTimeMillis() > onCreateTime + TIME_TO_NOT_RELOAD_SINCE_ON_CREATE) {
            Logging.d(LOG_TAG, TimeUnit.SECONDS.convert(TIME_TO_RELOAD_MILLIS, TimeUnit.MILLISECONDS), " seconds passed, reloading content");
            feedLoader.reLoad();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FEED_IDENTIFIER_KEY, targetIdentifier.toUniqueIdentifier());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Recyclable recyclable : weakRecyclableItemsForRelease) {
            if (recyclable != null) {
                recyclable.recycle();
            }
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public FeedIdentifier getTargetIdentifier() {
        return targetIdentifier;
    }

    private void customizeRecycler(RecyclerView recyclerView) {
        GfyStaggeredGridLayoutManager layoutManager = new GfyStaggeredGridLayoutManager(getColumnCount(), getOrientation());
        if (getOrientation() == OrientationHelper.VERTICAL)
            layoutManager.setGapStrategy(GfyStaggeredGridLayoutManager.GAP_HANDLING_NONE);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setLayoutManager(layoutManager);
        applyItemPaddingDecoration();

        recyclerView.addOnScrollListener(loadMoreController);
        recyclerView.addOnScrollListener(autoPlayController);
        recyclerView.setRecyclerListener(holder -> {
            Logging.d(LOG_TAG, "::RecyclerListener::onViewRecycled(", holder.hashCode(), ")");
            if (holder instanceof Recyclable) {
                ((Recyclable) holder).recycle();
            }
        });

        onCustomizeRecycler(recyclerView);
    }

    protected void applyItemPaddingDecoration() {
        recyclerView.addItemDecoration(new OffsetPaddingItemDecoration(getResources().getDimensionPixelOffset(R.dimen.gfycat_categories_cell_padding), getColumnCount(), getOffset()));
    }

    protected CellController getCellController() {
        return localCellController == null ? localCellController = new LocalCellController() : localCellController;
    }

    private class LocalCellController implements CellController {
        @Override
        public void onClick(Gfycat gfycat, int positionInFeed) {
            BaseColumnFeedFragment.this.onClick(targetIdentifier, gfycat, positionInFeed);
        }
    }

    private class LocalListener implements FeedLoadingDelegate.FeedLoadingListener {

        @Override
        public void onFeedLoadingStarted() {
            if (dataLoadProgressListener != null) {
                dataLoadProgressListener.onDataLoadStarted();
            }
        }

        @Override
        public void onFeedLoaded(FeedData feedData) {
            Logging.d(LOG_TAG, "::mGfycatsLoader::onFeedLoaded(", feedData, ") count = " + feedData.getCount());

            if (!isAdded()) return;

            if (dataLoadProgressListener != null && (feedData.isClosed() || feedData.getCount() > 1)) {
                dataLoadProgressListener.onDataLoadFinished();
            }

            int positionChanged = Math.max(adapter.getItemCount() - getColumnCount(), 0);

            boolean rangeChangeHappens = adapter.updateFeed(feedData.getIdentifier(), feedData.getGfycats());

            recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    recyclerView.removeOnLayoutChangeListener(this);
                    loadMoreController.forceUpdate(recyclerView);
                }
            });

            autoPlayController.forceUpdate(recyclerView);

            if (rangeChangeHappens) {
                adapter.notifyItemRangeChanged(positionChanged, Math.min(getColumnCount(), adapter.getItemCount()));
            }

            loadMoreController.resetLoadingState();
        }

        @Override
        public void onError(Throwable throwable) {
            Logging.d(LOG_TAG, throwable, "message = ", throwable.getMessage());
            if (throwable instanceof FeedManager.NoSearchResultException) {
                if (adapter == null || adapter.getItemCount() == 0) {
                    /**
                     * We should display such toast only if there are no content to show.
                     */
                    Toast.makeText(getContext(), R.string.gfycat_no_search_result, Toast.LENGTH_SHORT).show();
                }
            } else {
                Logging.c(LOG_TAG, "Failed to load gfycats for " + getTargetIdentifier().toUniqueIdentifier(), new ChainedException(throwable));
                Toast.makeText(getContext(), R.string.gfycats_can_not_load_gfycats, Toast.LENGTH_SHORT).show();
                loadMoreController.resetLoadingState();
            }
            if (dataLoadProgressListener != null) {
                dataLoadProgressListener.onDataLoadError();
            }
        }
    }

    private class LoadMoreListener extends EndlessScrollListener {
        @Override
        public void onLoadMore(int page) {
            feedLoader.loadMore();
        }
    }
}