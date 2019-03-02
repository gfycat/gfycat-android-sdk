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

package com.gfycat.picker.category;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.gfycat.common.Recyclable;
import com.gfycat.common.lifecycledelegates.BaseFragment;
import com.gfycat.common.lifecycledelegates.FragmentContextResolver;
import com.gfycat.common.lifecycledelegates.LifecycleLoggingDelegate;
import com.gfycat.common.recycler.AutoPlayController;
import com.gfycat.common.recycler.EmptySpanAdapter;
import com.gfycat.common.recycler.GridSpanSizeLookup;
import com.gfycat.common.recycler.GroupAdapter;
import com.gfycat.common.recycler.PlaybackManager;
import com.gfycat.common.recycler.decorations.PaddingItemDecoration;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Sugar;
import com.gfycat.core.AdsManager;
import com.gfycat.core.GfyCore;
import com.gfycat.core.gfycatapi.pojo.GfycatCategoriesList;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;
import com.gfycat.core.gfycatapi.pojo.GfycatRecentCategory;
import com.gfycat.core.network.ConnectionEstablishedDelegate;
import com.gfycat.picker.R;
import com.gfycat.picker.ads.PickerAdsPlacement;
import com.gfycat.picker.photomoments.PhotoMomentsCategoryHelper;
import com.gfycat.picker.search.CategoriesFragment;
import com.gfycat.picker.search.CategoriesFragmentController;
import com.gfycat.picker.search.DataLoadProgressListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import io.reactivex.disposables.Disposable;

/**
 * Created by anton on 11/1/16.
 */

public class GfycatCategoriesFragment extends BaseFragment implements CategoriesFragment {

    private static final String LOG_TAG = "GfycatCategoriesFragment";

    private static final String EXTRA_FILTER = "EXTRA_FILTER";
    private static final String EXTRA_RECENT_CATEGORY_ENABLED = "EXTRA_RECENT_CATEGORY_ENABLED";

    private GroupAdapter groupAdapter;
    private Disposable categoriesDisposable;

