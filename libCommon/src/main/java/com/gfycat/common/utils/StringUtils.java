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

package com.gfycat.common.utils;

/**
 * Created by Andrew Khloponin
 */
public class StringUtils {
    private static final int INDEX_NOT_FOUND = -1;
    private static final String EMPTY = "";

    /**
     * Concat strings array to one string.
     */
    public static String concatAsString(String... message) {
        return concatAsString((Object[]) message);
    }

    /**
     * Concat single string from item.toBase64String().
     */
    public static String concatAsString(Object... message) {
        StringBuilder sb = new StringBuilder();
        for (Object item : message) {
            sb.append(item);
        }
        return sb.toString();
    }

    public static String difference(String str1, String str2) {
        if (str1 == null) {
            return str2;
        }
        if (str2 == null) {
            return str1;
        }
        int at = indexOfDifference(str1, str2);
        if (at == INDEX_NOT_FOUND) {
            return EMPTY;
        }
        return str2.substring(at);
    }

    public static int indexOfDifference(String str1, String str2) {
        if (str1 == str2) {
            return INDEX_NOT_FOUND;
        }
        if (str1 == null || str2 == null) {
            return 0;
        }
        int i;
        for (i = 0; i < str1.length() && i < str2.length(); ++i) {
            if (str1.charAt(i) != str2.charAt(i)) {
                break;
            }
        }
        if (i < str2.length() || i < str1.length()) {
            return i;
        }
        return INDEX_NOT_FOUND;
    }
}