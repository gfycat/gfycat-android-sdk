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

import android.support.annotation.Nullable;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Utils;
import com.gfycat.disklrucache.DiskLruCache;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;

import io.reactivex.Observable;


/**
 * Implementation of DiskCache contract on top of DiskLruCache.
 * <p>
 * Created by dekalo on 01.09.15.
 */
public class DefaultDiskCache implements DiskCache {

    public static final long DEFAULT_MIN_SPACE_MB = 50;
    public static final long DEFAULT_MAX_SPACE_MB = 200;

    private static final float GFYCAT_CACHE_SPACE_FRACTION = 0.5f;
    private static final long MIN_GUARANTEED_FREE_USER_SPACE_MB = 50;

    private static final java.lang.String LOG_TAG = "DefaultDiskCache";

    private final static String CACHE_FOLDER_RELATIVE_PATH = "gfycat_media_cache";
    private static final int APP_VERSION = 1;

    private static volatile DefaultDiskCache INSTANCE;

    private final DiskLruCache diskLruCache;

    private static DiskLruCache construct(Queue<File> cacheFolderOptions, CacheSizeOptions cacheSizeOptions) {

        DiskLruCache result = null;

        while (!cacheFolderOptions.isEmpty()) {
            File option = cacheFolderOptions.poll();
            result = construct(option, cacheSizeOptions);

            if (result != null) {
                Logging.c(LOG_TAG, "DefaultDiskCache constructed at directory = ", option);
                break;
            }
        }

        if (result == null) {
            Logging.c(LOG_TAG, "Unable to construct DefaultDiskCache working without cache.");
        }

        return result;
    }

    private static DiskLruCache construct(File cacheFolder, CacheSizeOptions cacheSizeOptions) {
        if (cacheFolder == null) return null;
        try {
            Logging.d(LOG_TAG, "::DefaultDiskCache(...) try to construct cache on dir = ", cacheFolder);
            return DiskLruCache.open(new File(cacheFolder, CACHE_FOLDER_RELATIVE_PATH), APP_VERSION, calculateOptimalCacheSize(cacheFolder, cacheSizeOptions));
        } catch (IOException e) {
            Logging.c(LOG_TAG, "Unable to construct cache, IOException happened while constructing cache at = ", cacheFolder, ". ", e);
        } catch (NotEnoughSpace e) {
            Logging.c(LOG_TAG, "Unable to construct cache, not enough space usable = ", e.usableSpace, " gfycatSpace = ", e.gfycatSpace, " at = ", cacheFolder);
        } catch (CacheSizeNotSet e) {
            Logging.d(LOG_TAG, "Cache size parameters are set to 0");
        } catch (CacheSizeSetIncorrectly e) {
            Logging.d(LOG_TAG, "Unable to construct cache. Cache size parameters are set incorrectly");
        }
        return null;
    }

    private static long calculateOptimalCacheSize(File cacheFolder, CacheSizeOptions cacheSizeOptions) throws NotEnoughSpace, CacheSizeSetIncorrectly, CacheSizeNotSet {
        if (cacheSizeOptions.minSpaceMb > cacheSizeOptions.maxSpaceMb) {
            throw new CacheSizeSetIncorrectly();
        }

        if (cacheSizeOptions.maxSpaceMb == 0) {
            throw new CacheSizeNotSet();
        }

        long minSpace = cacheSizeOptions.minSpaceMb * Utils.MB;
        long maxSpace = cacheSizeOptions.maxSpaceMb * Utils.MB;

        long freeUserSpace = cacheFolder.getUsableSpace();
        long fractionatedUserSpace = (long) (freeUserSpace * GFYCAT_CACHE_SPACE_FRACTION);
        long allowedCacheSpace = freeUserSpace - (MIN_GUARANTEED_FREE_USER_SPACE_MB * Utils.MB);
        long proposedCacheSpace = Math.max(minSpace, fractionatedUserSpace);

        if (allowedCacheSpace < minSpace) {
            throw new NotEnoughSpace(freeUserSpace, proposedCacheSpace);
        }

        if (allowedCacheSpace < proposedCacheSpace) {
            proposedCacheSpace = allowedCacheSpace;
        }

        if (proposedCacheSpace > maxSpace) {
            proposedCacheSpace = maxSpace;
        }
        return proposedCacheSpace;
    }

    private DefaultDiskCache(Queue<File> cacheFolderOptions, CacheSizeOptions cacheSizeOptions) {
        diskLruCache = construct(cacheFolderOptions, cacheSizeOptions);
    }

    public static synchronized DefaultDiskCache initialize(Queue<File> cacheFolderOptions, CacheSizeOptions cacheSizeOptions) {
        if (INSTANCE == null) {
            INSTANCE = new DefaultDiskCache(cacheFolderOptions, cacheSizeOptions);
        }
        return INSTANCE;
    }

