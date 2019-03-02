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

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by dekalo on 26.10.15.
 */
public abstract class RxScrollListener extends RecyclerView.OnScrollListener {

    protected abstract void onActionCall(RecyclerView recyclerView);

    protected abstract Observable<RecyclerView> configure(Observable<RecyclerView> initial);

    private final ScrollEventsEmitter emitter = new ScrollEventsEmitter();
    private final Disposable disposable;

    public RxScrollListener() {
        disposable = configure(Observable.create(emitter))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onActionCall);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        emitter.emit(recyclerView);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            emitter.emit(recyclerView);
        }
    }


    public void forceUpdate(RecyclerView recyclerView) {
        emitter.emit(recyclerView);
    }

    private class ScrollEventsEmitter implements ObservableOnSubscribe<RecyclerView> {

        private Emitter<? super RecyclerView> emitter;

        @Override
        public void subscribe(ObservableEmitter<RecyclerView> emitter) throws Exception {
            this.emitter = emitter;
        }

        public void emit(RecyclerView recyclerView) {
            emitter.onNext(recyclerView);
        }
    }
}
