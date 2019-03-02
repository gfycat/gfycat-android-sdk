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

package com.gfycat.common;

import android.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Key value map, designed for logging purpose for handling API call context.
 * <p>
 * Best practice is to add to ContextDetails only values that you want to log.
 */
public class ContextDetails {

    private Map<String, String> map = new HashMap<>();
    private String toString;

    public ContextDetails(String key, String val) {
        this(Pair.create(key, val));
    }

    @SafeVarargs
    public ContextDetails(Pair<String, String>... args) {
        for (Pair<String, String> pair : args) {
            map.put(pair.first, pair.second);
        }
    }

    /**
     * Get value by key.
     */
    public String get(String key) {
        return map.get(key);
    }

    /**
     * Put key value pair to context map.
     *
     * @return Returns this.
     */
    public ContextDetails put(String key, String val) {
        map.put(key, val);
        toString = null;
        return this;
    }

    private ContextDetails(ContextDetails other) {
        map.putAll(other.map);
    }

    private String generateToString() {
        if (map.isEmpty()) return "";
        else {
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            StringBuilder sb = new StringBuilder("[");
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                sb.append(next.getKey()).append("=").append(next.getValue());
                if (iterator.hasNext()) sb.append(" ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        if (toString == null) {
            toString = generateToString();
        }
        return toString;
    }

    public ContextDetails copy() {
        return new ContextDetails(this);
    }
}
