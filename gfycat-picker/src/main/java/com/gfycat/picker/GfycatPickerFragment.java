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

package com.gfycat.picker;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.gfycat.common.Function;
import com.gfycat.common.ProgressBarController;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Sugar;
import com.gfycat.common.utils.UIUtils;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.FeedIdentifierFactory;
import com.gfycat.core.GfyPrivateHelper;
import com.gfycat.core.PublicFeedIdentifier;
import com.gfycat.core.RecentFeedIdentifier;
import com.gfycat.core.db.CloseMode;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;
import com.gfycat.picker.bi.KeyboardLogger;
import com.gfycat.picker.bi.KeyboardLogger.SendGfycatSource;
import com.gfycat.picker.bi.LazyLogger;
import com.gfycat.picker.category.GfycatCategoriesFragment;
import com.gfycat.picker.onecategory.OneCategoryFeedFragment;
import com.gfycat.picker.photomoments.IPhotoMomentsFragment;
import com.gfycat.picker.photomoments.PhotoMomentsUiFactory;
import com.gfycat.picker.photomoments.PhotoMomentsUiFactoryInitializer;
import com.gfycat.picker.search.CategoriesFragment;
import com.gfycat.picker.search.CategoriesFragmentController;
import com.gfycat.picker.search.DataLoadProgressListener;
import com.gfycat.picker.search.DefaultSearchCategoryView;
import com.gfycat.picker.search.SearchController;
import com.gfycat.picker.search.SearchControllerListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * This is a standalone Gfycat picker. Displays a selection of top categories, recently tapped Gfycats category.
 * Provides a search Gfycat user interface.
 * <p>
 * Subscribe for onGfycatSelected event to get a {@link Gfycat} object user has selected.
 * <p>
 * Set accentTint parameter to customize the accent color of a Picker for your app needs.
 * <p>
 * NOTE: {@link com.gfycat.core.GfyCore} needs to be initialized before this Fragment is used.
 * <p>
 * See <a href="http://developers.gfycat.com/androidsdk/#gfycat-picker-fragment">http://developers.gfycat.com/androidsdk/#gfycat-picker-fragment</a> for more integration details.
 */

public class GfycatPickerFragment extends Fragment implements CategoriesFragmentController {
    public static final float DEFAULT_CATEGORY_ASPECT_RATIO = 1.0f;
    public static final int DEFAULT_ORIENTATION = OrientationHelper.VERTICAL;

