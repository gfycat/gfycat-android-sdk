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

package com.gfycat.picker.onecategory;

import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.gfycat.common.lifecycledelegates.FragmentContextResolver;
import com.gfycat.common.lifecycledelegates.LifecycleLoggingDelegate;
import com.gfycat.common.recycler.EmptySpanAdapter;
import com.gfycat.common.recycler.GroupAdapter;
import com.gfycat.common.recycler.decorations.PaddingItemDecoration;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Sugar;
import com.gfycat.core.AdsManager;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.bi.BIContext;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.picker.R;
import com.gfycat.picker.ads.PickerAdsPlacement;
import com.gfycat.picker.bi.LazyLogger;
import com.gfycat.picker.category.SingleAdAdapter;
import com.gfycat.picker.feed.BaseColumnFeedFragment;
import com.gfycat.picker.feed.GfycatDataAdapter;
import com.gfycat.picker.search.CategoriesFragment;
import com.gfycat.picker.search.CategoriesFragmentController;
import com.gfycat.picker.search.DataLoadProgressListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by dekalo on 26.01.17.
 */

public class OneCategoryFeedFragment extends BaseColumnFeedFragment implements CategoriesFragment {

    private static final String LOG_TAG = "OneCategoryFeedFragment";
    private static final String GFYCAT_KEY = "GFYCAT_KEY";
    private static final long SKIP_DURATION = 300;

    private Disposable searchDisposable;
    private SingleAdAdapter singleAdAdapter;
    private SingleGfycatAdapter singleItemAdapter;

    public static OneCategoryFeedFragment create(FeedIdentifier identifier) {
        OneCategoryFeedFragment fragment = new OneCategoryFeedFragment();
        fragment.setArguments(prepareArguments(new Bundle(), identifier));
        return fragment;
    }

    private Gfycat gfycat;
    private BehaviorSubject<String> searchQuery = BehaviorSubject.create();
    private RecyclerView.OnScrollListener scrollListener;

    public OneCategoryFeedFragment() {
        addDelegate(new LifecycleLoggingDelegate(new FragmentContextResolver(this), LOG_TAG));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gfycat = getArguments().getParcelable(GFYCAT_KEY);
        LazyLogger.get().logSearchVideos(getTargetIdentifier().toName());
    }

    @Override
    protected float getCornerRadius() {
        return getCategoriesFragmentController().getGfycatCornerRadius();
    }

    private boolean isViewBigEnoughForAd(View view) {
        return (view.getMeasuredHeight() * 1.0f / getResources().getDisplayMetrics().heightPixels) >= AdsManager.MIN_VIEW_TO_SCREEN_HEIGHT_RATIO;
    }

    private CategoriesFragmentController getCategoriesFragmentController() {
        if (getParentFragment() != null && getParentFragment() instanceof CategoriesFragmentController) {
            return (CategoriesFragmentController) getParentFragment();
        } else if (getActivity() != null && getActivity() instanceof CategoriesFragmentController) {
            return (CategoriesFragmentController) getActivity();
        }
        throw new IllegalStateException("Not getActivity() not getParentFragment() not implements GfycatCategoryControllerController interface");
    }

    @Override
    protected BIContext getBIContext() {
        return new BIContext(BIContext.PLAY_IN_CATEGORY);
    }

