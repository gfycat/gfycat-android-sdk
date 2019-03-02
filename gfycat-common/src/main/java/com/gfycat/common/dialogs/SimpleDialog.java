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

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

/**
 * Simple dialog with title, details and ok button.
 * <p>
 * Created by dekalo on 14.12.15.
 */
public class SimpleDialog extends CustomizableDialogFragment {

    private static final String DIALOG_TITLE = "DIALOG_TITLE";
    private static final String DIALOG_MESSAGE = "DIALOG_MESSAGE";

    public SimpleDialog prepareArgs(String title, String message) {
        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putString(DIALOG_MESSAGE, message);
        setArguments(args);
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(DIALOG_TITLE))
                .setMessage(getArguments().getString(DIALOG_MESSAGE))
                .setPositiveButton(getOkButtonText(), (dialog, which) -> onOk())
                .create();
    }

    protected void onOk() {
        dismiss();
    }

    protected String getOkButtonText() {
        return getString(android.R.string.ok);
    }
}
