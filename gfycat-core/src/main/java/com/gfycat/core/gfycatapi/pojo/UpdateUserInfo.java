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

package com.gfycat.core.gfycatapi.pojo;

import android.text.TextUtils;

import com.gfycat.common.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * User info container used for user info update request. Use current field value if no change required on it
 */
public class UpdateUserInfo {
    private List<Operation> operations;

    /**
     * Update user info request sent to gfycat.com
     * <p>
     * See <a href="http://developers.gfycat.com/api/#updating-user-39-s-details">http://developers.gfycat.com/api/#updating-user-39-s-details</a>
     *
     * @param userInfo       base userInfo to compare with.
     * @param newName        provide newName or existing({@link UserInfo#getUsername()}) if you do not want to modify.
     * @param newUrl         provide newUrl or existing({@link UserInfo#getUrl()} if you do not want to modify.
     * @param newDescription provide newDescription or existing({@link UserInfo#getDescription()} if you do not want to modify.
     */
    public UpdateUserInfo(UserInfo userInfo,
                          String newName,
                          String newUrl,
                          String newDescription) {
        this.operations = new ArrayList<>();

        if (!Utils.equals(userInfo.getName(), newName))
            operations.add(new Operation("/name", newName));

        if (!Utils.equals(userInfo.getProfileUrl(), newUrl))
            operations.add(new Operation("/profile_url", newUrl));

        if (!Utils.equals(userInfo.getDescription(), newDescription))
            operations.add(new Operation("/description", newDescription));
    }

    private class Operation {
        private static final String ADD = "add";
        private static final String REMOVE = "remove";

        String op;
        String path;
        String value;

        Operation(String path, String value) {
            if (TextUtils.isEmpty(value))
                this.op = REMOVE;
            else
                this.op = ADD;

            this.path = path;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Operation{" +
                    "op='" + op + '\'' +
                    ", path='" + path + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "UpdateUserInfo{" +
                "operations=" + operations +
                '}';
    }
}
