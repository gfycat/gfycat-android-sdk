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
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

public class ProgressDialogFragment extends CustomizableDialogFragment {

    private static final String PROGRESS_DIALOG = ProgressDialogFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setTitle(getTitle());
        dialog.setMessage(getMessage());
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    public static void show(AppCompatActivity activity, String title, String message) {
        new ProgressDialogFragment()
                .title(title)
                .message(message)
                .show(activity.getSupportFragmentManager(), PROGRESS_DIALOG);
    }

    public static void show(AppCompatActivity activity, String title, String message, OnDismissCallback onDismissCallback) {
        new ProgressDialogFragment()
                .title(title)
                .message(message)
                .withDismissCallback(onDismissCallback)
                .show(activity.getSupportFragmentManager(), PROGRESS_DIALOG);
    }

    public static void hide(AppCompatActivity activity) {
        ProgressDialogFragment progressDialog = (ProgressDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG);
        if (progressDialog != null) {
            activity.getSupportFragmentManager().beginTransaction().remove(progressDialog).commit();
        }
    }

    @Override
    public void changeTitle(String title) {
        super.changeTitle(title);
        if (getDialog() != null) getDialog().setTitle(title);
    }

    @Override
    public void changeMessage(String message) {
        super.changeMessage(message);
        if (getDialog() != null) ((ProgressDialog) getDialog()).setMessage(message);
    }

    public static boolean isShown(AppCompatActivity activity) {
        return activity != null
                && activity.getSupportFragmentManager() != null
                && activity.getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG) != null;
    }
}