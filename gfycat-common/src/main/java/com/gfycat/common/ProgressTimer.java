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

package com.gfycat.common;

import android.os.CountDownTimer;

/**
 * Created by dekalo on 07.11.16.
 */

public class ProgressTimer extends CountDownTimer {

    private static final long TIMER_TICK = 16;
    private final long duration;
    private long timePassed = 0;

    private final Action1<Float> progressAction;
    private final Action0 finishAction;

    public ProgressTimer(long millisInFuture, Action1<Float> progressAction, Action0 finishAction) {
        this(millisInFuture, TIMER_TICK, progressAction, finishAction);
    }

    public ProgressTimer(long millisInFuture, long countDownInterval, Action1<Float> progressAction, Action0 finishAction) {
        super(millisInFuture, countDownInterval);
        duration = millisInFuture;
        this.progressAction = progressAction;
        this.finishAction = finishAction;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        progressAction.call((duration - millisUntilFinished) / (float) duration);
        timePassed = duration - millisUntilFinished;
    }

    public long getTimePassed() {
        return timePassed;
    }

    @Override
    public void onFinish() {
        progressAction.call(1f);
        timePassed = duration;
        finishAction.call();
    }
}
