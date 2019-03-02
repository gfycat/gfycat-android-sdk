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

package com.gfycat.core.storage;

import com.gfycat.common.ContextDetails;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by dgoliy on 4/11/17.
 */

public class MediaFilesManagerAsyncWrapper implements MediaFilesManager {
    private ReplaySubject<MediaFilesManager> subject = ReplaySubject.create();

    public void init(MediaFilesManager mediaFilesManager) {
        if (subject.hasComplete()) {
            return;
        }
        subject.onNext(mediaFilesManager);
        subject.onComplete();
    }

    @Override
    public Single<byte[]> loadAsByteArray(Gfycat gfycat, MediaType mediaType) {
        return subject.singleOrError().flatMap(manager -> manager.loadAsByteArray(gfycat, mediaType));
    }

    @Override
    public Single<File> loadAsFile(Gfycat gfycat, MediaType mediaType) {
        return subject.singleOrError().flatMap(manager -> manager.loadAsFile(gfycat, mediaType));
    }

    @Override
    public Single<byte[]> loadAsByteArray(Gfycat gfycat, MediaType mediaType, ContextDetails contextDetails) {
        return subject.singleOrError().flatMap(manager -> manager.loadAsByteArray(gfycat, mediaType, contextDetails));
    }

    @Override
    public Single<File> loadAsFile(Gfycat gfycat, MediaType mediaType, ContextDetails contextDetails) {
        return subject.singleOrError().flatMap(manager -> manager.loadAsFile(gfycat, mediaType, contextDetails));
    }
}
