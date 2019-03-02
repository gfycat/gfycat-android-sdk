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
import android.text.TextUtils;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.creation.pojo.CreateGfycatRequest;
import com.gfycat.core.creation.pojo.CreatedGfycat;
import com.gfycat.core.creation.pojo.CreationStatus;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Created by dekalo on 23.03.17.
 */

public class DefaultUploadManager implements UploadManager {

    private static final UploadListener DUMMY_LISTENER = (stage, progress) -> {
    };
    private static final int MAX_PROGRESS_VALUE = 100;

    private static final String STATUS_COMPLETE = "complete";
    private static final String STATUS_ERROR = "error";

    private final long DEFAULT_EXPECTED_CREATION_TIME_MS = TimeUnit.MINUTES.toMillis(3);
    private final long DEFAULT_INITIAL_TIMEOUT_TIME_MS = TimeUnit.SECONDS.toMillis(10);
    private final long DEFAULT_PING_TIMEOUT_TIME_MS = TimeUnit.SECONDS.toMillis(5);

    private final CreationAPI creationAPI;
    private final OkHttpClient uploadClient;
    private final GetGfycatByName getGfycatByName;
    private final String fileUploadEndpoint;
    private final long expectedCreationTimeMs;
    private final long initialTimeoutMs;
    private final long pingTimeoutMs;


    public DefaultUploadManager(@NonNull CreationAPI creationAPI, @NonNull OkHttpClient uploadClient, @NonNull String fileUploadEndpoint, @NonNull GetGfycatByName getGfycatByName) {
        this.creationAPI = creationAPI;
        this.uploadClient = uploadClient;
        this.fileUploadEndpoint = fileUploadEndpoint;
        this.getGfycatByName = getGfycatByName;
        expectedCreationTimeMs = DEFAULT_EXPECTED_CREATION_TIME_MS;
        initialTimeoutMs = DEFAULT_INITIAL_TIMEOUT_TIME_MS;
        pingTimeoutMs = DEFAULT_PING_TIMEOUT_TIME_MS;
    }

    public DefaultUploadManager(@NonNull CreationAPI creationAPI, @NonNull OkHttpClient uploadClient, @NonNull String fileUploadEndpoint, @NonNull GetGfycatByName getGfycatByName, long expected, long initial, long ping) {
        this.creationAPI = creationAPI;
        this.uploadClient = uploadClient;
        this.fileUploadEndpoint = fileUploadEndpoint;
        this.getGfycatByName = getGfycatByName;
        this.expectedCreationTimeMs = expected;
        this.initialTimeoutMs = initial;
        this.pingTimeoutMs = ping;
    }

    @Override
    public String requestCreationKey(CreateGfycatRequest creationParams) throws CanNotCreateKeyException {
        Response<CreatedGfycat> result;
        try {
            result = creationAPI.createGfycat(creationParams).execute();
        } catch (IOException e) {
            throw new CanNotCreateKeyException("IOException during createGfycat request", e);
        }

        if (!result.isSuccessful()) {
            throw new CanNotCreateKeyException("CreationAPI.createGfycat was not successful. code = " + result.code() + " message = " + result.message());
        }

        CreatedGfycat createdGfycat = result.body();

        if (TextUtils.isEmpty(createdGfycat.getGfyname())) {
            Assertions.fail(new IllegalStateException("Created gfyname is empty but request is successful."));
            throw new CanNotCreateKeyException("CreatedGfycat response is not ok = [" + createdGfycat + "]");
        }

        return createdGfycat.getGfyname();
    }

    @Override
    public void upload(String creationKey, InputStream inputStream) throws CanNotUploadGfycatException {
        upload(creationKey, inputStream, DUMMY_LISTENER);
    }

    @Override
    public void upload(String creationKey, InputStream inputStream, UploadListener listener) throws CanNotUploadGfycatException {
        try {
            listener.onUpdate(UploadListener.Stage.UPLOADING, 0);

            Request uploadRequest = new Request.Builder()
                    .url(fileUploadEndpoint + creationKey)
                    .put(new InputStreamRequestBody(
                            MediaType.parse("filename=" + creationKey),
                            inputStream, progress -> listener.onUpdate(UploadListener.Stage.UPLOADING,
                            progress)))
                    .build();

            okhttp3.Response uploadResponse = uploadClient.newCall(uploadRequest).execute();

            if (!uploadResponse.isSuccessful())
                throw new CanNotUploadGfycatException("File uploading was not successful. code = " + uploadResponse.code() + " message = " + uploadResponse.message());

            listener.onUpdate(UploadListener.Stage.UPLOADING, MAX_PROGRESS_VALUE);
        } catch (IOException e) {
            throw new CanNotUploadGfycatException("IOException during content uploading", e);
        }
    }

    @Override
    public Gfycat waitUntilReady(@NonNull String creationKey) throws CanNotGetGfycatStatusException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException {
        return waitUntilReady(creationKey, DEFAULT_SERVER_PROCESSING_TIMEOUT);
    }