    public static DefaultDiskCache get() {
        return INSTANCE;
    }

    private static void guard() {
        Assertions.assertNotUIThread(IllegalAccessException::new);
    }

    private boolean isValid() {
        return diskLruCache != null;
    }

    @Override
    public boolean isAvailable() {
        return isValid();
    }

    public boolean isAvailable(String fileKey) {
        guard();
        if (!isValid()) return false;
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(fileKey);
            if (snapshot == null) return true;
            return false;
        } catch (IOException e) {
            Logging.e(LOG_TAG, "::DefaultDiskCache::get(" + fileKey + ") IOException happens while getting.", e);
            return false;
        }
    }

    public File get(String fileKey) {
        guard();
        if (!isValid()) return null;
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(fileKey);
            if (snapshot == null) return null;
            return snapshot.getFile();
        } catch (IOException e) {
            Logging.e(LOG_TAG, "::DefaultDiskCache::get(" + fileKey + ") IOException happens while getting.", e);
            return null;
        }
    }

    @Override
    public Observable<File> loadFile(String fileKey) {
        return Observable.create(subscriber -> {
            subscriber.onNext(get(fileKey));
            subscriber.onComplete();
        });
    }

    private void abortQuietly(@Nullable DiskLruCache.Editor editor) {
        if (editor == null) return;
        try {
            editor.abort();
        } catch (IOException e) {
            Logging.e(LOG_TAG, "::abortQuietly() fails", e);
        }
    }

    @Override
    public DiskLruCache.Editor edit(String fileKey) {
        guard();
        if (isValid()) {
            try {
                return diskLruCache.edit(fileKey);
            } catch (IOException e) {
                // eat error
            }
        }
        return null;
    }

    @Override
    public void put(String fileKey, InputStream is) throws IOException, NotValidCacheException, OtherEditInProgressException {
        Logging.d(LOG_TAG, "put(", fileKey, ")");
        guard();
        if (!isValid()) {
            String message = "::put(" + fileKey + ") failed due to storage is invalid.";
            Logging.d(LOG_TAG, message);
            throw new NotValidCacheException(message);
        }
        DiskLruCache.Editor val = null;
        try {
            val = diskLruCache.edit(fileKey);
            if (val == null) {
                String message = "::put(" + fileKey + ") other edit is in progress, skip.";
                Logging.d(LOG_TAG, message);
                throw new OtherEditInProgressException(message);
            }
        } catch (IOException e) {
            abortQuietly(val);
            throw e;
        }
        OutputStream os;
        try {
            os = val.newOutputStream();
        } catch (IOException e) {
            Logging.e(LOG_TAG, "::put(" + fileKey + ") failed to get OutputStream ", e);
            abortQuietly(val);
            throw e;
        }
        try {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            Logging.e(LOG_TAG, "::put(" + fileKey + ") failed to copy content ", e);
            abortQuietly(val);
            throw e;
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }

        try {
            val.commit();
            Logging.d(LOG_TAG, "put(", fileKey, ") SUCCESS");
        } catch (IOException e) {
            Logging.e(LOG_TAG, "::put(" + fileKey + ") FAILED to commit ", e);
            abortQuietly(val);
            throw e;
        }
    }

    public static class CacheSizeOptions {
        private long minSpaceMb;
        private long maxSpaceMb;

        /**
         * Create {@link CacheSizeOptions} instance with default space values
         */
        public CacheSizeOptions() {
            minSpaceMb = DEFAULT_MIN_SPACE_MB;
            maxSpaceMb = DEFAULT_MAX_SPACE_MB;
        }

        public void setMinSpace(long megabytes) {
            if (megabytes < 0) {
                minSpaceMb = 0;
            } else {
                minSpaceMb = megabytes;
            }
        }

        public void setMaxSpace(long megabytes) {
            if (megabytes < 0) {
                maxSpaceMb = 0;
            } else {
                maxSpaceMb = megabytes;
            }
        }
    }

    /**
     * Thrown if other disk cache edit with same key is in progress
     */
    public static class OtherEditInProgressException extends Exception {
        OtherEditInProgressException(String message) {
            super(message);
        }
    }

    /**
     * Thrown on operations with not valid cache.
     */
    public static class NotValidCacheException extends Exception {
        NotValidCacheException(String message) {
            super(message);
        }
    }

    private static class CacheSizeNotSet extends Exception {
    }

    private static class CacheSizeSetIncorrectly extends Exception {
    }

    private static class NotEnoughSpace extends Exception {
        final long gfycatSpace;
        final long usableSpace;

        public NotEnoughSpace(long usableSpace, long gfycatSpace) {
            this.usableSpace = usableSpace;
            this.gfycatSpace = gfycatSpace;
        }
    }
}
