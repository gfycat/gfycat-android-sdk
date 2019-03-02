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

package com.gfycat.common.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.gfycat.common.utils.Assertions;

/**
 * Created by dekalo on 18.01.16.
 */
public class CustomizableDialogFragment extends DialogFragment {

    protected static final String DIALOG_TITLE = "DIALOG_TITLE";
    protected static final String DIALOG_MESSAGE = "DIALOG_MESSAGE";
    protected static final String DIALOG_POSITIVE = "DIALOG_POSITIVE";
    protected static final String DIALOG_NEGATIVE = "DIALOG_NEGATIVE";
    protected static final String DIALOG_IS_CANCELABLE = "DIALOG_IS_CANCELABLE";
    protected static final String BACK_PRESS_ON_DISMISS = "BACK_PRESS_ON_DISMISS";
    protected static final String DISMISS_INTENT_KEY = "DISMISS_INTENT_KEY";

    private String title, message, positive, negative;

    private OnDismissCallback onDismissCallback;

    protected Bundle arguments() {
        Bundle result = getArguments();
        if (result == null) {
            setArguments(new Bundle());
            result = getArguments();
        }
        Assertions.assertNotNull(result, NullPointerException::new);
        return result;
    }

    public CustomizableDialogFragment withDismissCallback(OnDismissCallback dismissCallback) {
        this.onDismissCallback = dismissCallback;
        return this;
    }

    public CustomizableDialogFragment message(String message) {
        arguments().putString(DIALOG_MESSAGE, message);
        return this;
    }

    public CustomizableDialogFragment title(String title) {
        arguments().putString(DIALOG_TITLE, title);
        return this;
    }

    public CustomizableDialogFragment prepareBackPressOnDismiss(boolean backPressOnDismiss) {
        arguments().putBoolean(BACK_PRESS_ON_DISMISS, backPressOnDismiss);
        return this;
    }

    public CustomizableDialogFragment prepareAsCancelable(boolean cancelable) {
        arguments().putBoolean(DIALOG_IS_CANCELABLE, cancelable);
        return this;
    }

    public CustomizableDialogFragment setDismissIntent(Intent dismissIntent) {
        arguments().putParcelable(DISMISS_INTENT_KEY, dismissIntent);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeMessage(String message) {
        this.message = message;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(arguments().getBoolean(DIALOG_IS_CANCELABLE, true));
        title = getArguments().getString(DIALOG_TITLE);
        message = getArguments().getString(DIALOG_MESSAGE);
        positive = getArguments().getString(DIALOG_POSITIVE);
        negative = getArguments().getString(DIALOG_NEGATIVE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.getString(DIALOG_TITLE, title);
        outState.getString(DIALOG_MESSAGE, message);
        outState.getString(DIALOG_POSITIVE, positive);
        outState.getString(DIALOG_NEGATIVE, negative);
    }

    public void setOnDismissCallback(OnDismissCallback onDismissCallback) {
        this.onDismissCallback = onDismissCallback;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (onDismissCallback != null)
            onDismissCallback.onDismiss();

        if (getActivity() == null)
            return;

        if (arguments().getParcelable(DISMISS_INTENT_KEY) != null)
            getActivity().startActivity(arguments().getParcelable(DISMISS_INTENT_KEY));

        if (arguments().getBoolean(BACK_PRESS_ON_DISMISS))
            getActivity().onBackPressed();
    }
}