    @Override
    public Gfycat waitUntilReady(@NonNull String creationKey, long timeout) throws CanNotGetGfycatStatusException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException {

        if (TextUtils.isEmpty(creationKey)) {
            throw new IllegalArgumentException("Creation key is empty.");
        }

        CreationStatus creationStatus = null;
        long startTime = System.currentTimeMillis();

        int tryCount = 0;

        do {

            try {
                long timeTillTimeout = Math.max(0, System.currentTimeMillis() + timeout - startTime);
                long defaultSleepTime = tryCount == 0 ? initialTimeoutMs : pingTimeoutMs;
                Thread.sleep(Math.min(timeTillTimeout, defaultSleepTime));
            } catch (InterruptedException e) {
                throw new CanNotGetGfycatStatusException(creationKey, "InterruptedException happened", e);
            }

            try {
                Response<CreationStatus> creationStatusResponse = creationAPI.getCreationStatus(creationKey).execute();

                if (!creationStatusResponse.isSuccessful()) {
                    throw new CanNotGetGfycatStatusException(creationKey, "creationStatusResponse is not successful (" + creationKey + ") code = " + creationStatusResponse.code() + " message = " + creationStatusResponse.message());
                } else {
                    CreationStatus localCreationStatus = creationStatusResponse.body();

                    if (STATUS_ERROR.equals(localCreationStatus.getTask())) {
                        throw new FailedToCreateGfycatException(creationKey, localCreationStatus.getDescription());
                    }

                    if (isCreationEnded(creationKey, localCreationStatus)) {
                        // exit on successfully received gfycat here
                        creationStatus = localCreationStatus;
                        break;
                    }
                }

            } catch (IOException e) {
                throw new CanNotGetGfycatStatusException(creationKey, "IOException while accessing CreationApi.getCreationStatus(" + creationKey + ")", e);
            }

            tryCount++;

        } while (startTime + expectedCreationTimeMs > System.currentTimeMillis());

        if (creationStatus == null) {
            throw new CreationTimeoutException(creationKey, "Status tracking ended by timeout");
        }

        try {
            return getGfycatByName.getGfycat(creationKey);
        } catch (IOException e) {
            throw new CanNotGetGfycatStatusException(creationKey, "Creation ends, can not get Gfycat object from server", e);
        } catch (Throwable throwable) {

            if (is404(throwable)) {
                // will happens if gfycat was deleted before getGfycat call.
                throw new GfycatWasDeletedBeforeCompletionException(creationKey, throwable);
            }

            Assertions.fail(new IllegalStateException("getGfycatByName.getGfycat() throws throwable creationKey = " + creationKey + " creationStatus = " + creationStatus, throwable));
            throw new CanNotGetGfycatStatusException(creationKey, "Creation ends, can not get Gfycat object from server", throwable);
        }
    }

    private boolean is404(Throwable throwable) {
        return (throwable instanceof HttpException && ((HttpException) throwable).code() == 404) || (throwable.getCause() != null && is404(throwable.getCause()));
    }

    @Override
    public Gfycat createGfycat(CreateGfycatRequest creationParams, InputStream inputStream) throws CanNotCreateKeyException, CanNotGetGfycatStatusException, CanNotUploadGfycatException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException {
        return createGfycat(creationParams, inputStream, DUMMY_LISTENER);
    }

    @Override
    public Gfycat createGfycat(CreateGfycatRequest creationParams, InputStream inputStream, UploadListener listener) throws CanNotCreateKeyException, CanNotGetGfycatStatusException, CanNotUploadGfycatException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException {
        listener.onUpdate(UploadListener.Stage.REQUESTING_KEY, -1);
        String gfyName = requestCreationKey(creationParams);
        upload(gfyName, inputStream, listener);
        listener.onUpdate(UploadListener.Stage.SERVER_PROCESSING, -1);
        return waitUntilReady(gfyName);
    }

    @Override
    public Gfycat getGfycatIfReady(String gfyName) {
        try {
            Gfycat gfycat = getGfycatByName.getGfycat(gfyName);
            if (gfycat != null) return gfycat;
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean isGfycatReady(String gfyName) {
        return getGfycatIfReady(gfyName) != null;
    }

    private boolean isCreationEnded(String gfyName, CreationStatus creationStatus) {
        if (creationStatus != null && STATUS_COMPLETE.equals(creationStatus.getTask())) {
            if (!Utils.equals(gfyName, creationStatus.getGfyname())) {
                Assertions.fail(new IllegalStateException("Expected(" + gfyName + ") and actual(" + creationStatus.getGfyname() + ") gfyNames differ."));
                throw new InternalCreationException();
            }
            return true;
        } else {
            return false;
        }
    }

    public interface GetGfycatByName {
        Gfycat getGfycat(String gfyName) throws IOException;
    }
}
