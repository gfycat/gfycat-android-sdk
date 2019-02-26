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

import com.gfycat.common.ChainedException;
import com.gfycat.common.ContextDetails;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Provides access to Gfycat media cache files.
 * <p>
 * !!! Warning: Cache files are for READ ONLY purpose. <u>Do not modify or delete them!</u>
 */
public class CachedMediaFilesManager implements MediaFilesManager {

    private static final String LOG_TAG = "CachedMediaFilesManager";
    private static final int FOBIDDEN_CODE = 403;

    private final Map<String, BehaviorSubject<File>> ongoingDownloads = new HashMap<>();
    private final DiskCache diskCache;
    private final MediaApi mediaApi;

    public CachedMediaFilesManager(OkHttpClient videoClient, DiskCache diskCache) {
        this.diskCache = diskCache;
        this.mediaApi = new Retrofit.Builder()
                .client(videoClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("https://not_used")
                .build()
                .create(MediaApi.class);
    }

    @Override
    public Single<byte[]> loadAsByteArray(Gfycat gfycat, MediaType mediaType) {
        return loadAsByteArray(gfycat, mediaType, new ContextDetails());
    }

    @Override
    public Single<byte[]> loadAsByteArray(Gfycat gfycat, MediaType mediaType, ContextDetails contextDetails) {
        return loadAsByteArray(
                mediaType.getUrl(gfycat),
                mediaType.getVideoStorageId(gfycat),
                contextDetails.copy()
                        .put("loadingType", "loadAsByteArray")
                        .put("mediaType", mediaType.getName())
                        .put("gfyId", gfycat.getGfyId()));
    }

    @Override
    public Single<File> loadAsFile(Gfycat gfycat, MediaType mediaType) {
        return loadAsFile(gfycat, mediaType, new ContextDetails());
    }

    @Override
    public Single<File> loadAsFile(Gfycat gfycat, MediaType mediaType, ContextDetails contextDetails) {
        return loadAsFile(
                mediaType.getUrl(gfycat),
                mediaType.getVideoStorageId(gfycat),
                contextDetails.copy()
                        .put("loadingType", "loadAsFile")
                        .put("mediaType", mediaType.getName())
                        .put("gfyId", gfycat.getGfyId()));
    }

    private Single<File> loadAsFile(String resource, String fileKey, ContextDetails contextDetails) {
        return findFileInCacheObservable(fileKey, contextDetails)
                .onErrorResumeNext(findFileInPendingDownloadsOrDownload(resource, fileKey, contextDetails));
    }

    private Single<byte[]> loadAsByteArray(String resource, String uniqueKey, ContextDetails contextDetails) {
        return loadAsFile(resource, uniqueKey, contextDetails)
                .flatMap(CachedMediaFilesManager::loadFileInMemory)
                .onErrorResumeNext((Function<Throwable, SingleSource<byte[]>>) throwable -> {
                    if (throwable instanceof InterruptedIOException) {
                        return Single.error(throwable);
                    } else {
                        return loadFromNetworkAsByteArray(resource, contextDetails);
                    }
                });
    }

    private Single<byte[]> loadFromNetworkAsByteArray(String resource, ContextDetails contextDetails) {
        return mediaApi.load(resource)
                .map(ResponseBody::bytes)
                .onErrorResumeNext(throwable -> processErrors(throwable, resource, contextDetails));
    }

    private <T> Single<T> processErrors(Throwable throwable, String resource, ContextDetails contextDetails) {

        if (throwable instanceof HttpException && ((HttpException) throwable).code() == FOBIDDEN_CODE) {
            return Single.error(new ForbiddenGfycatException("Resource " + resource + " return 403. " + contextDetails));
        } else if (throwable instanceof InterruptedIOException) {
            Logging.d(LOG_TAG, "InterruptedIOException for " + resource + " " + contextDetails);
            return Single.error(throwable);
        } else if (throwable instanceof IOException) {
            String message = "IOException for " + resource + " " + contextDetails;
            return Single.error(new IllegalStateException(message, throwable));
        } else if (throwable instanceof DefaultDiskCache.NotValidCacheException) {
            // possible situation if available space size is less than 1%
            return Single.error(new ChainedException(throwable));
        } else if (throwable instanceof DefaultDiskCache.OtherEditInProgressException) {
            String message = "DefaultDiskCache for " + resource + " " + contextDetails;
            Assertions.fail(new RuntimeException(message, throwable));
            return Single.error(new ChainedException(throwable));
        } else {
            String message = "Other Exception for " + resource + " " + contextDetails;
            return Single.error(new IllegalStateException(message, throwable));
        }
    }

    private static Single<byte[]> loadFileInMemory(File file) {
        try {
            return Single.just(FileUtils.readFileToByteArray(file));
        } catch (IOException e) {
            return Single.error(e);
        }
    }

    private Single<File> findFileInCacheObservable(String fileKey, ContextDetails contextDetails) {

        return Single.create((SingleOnSubscribe<File>) subscriber -> {
            File file = diskCache.get(fileKey);
            if (file != null) {
                Logging.d(LOG_TAG, "Cache hit for: ", fileKey, " ", contextDetails);
                subscriber.onSuccess(file);
            } else {
                Logging.d(LOG_TAG, "No ", fileKey, " in cache.", " ", contextDetails);
                subscriber.tryOnError(new RuntimeException("No such file in cache"));
            }
        }).doOnSubscribe(ignored -> Logging.d(LOG_TAG, "findFileInCacheObservable(", fileKey, ") doOnSubscribe", " ", contextDetails));

    }

    private Single<? extends File> findFileInPendingDownloadsOrDownload(String resource, String fileKey, ContextDetails contextDetails) {
        return Single.create(subscriber -> {

            BehaviorSubject<File> behaviorSubject;
            boolean alreadyDownloading;

            synchronized (ongoingDownloads) {
                behaviorSubject = ongoingDownloads.get(fileKey);
                alreadyDownloading = behaviorSubject != null;
                Logging.d(LOG_TAG, "pending downloads ", fileKey, " alreadyDownloading = ", alreadyDownloading, " ", contextDetails);
                if (!alreadyDownloading) {
                    ongoingDownloads.put(fileKey, behaviorSubject = BehaviorSubject.create());
                    Logging.d(LOG_TAG, "save behaviorSubject to ongoingDownloads ", fileKey, " ", contextDetails);
                }
            }

            behaviorSubject.subscribe(subscriber::onSuccess, subscriber::tryOnError);

            if (!alreadyDownloading) {

                final BehaviorSubject<File> finalSubject = behaviorSubject;

                downloadAndSaveToCacheObservable(diskCache, resource, fileKey, contextDetails)
                        .doOnSubscribe(ignored -> Logging.d(LOG_TAG, "doNetworkRequest(", resource, ", ", fileKey, ")", " ", contextDetails))
                        .doOnSuccess(ignored -> ongoingDownloads.remove(fileKey))
                        .doOnError(throwable -> Logging.d(LOG_TAG, throwable, "doNetworkRequest doOnError ", fileKey, " ", contextDetails))
                        .doOnError(throwable -> ongoingDownloads.remove(fileKey))
                        .subscribe(
                                file -> {
                                    finalSubject.onNext(file);
                                    finalSubject.onComplete();
                                },
                                throwable -> finalSubject.onError(throwable));
            }
        });
    }

    private Single<File> downloadAndSaveToCacheObservable(DiskCache diskCache, String resource, String fileKey, ContextDetails contextDetails) {

        return mediaApi.load(resource)
                .map(response -> {
                    diskCache.put(fileKey, response.byteStream());
                    File file = diskCache.get(fileKey);
                    if (file == null)
                        throw new IOException("Can not get file from diskCache, fileKey = " + fileKey + ", " + contextDetails + ", " + resource);
                    Logging.d(LOG_TAG, "Success, return file for ", fileKey, " ", contextDetails);
                    return file;
                })
                .onErrorResumeNext(throwable -> processErrors(throwable, resource, contextDetails));
    }

    /**
     * If gfycat.com returned 403 response.
     */
    public static class ForbiddenGfycatException extends RuntimeException {
        public ForbiddenGfycatException(String message) {
            super(message);
        }
    }
}
