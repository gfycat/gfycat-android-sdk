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

import com.gfycat.core.creation.pojo.CreateGfycatRequest;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.io.InputStream;

/**
 * Manages Gfycat uploading to Gfycat.com
 * <p>
 * Upload flow consists of 3 separate steps: request creation key, upload content and server processing.
 * See details: http://developers.gfycat.com/api/#upload-gifs
 */
public interface UploadManager {

    /**
     * Estimated time for a server to process a {@link Gfycat}.
     */
    long DEFAULT_SERVER_PROCESSING_TIMEOUT = 3 * 60 * 1000;

    /**
     * Creation key request. This is the first creation flow step.
     * <p>
     * Call {@link #upload(String, InputStream)} method next, by providing returned key.
     *
     * @param creationParams creation parameters.
     * @return Returns a unique creation key. Needed for upload and processing status check.
     * @throws CanNotCreateKeyException in case of request failure.
     */
    String requestCreationKey(CreateGfycatRequest creationParams) throws CanNotCreateKeyException;

    /**
     * Upload video file with creation key provided by {@link #requestCreationKey(CreateGfycatRequest)}. This is a second creation flow step.
     *
     * @param creationKey unique key for this upload.
     * @param inputStream gfycat content from stream.
     * @throws CanNotUploadGfycatException thrown error happens during uploading.
     */
    void upload(String creationKey, InputStream inputStream) throws CanNotUploadGfycatException;

    /**
     * Same as {@link #upload(String, InputStream)} but with progress reporting.
     */
    void upload(String creationKey, InputStream inputStream, UploadListener uploadListener) throws CanNotUploadGfycatException;

    /**
     * Synchronously wait for server processing result. This is the third creation flow step.
     * <p>
     * {@link Gfycat} will be returned once it becomes available on server.
     *
     * @param creationKey unique key for this upload provided by {@link #requestCreationKey(CreateGfycatRequest)}
     * @param timeout     wait timeout.
     * @return Returns {@link Gfycat} available on server.
     * @throws CanNotGetGfycatStatusException if {@link Gfycat} was not returned by server within a specified timeout.
     */
    Gfycat waitUntilReady(String creationKey, long timeout) throws CanNotGetGfycatStatusException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException;

    /**
     * Same as {@link #waitUntilReady(String)} but with {@link #DEFAULT_SERVER_PROCESSING_TIMEOUT}
     */
    Gfycat waitUntilReady(String creationKey) throws CanNotGetGfycatStatusException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException;

    /**
     * @param gfyName of uploaded gfycat
     * @return Returns {@link Gfycat} if it exists, null otherwise.
     */
    Gfycat getGfycatIfReady(String gfyName);

    /**
     * @param gfyName of uploaded gfycat.
     * @return Returns {@code true} if Gfycat is available, false otherwise.  Tip: use {@link com.gfycat.core.downloading.FeedManager#getGfycat(String)} to get it if {@code true} returned.
     */
    boolean isGfycatReady(String gfyName);

    /**
     * Encapsulates all three steps of creation flow.
     * Calls the following methods in the corresponding order: {@link #requestCreationKey(CreateGfycatRequest)}, {@link #upload(String, InputStream)}, {@link #waitUntilReady(String)}
     *
     * @param creationParams creation parameters.
     * @param inputStream    gfycat content from stream.
     * @return Returns {@link Gfycat} available on server.
     * @throws CanNotGetGfycatStatusException if {@link Gfycat} was not returned by server within a default {@link #DEFAULT_SERVER_PROCESSING_TIMEOUT} timeout.
     */
    Gfycat createGfycat(CreateGfycatRequest creationParams, InputStream inputStream) throws CanNotCreateKeyException, CanNotGetGfycatStatusException, CanNotUploadGfycatException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException;

    /**
     * Same as {@link #createGfycat(CreateGfycatRequest, InputStream)} but with progress reporting.
     */
    Gfycat createGfycat(CreateGfycatRequest creationParams, InputStream inputStream, UploadListener uploadListener) throws CanNotCreateKeyException, CanNotGetGfycatStatusException, CanNotUploadGfycatException, FailedToCreateGfycatException, GfycatWasDeletedBeforeCompletionException;

    /**
     * Happens when creation flow ended by timeout.
     * <p>
     * Gfycat should be still available later on server.
     */
    class CreationTimeoutException extends CanNotGetGfycatStatusException {
        CreationTimeoutException(String gfyName, String message) {
            super(gfyName, message);
        }
    }

    /**
     * Indicates that creation flow terminated due to internal logic error.
     */
    class InternalCreationException extends RuntimeException {
    }

    /**
     * Indicates that request creation key and upload content steps have passed, but
     * Gfycat.com was unable to create Gfycat from uploaded content.
     */
    class FailedToCreateGfycatException extends Exception {
        private final String gfyName;

        FailedToCreateGfycatException(String gfyName, String description) {
            super(description);
            this.gfyName = gfyName;
        }

        /**
         * @return Returns gfyName of a Gfycat that is being processed on server.
         */
        public String getGfyName() {
            return gfyName;
        }
    }

    /**
     * Indicates that Gfycat was deleted on server, while creation flow was in process.
     * <p>
     * Due to our creation completion tracking implementation (polling server for completion status),
     * it is possible to delete completed gfycat before it was tracked by client as completed,
     * in this case exception {@link GfycatWasDeletedBeforeCompletionException} raises.
     */
    class GfycatWasDeletedBeforeCompletionException extends Exception {
        private final String gfyName;

        public GfycatWasDeletedBeforeCompletionException(String gfyName, Throwable throwable) {
            super(throwable);
            this.gfyName = gfyName;
        }

        public String getGfyName() {
            return gfyName;
        }
    }

    /**
     * Indicates that request creation key and upload content steps have passed, but
     * {@link UploadManager} was unable to retrieve creation status from Gfycat.com
     * <p>
     * Use {@link #isGfycatReady(String)} method to check Gfycat status later.
     */
    class CanNotGetGfycatStatusException extends Exception {
        private final String gfyName;

        CanNotGetGfycatStatusException(String gfyName, String message, Throwable cause) {
            super(message, cause);
            this.gfyName = gfyName;
        }

        CanNotGetGfycatStatusException(String gfyName, String message) {
            super(message);
            this.gfyName = gfyName;
        }

        /**
         * @return Returns gfyName of a Gfycat that is being processed on server.
         */
        public String getGfyName() {
            return gfyName;
        }
    }

    /**
     * Indicates that upload content step failed.
     */
    class CanNotUploadGfycatException extends Exception {

        public CanNotUploadGfycatException(String message, Exception cause) {
            super(message, cause);
        }

        public CanNotUploadGfycatException(String message) {
            super(message);
        }
    }

    /**
     * Indicates that request creation key step failed.
     */
    class CanNotCreateKeyException extends Exception {
        public CanNotCreateKeyException(String message, Throwable cause) {
            super(message, cause);
        }

        public CanNotCreateKeyException(String message) {
            super(message);
        }
    }
}
