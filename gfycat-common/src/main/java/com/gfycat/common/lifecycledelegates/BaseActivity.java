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

package com.gfycat.common.lifecycledelegates;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Delegates holder.
 * <p>
 * Created by dekalo on 30.09.15.
 */
public abstract class BaseActivity extends AppCompatActivity implements DelegateHolder {

    private final List<LifecycleDelegate> lifecycleDelegateList = new ArrayList<>();

    public BaseActivity() {
    }

    protected void addDelegates(LifecycleDelegate... lifecycleDelegate) {
        lifecycleDelegateList.addAll(Arrays.asList(lifecycleDelegate));
    }

    protected void addDelegate(LifecycleDelegate lifecycleDelegate) {
        lifecycleDelegateList.add(lifecycleDelegate);
    }

    public <T> T getDelegate(Class<T> clazz) {
        for (LifecycleDelegate delegate : lifecycleDelegateList) {
            if (clazz.isInstance(delegate)) {
                return (T) delegate;
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (LifecycleDelegate delegate : lifecycleDelegateList) {
            delegate.onCreate(savedInstanceState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        for (LifecycleDelegate delegate : lifecycleDelegateList) {
            delegate.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (LifecycleDelegate delegate : lifecycleDelegateList) {
            delegate.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (LifecycleDelegate delegate : lifecycleDelegateList) {
            delegate.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (LifecycleDelegate delegate : lifecycleDelegateList) {
            delegate.onStop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (LifecycleDelegate delegate : lifecycleDelegateList) {
            delegate.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (LifecycleDelegate delegate : lifecycleDelegateList) {
            delegate.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (LifecycleDelegate delegate : lifecycleDelegateList) {
            delegate.onDestroy();
        }
    }

}
