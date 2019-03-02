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

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by dekalo on 25.01.17.
 */

public class DiskCacheUtils {

    public static String randomAvailableKeyWithSuffix(DiskCache cache, String suffix) {
        int MAX_TRY_COUNT = 5;
        for (int i = 0; i < MAX_TRY_COUNT; i++) {
            String candidate = UUID.randomUUID() + suffix;
            if (cache.isAvailable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static String randomPathWithSuffixInCache(DiskCache cache, String suffix, byte[] bytes) {

        InputStream is = null;

        try {
            String fileKey = randomAvailableKeyWithSuffix(cache, suffix);
            is = new ByteArrayInputStream(bytes);
            cache.put(fileKey, is);
            return cache.get(fileKey).getPath();
        } catch (IOException | DefaultDiskCache.NotValidCacheException | DefaultDiskCache.OtherEditInProgressException e) {
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
