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

/**
 * Provides access to Gfycat media cache files.
 * <p>
 * !!! Warning: Cache files are for READ ONLY purpose. <u>Do not modify or delete them!</u>
 */
public interface MediaFilesManager {

    /**
     * Allocates Gfycat media file as byte array into memory.
     * <p>
     * Looks for a media file in cache. Downloads and saves it to one if not found.
     *
     * @param gfycat    of media file to get.
     * @param mediaType type of media file to get.
     * @return byte array of requested media file allocated in memory.
     */
    Single<byte[]> loadAsByteArray(Gfycat gfycat, MediaType mediaType);

    /**
     * Get Gfycat media file.
     * <p>
     * Looks for a media file in cache. Downloads and saves it to one if not found.
     * <p>
     * !!! Warning: You MUST NOT modify or delete this file. Treat is as READ ONLY file.
     *
     * @param gfycat    of media file to get.
     * @param mediaType type of media file to get.
     * @return media file in cache.
     */
    Single<File> loadAsFile(Gfycat gfycat, MediaType mediaType);

    /**
     * Same as {@link MediaFilesManager#loadAsByteArray(Gfycat, MediaType)}, but with {@link ContextDetails} for logging purposes.
     */
    Single<byte[]> loadAsByteArray(Gfycat gfycat, MediaType mediaType, ContextDetails contextDetails);

    /**
     * Same as {@link MediaFilesManager#loadAsFile(Gfycat, MediaType)}, but with {@link ContextDetails} for logging purposes.
     */
    Single<File> loadAsFile(Gfycat gfycat, MediaType mediaType, ContextDetails contextDetails);
}
