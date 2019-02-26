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

import com.gfycat.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;

/**
 * Video storage interface.
 * <p/>
 * Created by dekalo on 01.09.15.
 */
public interface DiskCache {

    /**
     * @return true is we may work with it, false otherwise
     */
    boolean isAvailable();

    /**
     * @return true if fileKey is available.
     */
    boolean isAvailable(String fileKey);
    /**
     * Synchronously get video File associated with fileKey.
     *
     * @param fileKey - id of video file.
     * @return File associated with fileKey or null, if there are no such.
     */
    File get(String fileKey);

    /**
     * Get file by fileKet via RxJava.
     */
    Observable<File> loadFile(String fileKey);

    /**
     * Open file asociated with key for editing.
     */
    DiskLruCache.Editor edit(String fileKey);

    /**
     * Save InputStream asociated with fileKey as file.
     *
     * @param fileKey of file.
     * @param is      to save.
     */
    void put(String fileKey, InputStream is) throws IOException, DefaultDiskCache.NotValidCacheException, DefaultDiskCache.OtherEditInProgressException;
}
