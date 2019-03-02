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

import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.UIUtils;

/**
 * Serves for making show/hide less choppy (avoid showing a bar for a tiny period of time).
 * No more blinking!
 * <p>
 * Created by dgoliy on 2/3/17.
 */

public class ProgressBarController {
    private static final String LOG_TAG = "ProgressBarController";
    private final int DELAY_START_SHOW_MILLIS = 500;
    private final int DURATION_MIN_SHOW_MILLIS = 2000;

    private long currentShowStartTimeMillis = 0;

    private ProgressBar progressBar;

    private Handler handler = new Handler();
    private Runnable runnableShow = () -> {
        if (progressBar == null) {
            return;
        }

        animateShow();
    };
    private Runnable runnableHide = () -> {
        if (progressBar == null) {
            return;
        }

        currentShowStartTimeMillis = 0;
        animateHide();
    };

    public ProgressBarController(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void show() {
        Logging.d(LOG_TAG, "show()");
        handler.removeCallbacks(runnableHide);
        if (progressBar.getVisibility() == View.VISIBLE) {
            return;
        }
        long delay = DELAY_START_SHOW_MILLIS - (System.currentTimeMillis() - currentShowStartTimeMillis);
        if (delay <= 0) {
            currentShowStartTimeMillis = System.currentTimeMillis();
            handler.postDelayed(runnableShow, DELAY_START_SHOW_MILLIS);
        }
    }

    public void hide() {
        Logging.d(LOG_TAG, "hide()");
        if (progressBar.getVisibility() == View.GONE) {
            handler.removeCallbacks(runnableShow);
            return;
        }
        long delay = DURATION_MIN_SHOW_MILLIS - (System.currentTimeMillis() - currentShowStartTimeMillis);
        handler.removeCallbacks(runnableHide);
        if (delay <= 0) {
            handler.post(runnableHide);
        } else {
            handler.postDelayed(runnableHide, delay);
        }
    }

    private void animateShow() {
        UIUtils.showView(progressBar, true);
    }

    private void animateHide() {
        UIUtils.hideView(progressBar, true);
    }
}