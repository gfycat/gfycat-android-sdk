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

package com.gfycat.picker.search;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.gfycat.common.utils.UIUtils;
import com.gfycat.picker.R;

/**
 * Created by dekalo on 03.02.17.
 */

public class DefaultSearchCategoryView extends RelativeLayout implements SearchController {

    private EditText searchText;
    private ImageButton closeButton;
    private ImageButton searchIcon;
    private int hintPadding, textPadding;

    private SearchControllerListener searchControllerListener;

    public DefaultSearchCategoryView(Context context) {
        super(context);
        sharedCtor(context);
    }

    public DefaultSearchCategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedCtor(context);
    }

    public DefaultSearchCategoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedCtor(context);
    }

    private void sharedCtor(Context context) {
        LayoutInflater.from(context).inflate(R.layout.gfycat_default_search_category_view_layout, this, true);

        searchText = (EditText) findViewById(R.id.gfycat_search_text);
        closeButton = (ImageButton) findViewById(R.id.gfycat_search_close_btn);
        closeButton.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.gfycat_clear_material));

        searchIcon = (ImageButton) findViewById(R.id.gfycat_search_btn);
        searchIcon.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.gfycat_search_material));
        UIUtils.setDrawableTint(context, searchIcon.getDrawable(), R.color.gfycat_search);

        hintPadding = getResources().getDimensionPixelOffset(R.dimen.gfycat_hint_padding_in_search_view);
        textPadding = getResources().getDimensionPixelOffset(R.dimen.gfycat_text_padding_in_search_view);

        closeButton.setOnClickListener(v -> searchControllerListener.onClearClicked());

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchControllerListener.onQueryTextChange(s.toString());
                applyControlState(s.length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        applyControlState(false);

        searchText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String text = searchText.getText().toString();
                if (searchControllerListener != null) {
                    searchControllerListener.onSearchClicked(text);
                }
                UIUtils.hideKeyboardForced(searchText);
                searchText.clearFocus();
                return true;
            }
            return false;
        });
    }

    @Override
    public void setSearchControllerListener(SearchControllerListener searchControllerListener) {
        this.searchControllerListener = safe(searchControllerListener);
    }

    @Override
    public int getSearchHeight() {
        return getResources().getDimensionPixelOffset(R.dimen.gfycat_default_search_view_height);
    }

    @Override
    public void setAccentTintColor(int color) {
        UIUtils.setDrawableTint(closeButton.getDrawable(), color);
    }

    @Override
    public void setSearchViewVisible(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        setVisibility(visibility);
    }

    @Override
    public boolean isSearchViewVisible() {
        return getVisibility() == View.VISIBLE;
    }

    private SearchControllerListener safe(SearchControllerListener searchControllerListener) {
        return searchControllerListener == null ? new NoSearchControllerListener() : searchControllerListener;
    }

    @Override
    public void setSearchQuery(String query) {
        searchText.setText(query);
        searchText.setSelection(searchText.getText().length());
        searchText.clearFocus();
    }

    @Override
    public String getSearchQuery() {
        return searchText.getText().toString();
    }

    private void applyControlState(boolean isInFocus) {
        closeButton.setVisibility(isInFocus ? VISIBLE : GONE);
        searchIcon.setVisibility(isInFocus ? GONE : VISIBLE);

        searchText.setPadding(isInFocus ? textPadding : hintPadding,
                searchText.getPaddingTop(),
                searchText.getPaddingRight(),
                searchText.getPaddingBottom());
    }
}