    protected RecyclerView.Adapter customizeAdapter(GfycatDataAdapter adapter) {
        ArrayList<RecyclerView.Adapter> adapters = new ArrayList<>();
        ArrayList<String> adapterNames = new ArrayList<>();

        if (isVertical()) {
            adapters.add(new EmptySpanAdapter(getRecyclerView().getLayoutManager(), getCategoriesFragmentController().getContentTopPadding()));
        } else {
            adapters.add(new EmptySpanAdapter(getRecyclerView().getLayoutManager(), getCategoriesFragmentController().getContentLeftPadding(), ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        adapterNames.add("empty_span_adapter");

        if (isVertical()) {
            adapters.add(singleAdAdapter = new SingleAdAdapter(PickerAdsPlacement.CATEGORY_CONTENT, weakRecyclableItemsForRelease, getRecyclerView().getLayoutManager() instanceof StaggeredGridLayoutManager));
            adapterNames.add("onecategory:ad_item");
        }

        adapters.add(singleItemAdapter = new SingleGfycatAdapter(getTargetIdentifier(), gfycat, getOrientation(), getCornerRadius(), getCellController()));
        adapterNames.add("onecategory:single_item");

        adapters.add(adapter);
        adapterNames.add("onecategory:all_gfycats");

        if (isVertical()) {
            adapters.add(new EmptySpanAdapter(getRecyclerView().getLayoutManager(), getCategoriesFragmentController().getContentBottomPadding()));
            adapterNames.add("bottom_padding_adapter");
        }

        return new GroupAdapter(
                adapters.toArray(new RecyclerView.Adapter[adapters.size()]), adapterNames.toArray(new String[adapterNames.size()]));
    }

    private boolean isHorizontal() {
        return getOrientation() == OrientationHelper.HORIZONTAL;
    }

    private boolean isVertical() {
        return getOrientation() == OrientationHelper.VERTICAL;
    }

    protected int getOrientation() {
        return getCategoriesFragmentController().getOrientation();
    }

    @Override
    protected int getColumnCount() {
        return getCategoriesFragmentController().getGfycatsColumnCount();
    }

    @Override
    protected void onClick(FeedIdentifier identifier, Gfycat gfycat, int positionInFeed) {
        getCategoriesFragmentController().onGfycatClick(identifier, gfycat, positionInFeed);
    }

    @Override
    protected void onCustomizeRecycler(RecyclerView recyclerView) {
        Sugar.doIfNotNull(scrollListener, recyclerView::addOnScrollListener);

        if (isHorizontal()) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) recyclerView.getLayoutParams();
            params.topMargin += getCategoriesFragmentController().getContentTopPadding();
            recyclerView.setLayoutParams(params);
        }

        recyclerView.addOnLayoutChangeListener(
                (v, l, t, r, b, ol, ot, or, ob) -> recyclerView.post(() -> {
                    if (isResumed() && singleAdAdapter != null) {
                        singleAdAdapter.setAdsVisible(isViewBigEnoughForAd(recyclerView));
                    }
                }));
    }

    @Override
    protected void applyItemPaddingDecoration() {
        getRecyclerView().addItemDecoration(
                PaddingItemDecoration.dynamicFirstRowDecoration(
                        () -> 1, getResources().getDimensionPixelOffset(R.dimen.gfycat_categories_cell_padding), getColumnCount()));
    }

    @Override
    public void setFilter(String filter) {
        Logging.d(LOG_TAG, "setFilter(", filter, ")");
        searchQuery.onNext(filter);
    }

    private void internalSetFilter(String filter) {
        Logging.d(LOG_TAG, "internalSetFilter(", filter, ")");
        LazyLogger.get().logSearchVideos(filter);
        changeFeed(getCategoriesFragmentController().getFeedSelectionResolver().resolveSearchFeed(filter));
    }

    @Override
    public void onStart() {
        super.onStart();

        searchDisposable = searchQuery
                .debounce(SKIP_DURATION, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::internalSetFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        searchDisposable.dispose();
    }

    @Override
    protected void changeFeed(FeedIdentifier newIdentifier) {
        gfycat = null;
        Sugar.doIfNotNull(getArguments(), bundle -> bundle.remove(GFYCAT_KEY));
        singleItemAdapter.setGfycat(gfycat);
        super.changeFeed(newIdentifier);
    }

    @Override
    public void setScrollListener(RecyclerView.OnScrollListener scrollListener) {
        if (getRecyclerView() != null && scrollListener != null) {
            getRecyclerView().addOnScrollListener(scrollListener);
        } else {
            this.scrollListener = scrollListener;
        }
    }

    @Override
    public void setDataLoadProgressListener(DataLoadProgressListener dataLoadProgressListener) {
        super.setDataLoadProgressListener(dataLoadProgressListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (gfycat != null)
            outState.putSerializable(GFYCAT_KEY, gfycat);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (singleAdAdapter != null) singleAdAdapter.release();
    }
}
