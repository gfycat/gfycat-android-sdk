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
package com.gfycat.core;

import android.text.TextUtils;

import com.gfycat.common.utils.Utils;

import java.util.Objects;

public class SingleFeedIdentifier implements FeedIdentifier {

    private final String gfyId;

    public SingleFeedIdentifier(String gfyId) {
        this.gfyId = gfyId;
    }

    /* package */
    static FeedIdentifier create(String uniqueFeedIdentifier) {
        String[] keyAndName = uniqueFeedIdentifier.split(":");

        if (keyAndName.length != 2) {
            throw new IllegalStateException("Provided uniqueFeedIdentifier(" + uniqueFeedIdentifier + ") malformed.");
        }

        if (!Type.SINGLE.getName().equals(keyAndName[0])) {
            throw new IllegalArgumentException("Provided uniqueFeedIdentifier = " + uniqueFeedIdentifier + " is not single, it should start from " + Type.SINGLE.getName());
        }

        if (TextUtils.isEmpty(keyAndName[1])) {
            throw new IllegalStateException("gfyId is mull or empty in uniqueFeedIdentifier = " + uniqueFeedIdentifier);
        }

        return new SingleFeedIdentifier(keyAndName[1]);
    }

    @Override
    public FeedType getType() {
        return Type.SINGLE;
    }

    @Override
    public String toName() {
        return gfyId;
    }

    @Override
    public String toUniqueIdentifier() {
        return getType().getName() + ":" + gfyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleFeedIdentifier that = (SingleFeedIdentifier) o;
        return Utils.equals(gfyId, that.gfyId);
    }

    @Override
    public int hashCode() {
        return Utils.hash(gfyId);
    }
}
