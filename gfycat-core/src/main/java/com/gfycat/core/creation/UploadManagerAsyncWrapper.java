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

package com.gfycat.core.creation;

import android.support.annotation.NonNull;

import com.gfycat.core.creation.pojo.CreateGfycatRequest;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.io.InputStream;

import io.reactivex.subjects.ReplaySubject;

/**
 * Created by dgoliy on 4/11/17.
 */

public class UploadManagerAsyncWrapper implements UploadManager {

    private ReplaySubject<UploadManager> subject = ReplaySubject.create();

    public void init(UploadManager uploadManager) {
        if (subject.hasComplete()) {
            return;
        }
        subject.onNext(uploadManager);
        subject.onComplete();
    }

    @Override
    public String requestCreationKey(CreateGfycatRequest creationParams) throws CanNotCreateKeyException {
        return subject.blockingFirst().requestCreationKey(creationParams);
    }

    @Override
    public void upload(String creationKey, InputStream inputStream) throws CanNotUploadGfycatException {
        subject.blockingFirst().upload(creationKey, inputStream);
    }

    @Override
    public void upload(String creationKey, InputStream inputStream, UploadListener uploadListener) throws CanNotUploadGfycatException {
        subject.blockingFirst().upload(creationKey, inputStream, uploadListener);
    }

    @Override
    public Gfycat waitUntilReady(@NonNull String creationKey, long timeout) throws CanNotGetGfycatStatusException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException {
        return subject.blockingFirst().waitUntilReady(creationKey, timeout);
    }

    @Override
    public Gfycat waitUntilReady(@NonNull String creationKey) throws CanNotGetGfycatStatusException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException {
        return subject.blockingFirst().waitUntilReady(creationKey);
    }

    @Override
    public Gfycat getGfycatIfReady(String gfyName) {
        return subject.blockingFirst().getGfycatIfReady(gfyName);
    }

    @Override
    public boolean isGfycatReady(String gfyName) {
        return subject.blockingFirst().isGfycatReady(gfyName);
    }

    @Override
    public Gfycat createGfycat(CreateGfycatRequest creationParams, InputStream inputStream) throws CanNotCreateKeyException, CanNotGetGfycatStatusException, CanNotUploadGfycatException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException {
        return subject.blockingFirst().createGfycat(creationParams, inputStream);
    }

    @Override
    public Gfycat createGfycat(CreateGfycatRequest creationParams, InputStream inputStream, UploadListener uploadListener) throws CanNotCreateKeyException, CanNotGetGfycatStatusException, CanNotUploadGfycatException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException {
        return subject.blockingFirst().createGfycat(creationParams, inputStream, uploadListener);
    }
}
