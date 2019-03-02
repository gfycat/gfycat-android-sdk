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

package com.gfycat.core.downloading;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Created by Andrew Khloponin
 */
public class PrevRequest {
    private Observable prevRequestObs;
    private Consumer prevRequestOnNext;
    private Consumer<Throwable> prevRequestOnError;
    private boolean lastRequestFailed = false;


    public PrevRequest(Observable prevRequestObs, Consumer prevRequestOnNext, Consumer<Throwable> prevRequestOnError) {
        this.prevRequestObs = prevRequestObs;
        this.prevRequestOnNext = prevRequestOnNext;
        this.prevRequestOnError = prevRequestOnError;
    }


    public Observable getPrevRequestObs() {
        return prevRequestObs;
    }


    public boolean isLastRequestFailed() {
        return lastRequestFailed;
    }

    public void setLastRequestFailed(boolean lastRequestFailed) {
        this.lastRequestFailed = lastRequestFailed;
    }


    public boolean doRequest() {
        if (!lastRequestFailed) return false;
        lastRequestFailed = false;
        if (prevRequestObs != null)
            prevRequestObs.subscribe(prevRequestOnNext, prevRequestOnError);
        return true;
    }


    private <T> void savePrevRequest(Observable<T> observable, Consumer<T> onNext, Consumer<Throwable> onError) {
        this.prevRequestObs = observable;
        this.prevRequestOnNext = onNext;
        this.prevRequestOnError = onError;
    }
}