    private static final String LOG_TAG = "GfycatPickerFragment";
    private static final long NO_CATEGORY_RULE_DELAY = TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);
    private static final long EMPTY_SEARCH_RULE_DELAY = 300;
    private static final String TRENDING_TAG = "trending";
    private static final String CURRENT_QUERY_KEY = "CURRENT_QUERY_KEY";
    private static final String CURRENT_FEED_IDENTIFIER_KEY = "CURRENT_FEED_IDENTIFIER_KEY";
    private static final String IS_SEARCHBAR_VISIBLE_KEY = "IS_SEARCHBAR_VISIBLE_KEY";

    private static final String EXTRA_RECENT_CATEGORY_ENABLED = "EXTRA_RECENT_CATEGORY_ENABLED";
    private static final String EXTRA_CLOSE_ON_GFYCATCLICK = "EXTRA_CLOSE_ON_GFYCATCLICK";

    private ViewGroup root;

    private ProgressBar progressBar;

    private String currentSearchQuery = "";
    private SearchController searchController;
    private ProgressBarController progressBarController;

    private CategoriesFragment currentCategoriesFragment;
    private RecyclerView.OnScrollListener scrollListener;

    private DataLoadProgressListener dataLoadProgressListener;

    private Set<OnGfycatSelectedListener> onGfycatSelectedListeners = new HashSet<>();

    private GoToSearchInCategory goToSearchInCategory = new GoToSearchInCategory();
    private ExitFromCategoryOnEmptySearch exitFromCategoryOnEmptySearch = new ExitFromCategoryOnEmptySearch();

    // indicates that user is inside category from picker
    private boolean insideCategory;

    private int columnCountGfycats;
    private int columnCountCategories;
    private int accentTint = -1;

    private float aspectRatio = 1f;
    private int orientation = OrientationHelper.VERTICAL;

    private boolean closeOnGfycatClick = false;
    private boolean recentCategoryEnabled = false;

    private FeedIdentifier trendingIdentifierSample = PublicFeedIdentifier.fromReaction(TRENDING_TAG);

    private float categoryCornerRadius;
    private float gfycatCornerRadius;

    /**
     * @param enabled false if webp should not play, true otherwise.
     */
    public void setPlaybackEnabled(boolean enabled) {
        Sugar.doIfNotNull(getCurrentCategoriesFragment(), categoriesFragment -> categoriesFragment.setPlaybackEnabled(enabled));
    }

    /**
     * @param searchFilter from outside of GfycatPickerFragment.
     */
    public void setSearchFilter(@Nullable String searchFilter) {
        applyNewSearchQuery(searchFilter, false);

        if (searchController != null)
            searchController.setSearchQuery(searchFilter);
    }

    /**
     * @return currently applied search filter
     */
    public String getSearchFilter() {
        return currentSearchQuery;
    }

    /**
     * Provide possibility to add some views on top of categories fragments.
     */
    public void onCreateAdditionalViews(LayoutInflater inflater, ViewGroup fragmentRootView, @Nullable Bundle savedInstanceState) {
    }

    /**
     * Subscribe on Gfycat selected event.
     *
     * @param onGfycatSelectedListener
     */
    public void addOnGfycatSelectedListener(OnGfycatSelectedListener onGfycatSelectedListener) {
        onGfycatSelectedListeners.add(onGfycatSelectedListener);
    }

    /**
     * Unsubscribe from Gfycat selected event.
     *
     * @param onGfycatSelectedListener
     */
    public void removeOnGfycatSelectedListener(OnGfycatSelectedListener onGfycatSelectedListener) {
        onGfycatSelectedListeners.remove(onGfycatSelectedListener);
    }

    /**
     * Called when user clicked on gfycat in identifier.getName() category.
     */
    public void onGfycatSelected(FeedIdentifier identifier, Gfycat gfycat, int position) {
    }

    /**
     * @return columns count of gfycats that should be displayed.
     */
    @Override
    public int getGfycatsColumnCount() {
        return columnCountGfycats > 0 ? columnCountGfycats : getResources().getInteger(R.integer.gfycat_categories_gfycat_columns_count);
    }

    /**
     * @return search view padding from top.
     */
    public int getSearchViewTopPadding() {
        return 0;
    }

    /**
     * @return scrollable padding from bottom screen end to content.
     */
    @Override
    public int getContentBottomPadding() {
        return 0;
    }

    /**
     * @return scrollable padding from left screen end to content,
     * applied in case of horizontal scrolling.
     */
    @Override
    public int getContentLeftPadding() {
        return 0;
    }

    /**
     * @return scrollable padding from top screen end to content.
     */
    @Override
    public int getContentTopPadding() {
        return searchController.getSearchHeight();
    }

    /**
     * Called to have the fragment instantiate its Search View. This is optional,
     * and if not overridden default search view will be used.
     * <p>
     * This will be called in onCreateView(LayoutInflater, ViewGroup, Bundle).
     *
     * @param container Search View should be added to this ViewGroup object.
     * @return Return a SearchController instance, can not be null.
     */
    @NonNull
    public SearchController onCreateSearchView(@NonNull ViewGroup container) {
        DefaultSearchCategoryView localSearchController = new DefaultSearchCategoryView(container.getContext());
        container.addView(localSearchController);
        return localSearchController;
    }

    /**
     * Style picker's tint color.
     *
     * @param color - tint color.
     */
    public final void setAccentTintColor(int color) {
        accentTint = color;
        setupAccentTintColor();
    }

    /**
     * @return tint color for progress bar and search close button.
     */
    public int getAccentTintColor() {
        return accentTint;
    }

    private void setupAccentTintColor() {
        if (accentTint == -1) {
            accentTint = ContextCompat.getColor(getContext(), R.color.gfycat_accent_color);
        }
        if (progressBar != null) {
            progressBar.getIndeterminateDrawable().setColorFilter(getAccentTintColor(), PorterDuff.Mode.SRC_IN);
        }
        if (searchController != null) {
            searchController.setAccentTintColor(getAccentTintColor());
        }
    }

    /**
     * Enables Recent category that will be populated with Gfycats that have been tapped by user.
     *
     * @param recentCategoryEnabled
     */
    public void setRecentCategoryEnabled(boolean recentCategoryEnabled) {
        this.recentCategoryEnabled = recentCategoryEnabled;
    }

    /**
     * Will close current category and move back to categories list once Gfycat will be clicked
     *
     * @param shouldClose
     */
    public void setCloseOnGfycatClick(boolean shouldClose) {
        closeOnGfycatClick = shouldClose;
    }

    /**
     * Close category and move to categories list.
     */
    public final void closeCategory() {
        if (isResumed() && !(currentCategoriesFragment instanceof GfycatCategoriesFragment) && searchController != null) {
            searchController.setSearchQuery("");
            UIUtils.hideKeyboardForced(root);
            changeFragment(GfycatCategoriesFragment.create(recentCategoryEnabled));
            insideCategory = false;
        }
    }

    @Override
    public int getOrientation() {
        return orientation;
    }

    @Override
    public float getCategoryAspectRatio() {
        return aspectRatio;
    }

    public float getCategoryCornerRadius() {
        return categoryCornerRadius;
    }

    public float getGfycatCornerRadius() {
        return gfycatCornerRadius;
    }

    /**
     * @return count of categories to show on current screen.
     */
    @Override
    public int getCategoriesColumnCount() {
        return columnCountCategories > 0 ? columnCountCategories : getResources().getInteger(R.integer.gfycat_categories_columns_count);
    }

    /**
     * @return true if onBackPressed was handled by GfycatPickerFragment (category closed), false otherwise.
     */
    public final boolean onBackPressed() {
        if (getCurrentCategoriesFragment() != null
                && (getCurrentCategoriesFragment() instanceof OneCategoryFeedFragment)
                || getCurrentCategoriesFragment() instanceof IPhotoMomentsFragment) {
            closeCategory();
            searchController.setSearchViewVisible(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.GfycatPickerFragment);

        accentTint = a.getColor(R.styleable.GfycatPickerFragment_accentTint, ContextCompat.getColor(context, R.color.gfycat_accent_color));
        columnCountGfycats = a.getInteger(R.styleable.GfycatPickerFragment_columnCountGfycats, context.getResources().getInteger(R.integer.gfycat_categories_gfycat_columns_count));
        columnCountCategories = a.getInteger(R.styleable.GfycatPickerFragment_columnCountCategories, context.getResources().getInteger(R.integer.gfycat_categories_columns_count));
        aspectRatio = a.getFloat(R.styleable.GfycatPickerFragment_categoryAspectRatio, DEFAULT_CATEGORY_ASPECT_RATIO);
        closeOnGfycatClick = a.getBoolean(R.styleable.GfycatPickerFragment_closeOnGfycatClick, closeOnGfycatClick);
        recentCategoryEnabled = a.getBoolean(R.styleable.GfycatPickerFragment_recentCategoryEnabled, recentCategoryEnabled);
        orientation = a.getInt(R.styleable.GfycatPickerFragment_pickerOrientation, DEFAULT_ORIENTATION);
        gfycatCornerRadius = a.getDimension(R.styleable.GfycatPickerFragment_gfycatCornerRadius, context.getResources().getDimension(R.dimen.cardview_default_radius));
        categoryCornerRadius = a.getDimension(R.styleable.GfycatPickerFragment_gfycatCategoryCornerRadius, context.getResources().getDimension(R.dimen.cardview_default_radius));

        String onGfycatHandlerName = a.getString(R.styleable.GfycatPickerFragment_onGfycatSelected);
        if (onGfycatHandlerName != null)
            addOnGfycatSelectedListener(new DeclaredOnGfycatClickListener(this, onGfycatHandlerName));

        a.recycle();

        Logging.d(LOG_TAG, "GfycatPickerFragment attributes set to: accentTint=", accentTint,
                "; onGfycatHandlerName=", onGfycatHandlerName, "; columnCountGfycats=", columnCountGfycats,
                "; columnCountCategories=", columnCountCategories);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.gfycat_category_search_fragment_layout, container, false);

        initProgressBar(root);
        initDataLoadListener();

        ViewGroup searchPlace = (ViewGroup) root.findViewById(R.id.gfycat_search_view_place);

        searchController = onCreateSearchView(searchPlace);
        if (searchController == null)
            throw new NullPointerException("onCreateSearchView() must return valid search controller.");
        initSearchController();

        applySearchViewTopMargin(searchPlace, 0);

        if (getCurrentCategoriesFragment() == null) {
            changeFragment(GfycatCategoriesFragment.create(recentCategoryEnabled));
        }

        root.findViewById(R.id.gfycat_category_touch_handler).setOnTouchListener((v, event) -> {
            UIUtils.hideKeyboardForced(v);
            return false;
        });

        onCreateAdditionalViews(inflater, root, savedInstanceState);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        setupAccentTintColor();

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInstanceState(outState);
    }

    public void saveInstanceState(Bundle outState) {
        outState.putString(CURRENT_QUERY_KEY, currentSearchQuery);
        outState.putBoolean(EXTRA_RECENT_CATEGORY_ENABLED, recentCategoryEnabled);
        outState.putBoolean(EXTRA_CLOSE_ON_GFYCATCLICK, closeOnGfycatClick);
        if (currentCategoriesFragment instanceof OneCategoryFeedFragment) {
            FeedIdentifier identifierForSave = ((OneCategoryFeedFragment) currentCategoriesFragment).getTargetIdentifier();
            if (identifierForSave != null) {
                outState.putString(CURRENT_FEED_IDENTIFIER_KEY, identifierForSave.toUniqueIdentifier());
            }
        }
        outState.putBoolean(IS_SEARCHBAR_VISIBLE_KEY, searchController != null && searchController.isSearchViewVisible());
    }

    public void restoreInstanceState(Bundle inState) {
        if (inState == null) return;
        if (inState.containsKey(CURRENT_QUERY_KEY)) {
            currentSearchQuery = inState.getString(CURRENT_QUERY_KEY);
            if (searchController != null) {
                setCurrentSearchQuerySilently(currentSearchQuery);
            }
        }
        if (inState.containsKey(CURRENT_FEED_IDENTIFIER_KEY)) {
            String identifierForRestore = inState.getString(CURRENT_FEED_IDENTIFIER_KEY);
            if (!TextUtils.isEmpty(identifierForRestore)) {
                completeOpenCategory(FeedIdentifierFactory.fromUniqueIdentifier(identifierForRestore));
            }
        }

        recentCategoryEnabled = inState.getBoolean(EXTRA_RECENT_CATEGORY_ENABLED);
        closeOnGfycatClick = inState.getBoolean(EXTRA_CLOSE_ON_GFYCATCLICK);

        searchController.setSearchViewVisible(inState.getBoolean(IS_SEARCHBAR_VISIBLE_KEY, true));
    }

    private void initProgressBar(View root) {
        progressBar = (ProgressBar) root.findViewById(R.id.gfycat_categories_search_progress);
        applySearchViewTopMargin(progressBar, (int) getResources().getDimension(R.dimen.gfycat_categories_search_progress_margin));
        progressBarController = new ProgressBarController(progressBar);
    }

    private void initSearchController() {
        searchController.setSearchControllerListener(new SearchControllerListener() {
            @Override
            public void onQueryTextChange(String newText) {
                applyNewSearchQuery(newText, false);
            }

            @Override
            public void onClearClicked() {
                closeSearch();
            }

            @Override
            public void onSearchClicked(String text) {
                applyNewSearchQuery(text, true);
            }
        });
    }

    /**
     * Set current search query to specific value.
     */
    private void setCurrentSearchQuerySilently(String newText) {
        currentSearchQuery = newText;
        searchController.setSearchQuery(newText);
    }

    /**
     * Apply new search query internally.
     *
     * @param newText
     */
    private void applyNewSearchQuery(@Nullable String newText, boolean isForceSearch) {
        String newQuery = newText;
        if (newQuery == null) {
            newQuery = "";
        }
        if (!currentSearchQuery.equals(newQuery) || isForceSearch) {
            getCurrentCategoriesFragment().setFilter(newQuery);
            currentSearchQuery = newQuery;

            boolean withDelay = !isForceSearch;
            applyNoCategoriesRule(withDelay);
            applyExitFromCategoryOnEmptySearch(withDelay);
        }
    }

    private void closeSearch() {
        searchController.setSearchQuery("");
        UIUtils.hideKeyboardForced(root);
        if (currentCategoriesFragment instanceof OneCategoryFeedFragment) {
            changeFragment(GfycatCategoriesFragment.create(recentCategoryEnabled));
            insideCategory = false;
        }
    }

    /**
     * If all categories are filtered by search query.
     * We should do search by keyword, so we should open OneCategory fragment.
     */
    private void applyNoCategoriesRule(boolean withDelay) {
        root.removeCallbacks(goToSearchInCategory);
        long delay = withDelay ? NO_CATEGORY_RULE_DELAY : 0;
        if (currentCategoriesFragment instanceof GfycatCategoriesFragment && ((GfycatCategoriesFragment) currentCategoriesFragment).isAllCategoriesFilteredState()) {
            root.postDelayed(goToSearchInCategory, delay);
        }
    }

    /**
     * If searchQuery is empty and we are inside category, we should exit to categories view.
     */
    private void applyExitFromCategoryOnEmptySearch(boolean withDelay) {
        root.removeCallbacks(exitFromCategoryOnEmptySearch);
        long delay = withDelay ? EMPTY_SEARCH_RULE_DELAY : 0;
        if (shouldExitFromCategory()) {
            root.postDelayed(exitFromCategoryOnEmptySearch, delay);
        }
    }

    private void initDataLoadListener() {
        dataLoadProgressListener = new DataLoadProgressListener() {
            @Override
            public void onDataLoadStarted() {
                progressBarController.show();
            }

            @Override
            public void onDataLoadFinished() {
                progressBarController.hide();
                applyNoCategoriesRule(true);
            }

            @Override
            public void onDataLoadError() {
                progressBarController.hide();
            }
        };
    }

    private void applySearchViewTopMargin(View view, int additional) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.topMargin = getSearchViewTopPadding() + additional;
    }

    private CategoriesFragment getCurrentCategoriesFragment() {
        if (currentCategoriesFragment == null && isAdded()) {
            currentCategoriesFragment = (CategoriesFragment) getChildFragmentManager().findFragmentById(R.id.gfycat_category_fragment_placeholder);
        }
        return currentCategoriesFragment;
    }

    @Override
    public final void onCategoryClick(GfycatCategory category) {
        Logging.d(LOG_TAG, "onCategoryClick(", category.getTag(), ")");

        LazyLogger.get().logTapCategory(category.getTag());

        /**
         * We do not want not trigger actual search.
         */
        setCurrentSearchQuerySilently(category.getTagText());

        FeedIdentifier feedIdentifier = resolveFeedIdentifier(category);
        String digest = TRENDING_TAG.equals(category.getTag()) ? category.getDigest() : "";

        // recent is locally cached and should be created at this point, so open it immediately
        boolean isRecentFeed = RecentFeedIdentifier.recent().getType().equals(feedIdentifier.getType());
        if (isRecentFeed || category.getGfycat() == null) {
            completeOpenCategory(feedIdentifier);
        } else {
            GfyPrivateHelper.getFeedManagerImpl().toObservable()
                    .flatMap(feedManager -> feedManager.createFeedIfNotExist(feedIdentifier, category.getGfycat(), digest, CloseMode.Open))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            aVoid -> Function.ignore(),
                            throwable -> completeOpenCategory(feedIdentifier),
                            () -> completeOpenCategory(feedIdentifier));
        }
    }

    @Override
    public void onPhotoMomentsCategoryClick() {
        PhotoMomentsUiFactory photoMomentsUiFactory = new PhotoMomentsUiFactoryInitializer().getPhotoMomentsUiFactory(getActivity());
        if (photoMomentsUiFactory != null) {
            changeFragment(photoMomentsUiFactory.createPhotoMomentsFragment());
            insideCategory = false;
        }
    }

    private FeedIdentifier resolveFeedIdentifier(GfycatCategory category) {
        if (TRENDING_TAG.equals(category.getTag())) {
            return PublicFeedIdentifier.fromReaction(TRENDING_TAG);
        } else if (RecentFeedIdentifier.RECENT_FEED_TYPE.getName().equals(category.getTag())) {
            return RecentFeedIdentifier.recent();
        } else {
            return PublicFeedIdentifier.fromSearch(category.getTag());
        }
    }

    private void completeOpenCategory(FeedIdentifier identifier) {
        Assertions.assertUIThread(IllegalStateException::new);
        changeFragment(OneCategoryFeedFragment.create(identifier));
        insideCategory = true;
    }

    private final <T extends Fragment & CategoriesFragment> void changeFragment(@NonNull T fragment) {
        if (!isAdded() || (currentCategoriesFragment != null && fragment.getClass().equals(currentCategoriesFragment.getClass())))
            return;

        if (fragment instanceof IPhotoMomentsFragment) {
            searchController.setSearchViewVisible(false);
            progressBarController.hide();
        }

        currentCategoriesFragment = fragment;
        FragmentTransaction transaction = getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.gfycat_category_fragment_placeholder, fragment);

        transaction.commitAllowingStateLoss();

        if (scrollListener != null) {
            fragment.setScrollListener(scrollListener);
        }
        if (dataLoadProgressListener != null) {
            fragment.setDataLoadProgressListener(dataLoadProgressListener);
        }
    }

    @Override
    public final void onGfycatClick(FeedIdentifier identifier, Gfycat gfycat, int position) {
        LazyLogger.get().logSendGfycat(gfycat.getGfyId(), getSearchFilter(), getSendGfycatSource(identifier));

        onGfycatSelected(identifier, gfycat, position);

        for (OnGfycatSelectedListener listener : onGfycatSelectedListeners) {
            if (listener != null) {
                listener.onGfycatSelected(identifier, gfycat, position);
            }
        }

        if (recentCategoryEnabled) {
            GfyPrivateHelper.getFeedManagerImpl()
                    .flatMapCompletable(feedManager -> feedManager.addRecentGfycat(gfycat))
                    .subscribe();
        }

        if (closeOnGfycatClick) {
            closeCategory();
        }
    }

    @SendGfycatSource
    private String getSendGfycatSource(FeedIdentifier identifier) {
        return insideCategory ? KeyboardLogger.SOURCE_VALUE_CATEGORY : KeyboardLogger.SOURCE_VALUE_SEARCH;
    }

    protected final void setupScrollListener(RecyclerView.OnScrollListener scrollListener) {
        this.scrollListener = scrollListener;
        Sugar.doIfNotNull(getCurrentCategoriesFragment(), categoriesFragment -> categoriesFragment.setScrollListener(scrollListener));
    }

    protected final RecyclerView getRecyclerView() {
        return Sugar.callIfNotNull(getCurrentCategoriesFragment(), CategoriesFragment::getRecyclerView);
    }

    private boolean shouldExitFromCategory() {
        return TextUtils.isEmpty(getSearchFilter()) && currentCategoriesFragment instanceof OneCategoryFeedFragment;
    }

    public interface OnGfycatSelectedListener {
        /**
         * Called when user clicked on gfycat in identifier.getName() category.
         */
        void onGfycatSelected(FeedIdentifier identifier, Gfycat gfycat, int position);
    }

    private class ExitFromCategoryOnEmptySearch implements Runnable {
        @Override
        public void run() {
            Logging.d(LOG_TAG, "ExitFromCategoryOnEmptySearch::run()");
            if (shouldExitFromCategory() && isResumed()) {
                changeFragment(GfycatCategoriesFragment.create(recentCategoryEnabled));
                insideCategory = false;
            }
        }
    }

    private class GoToSearchInCategory implements Runnable {
        @Override
        public void run() {
            Logging.d(LOG_TAG, "GoToSearchInCategory::run()");
            if (isResumed()) {
                changeFragment(OneCategoryFeedFragment.create(PublicFeedIdentifier.fromSearch(searchController.getSearchQuery())));
                insideCategory = false;
            }
        }
    }

    private class DeclaredOnGfycatClickListener implements OnGfycatSelectedListener {
        private final GfycatPickerFragment mHostView;
        private final String mMethodName;

        private Method mResolvedMethod;
        private Context mResolvedContext;

        public DeclaredOnGfycatClickListener(@NonNull GfycatPickerFragment hostView, @NonNull String methodName) {
            mHostView = hostView;
            mMethodName = methodName;
        }

        @NonNull
        private void resolveMethod(@Nullable Context context) {
            while (context != null) {
                try {
                    if (!context.isRestricted()) {
                        final Method method = context.getClass().getMethod(mMethodName, FeedIdentifier.class, Gfycat.class, int.class);
                        if (method != null) {
                            mResolvedMethod = method;
                            mResolvedContext = context;
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // Failed to find method, keep searching up the hierarchy.
                }

                if (context instanceof ContextWrapper) {
                    context = ((ContextWrapper) context).getBaseContext();
                } else {
                    // Can't search up the hierarchy, null out and fail.
                    context = null;
                }
            }

            throw new IllegalStateException("Could not find method " + mMethodName
                    + "(FeedIdentifier, Gfycat, int) in a parent or ancestor Context for app:onGfycatSelected "
                    + "attribute defined on view " + mHostView.getClass());
        }

        @Override
        public void onGfycatSelected(FeedIdentifier identifier, Gfycat gfycat, int position) {
            if (mResolvedMethod == null) {
                resolveMethod(mHostView.getContext());
            }

            try {
                mResolvedMethod.invoke(mResolvedContext, identifier, gfycat, position);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "Could not execute non-public method for app:onGfycatSelected", e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(
                        "Could not execute method for app:onGfycatSelected", e);
            }
        }
    }
}