    public static GfycatCategoriesFragment create(boolean shouldEnableRecentCategory) {
        GfycatCategoriesFragment fragment = new GfycatCategoriesFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_RECENT_CATEGORY_ENABLED, shouldEnableRecentCategory);
        fragment.setArguments(bundle);
        return fragment;
    }

    private RecyclerView.OnScrollListener scrollListener;
    private RecyclerView recyclerView;

    private DataLoadProgressListener dataLoadProgressListener;

    private AutoPlayController autoPlayController = new AutoPlayController();
    private PlaybackManager playbackManager = new PlaybackManager(autoPlayController);

    private String filter = "";
    private CategoriesAdapter categoriesAdapter;
    private SingleCategoryAdapter recentAdapter;
    private SingleAdAdapter singleAdAdapter;

    private Set<Recyclable> weakRecyclableItemsForRelease = Collections.newSetFromMap(new WeakHashMap<>());

    private PhotoMomentsCategoryHelper photoMomentsCategoryHelper;

    private boolean recentCategoryEnabled = false;

    public GfycatCategoriesFragment() {
        GfyCore.assertInitializeState();

        ConnectionEstablishedDelegate connectionEstablishedDelegate = new ConnectionEstablishedDelegate(new FragmentContextResolver(GfycatCategoriesFragment.this),
                this::onConnected);
        addDelegate(connectionEstablishedDelegate);
        addDelegate(new LifecycleLoggingDelegate(new FragmentContextResolver(this), LOG_TAG));
    }

    private void onConnected() {
        startLoadingCategories();
    }

    private void startLoadingCategories() {
        if (dataLoadProgressListener != null) {
            dataLoadProgressListener.onDataLoadStarted();
        }
        Sugar.doIfNotNull(categoriesDisposable, Disposable::dispose);
        if (recentCategoryEnabled) {
            categoriesDisposable = GfycatCategoriesLoader.subscribe(
                    this::onRecentCategoryLoaded,
                    this::onCategoriesLoaded, this::onLoadingFailed, this::onLoadingComplete);
        } else {
            categoriesDisposable = GfycatCategoriesLoader.subscribeForCategories(
                    this::onCategoriesLoaded, this::onLoadingFailed, this::onLoadingComplete);
        }
    }

    private CategoriesFragmentController getCategoriesFragmentController() {
        if (getParentFragment() != null && getParentFragment() instanceof CategoriesFragmentController) {
            return (CategoriesFragmentController) getParentFragment();
        } else if (getActivity() != null && getActivity() instanceof CategoriesFragmentController) {
            return (CategoriesFragmentController) getActivity();
        }
        throw new IllegalStateException("Not getActivity() not getParentFragment() not implements CategoriesFragmentController interface");
    }

    private float getCornerRadius() {
        return getCategoriesFragmentController().getCategoryCornerRadius();
    }

    private int getOrientation() {
        return getCategoriesFragmentController().getOrientation();
    }

    private float getAspectRatio() {
        return getCategoriesFragmentController().getCategoryAspectRatio();
    }

    private int getColumnCount() {
        return getCategoriesFragmentController().getCategoriesColumnCount();
    }

    private void onClick(GfycatCategory category) {
        getCategoriesFragmentController().onCategoryClick(category);
    }

    /**
     * @param isVisible true if autoplay should work, false otherwise.
     *                  Calling this method with false - will stop all playbacks.
     */
    public void setPlaybackEnabled(boolean isVisible) {
        playbackManager.shouldPlay(isVisible);
    }

    /**
     * Will be shown categories that contains filter string.
     *
     * @param filter string that will filter categories.
     */
    public void setFilter(String filter) {
        Logging.d(LOG_TAG, "setFilter(", filter, ")");
        this.filter = filter;
        boolean adapterUpdated = false;
        if (categoriesAdapter != null) {
            categoriesAdapter.filter(filter);
            adapterUpdated = true;
        }

        if (recentAdapter != null) {
            recentAdapter.filter(filter);
            adapterUpdated = true;
        }

        if (adapterUpdated) {
            getRecyclerView().smoothScrollToPosition(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.gfycat_categories_fragment_layout, null);

        if (savedInstanceState != null) {
            filter = savedInstanceState.getString(EXTRA_FILTER);
            recentCategoryEnabled = savedInstanceState.getBoolean(EXTRA_RECENT_CATEGORY_ENABLED);
        } else if (getArguments() != null) {
            recentCategoryEnabled = getArguments().getBoolean(EXTRA_RECENT_CATEGORY_ENABLED);
        }

        initUI(root);
        initAdapters();

        return root;
    }

    private void applyItemPaddingDecoration() {
        recyclerView.addItemDecoration(
                PaddingItemDecoration.dynamicFirstRowDecoration(
                        () -> 1, getResources().getDimensionPixelOffset(R.dimen.gfycat_categories_cell_padding), getColumnCount()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_FILTER, filter);
        outState.putBoolean(EXTRA_RECENT_CATEGORY_ENABLED, recentCategoryEnabled);
    }

    @Override
    public void onStart() {
        super.onStart();
        playbackManager.started();

        startLoadingCategories();
    }

    @Override
    public void onStop() {
        super.onStop();
        playbackManager.stopped();
        Sugar.doIfNotNull(categoriesDisposable, Disposable::dispose);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Recyclable recyclable : weakRecyclableItemsForRelease) {
            if (recyclable != null) {
                recyclable.recycle();
            }
        }
        if (photoMomentsCategoryHelper != null) {
            photoMomentsCategoryHelper.release();
        }
    }

    private CategoriesAdapter getCategoriesAdapter() {
        return categoriesAdapter;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    private void initAdapters() {
        if (recyclerView == null) {
            throw new NullPointerException("initAdapters() called before recyclerView initialized!");
        }

        ArrayList<RecyclerView.Adapter> adapters = new ArrayList<>();
        ArrayList<String> adapterNames = new ArrayList<>();

        if (getOrientation() == OrientationHelper.VERTICAL) {
            adapters.add(new EmptySpanAdapter(recyclerView.getLayoutManager(), getCategoriesFragmentController().getContentTopPadding()));
            adapterNames.add("empty_span");
        } else {
            adapters.add(new EmptySpanAdapter(recyclerView.getLayoutManager(), getCategoriesFragmentController().getContentLeftPadding(), ViewGroup.LayoutParams.WRAP_CONTENT));
            adapterNames.add("empty_span");
        }

        if (getOrientation() == OrientationHelper.VERTICAL) {
            adapters.add(singleAdAdapter = new SingleAdAdapter(PickerAdsPlacement.CATEGORY_LIST, weakRecyclableItemsForRelease));
            adapterNames.add("ads");
        }

        if (recentCategoryEnabled) {
            recentAdapter =
                    new SingleCategoryAdapter(
                            this::onClick,
                            weakRecyclableItemsForRelease,
                            getAspectRatio(),
                            getOrientation(),
                            getCornerRadius());
            adapters.add(recentAdapter);
            adapterNames.add("recent");
        }

        categoriesAdapter = new CategoriesAdapter(
                this::onClick,
                weakRecyclableItemsForRelease,
                getAspectRatio(),
                getOrientation(),
                getCornerRadius());

        adapters.add(categoriesAdapter);
        adapterNames.add("categoriesAdapter");

        if (getOrientation() == OrientationHelper.VERTICAL) {
            adapters.add(new EmptySpanAdapter(recyclerView.getLayoutManager(), getCategoriesFragmentController().getContentBottomPadding()));
            adapterNames.add("bottom_padding_adapter");
        }

        recyclerView.setAdapter(groupAdapter = new GroupAdapter(
                adapters.toArray(new RecyclerView.Adapter[adapters.size()]), adapterNames.toArray(new String[adapterNames.size()])));
    }

    private void onRecentCategoryLoaded(GfycatRecentCategory gfycatCategory) {
        if (recentAdapter != null) {
            // set translated Recent title
            gfycatCategory.setTagText(getString(R.string.category_caption_recent));
            recentAdapter.update(gfycatCategory, R.drawable.ic_recent);
        }

        autoPlayController.forceUpdate(recyclerView);
    }

    private void onCategoriesLoaded(GfycatCategoriesList categoriesList) {
        Assertions.assertNotNull(categoriesList.getTags(), NullPointerException::new);

        if (categoriesAdapter != null) {
            categoriesAdapter.updateData(categoriesList.getTags());
        }

        autoPlayController.forceUpdate(recyclerView);
    }

    private void onLoadingFailed(Throwable throwable) {
        Logging.c(LOG_TAG, throwable, "error loading categories.");
        if (dataLoadProgressListener != null) {
            dataLoadProgressListener.onDataLoadError();
        }
    }

    private void onLoadingComplete() {
        if (dataLoadProgressListener != null) {
            dataLoadProgressListener.onDataLoadFinished();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (getOrientation() == OrientationHelper.HORIZONTAL) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) recyclerView.getLayoutParams();
            params.topMargin += getCategoriesFragmentController().getContentTopPadding();
            recyclerView.setLayoutParams(params);
        }
    }

    private void initUI(View root) {
        recyclerView = root.findViewById(R.id.recycler_view);
        int columnCount = getColumnCount();

        playbackManager.setRecyclerView(recyclerView);

        if (getAspectRatio() <= 0) {
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(columnCount, getOrientation());
            recyclerView.setLayoutManager(layoutManager);
        } else {
            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), columnCount, getOrientation(), false);
            layoutManager.setSpanSizeLookup(new GridSpanSizeLookup(this::getSpanSizeForPosition));
            recyclerView.setLayoutManager(layoutManager);
        }

        applyItemPaddingDecoration();

        recyclerView.addOnScrollListener(autoPlayController);
        Sugar.doIfNotNull(scrollListener, recyclerView::addOnScrollListener);
        recyclerView.setRecyclerListener(holder -> {
            if (holder instanceof Recyclable) {
                ((Recyclable) holder).recycle();
            }
        });

        recyclerView.addOnLayoutChangeListener(
                (v, l, t, r, b, ol, ot, or, ob) -> recyclerView.post(() -> {
                    if (isResumed() && singleAdAdapter != null) {
                        singleAdAdapter.setAdsVisible(isViewBigEnoughForAd(recyclerView));
                    }
                }));
    }

    private boolean isViewBigEnoughForAd(View view) {
        return (view.getMeasuredHeight() * 1.0f / getResources().getDisplayMetrics().heightPixels) >= AdsManager.MIN_VIEW_TO_SCREEN_HEIGHT_RATIO;
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
        this.dataLoadProgressListener = dataLoadProgressListener;
    }

    public boolean hasCategoriesThatSatisfiesFilter() {
        return getCategoriesAdapter() == null || getCategoriesAdapter().getItemCount() > 0 || (recentAdapter != null && recentAdapter.getItemCount() > 0);
    }

    public boolean isAllCategoriesFilteredState() {
        return !hasCategoriesThatSatisfiesFilter();
    }

    private int getSpanSizeForPosition(Integer position) {
        if (getOrientation() == OrientationHelper.VERTICAL) {
            if (position == 0)
                return getColumnCount(); // empty first item
            if (singleAdAdapter.getItemCount() > 0 && position <= singleAdAdapter.getItemCount())
                return getColumnCount(); // ads item
            if (position == groupAdapter.getItemCount() - 1)
                return getColumnCount(); // bottom padding item
        }
        return 1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (singleAdAdapter != null) singleAdAdapter.release();
    }
}