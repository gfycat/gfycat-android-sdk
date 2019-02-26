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

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;


/**
 * Created by dekalo on 18.05.17.
 */

public class RxContentObserver {

    public static Observable<Uri> create(Context context, Uri uri, boolean notifyForDescendants, Handler handler) {
        return Observable.create(new ObservableOnSubscribe<Uri>() {

            public ContentObserver observer;

            @Override
            public void subscribe(ObservableEmitter<Uri> emitter) throws Exception {
                context.getContentResolver().registerContentObserver(uri, notifyForDescendants, observer = new ContentObserver(handler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        emitter.onNext(uri);
                    }
                });

                emitter.setCancellable(() -> context.getContentResolver().unregisterContentObserver(observer));
            }
        });
    }

    public static Observable<Uri> create(Context context, Uri uri) {
        return create(context, uri, false, new Handler());
    }

    public static Observable<Uri> create(Context context, Uri uri, boolean notifyForDescedants) {
        return create(context, uri, notifyForDescedants, new Handler());
    }

    public static Observable<Uri> createOnUI(Context context, Uri uri) {
        return Observable.fromCallable(Handler::new)
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(handler -> create(context, uri, false, handler));
    }
}
