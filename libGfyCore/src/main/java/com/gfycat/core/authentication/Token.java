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

package com.gfycat.core.authentication;

import com.gfycat.core.gfycatapi.pojo.ErrorMessage;

import java.io.Serializable;

/**
 * Token container.
 * <p/>
 * Created by dekalo on 21.01.16.
 */
public interface Token extends Serializable {

    long serialVersionUID = 2394850107646685071L; // fair random

    /**
     * @return refreshToken if contains and null if not.
     */
    String getRefreshToken();

    /**
     * @return accessToken.
     */
    String getAccessToken();

    /**
     * @return error if such happens.
     */
    ErrorMessage getError();

    /**
     * @return username if there are such.
     */
    String getUserid();

    /**
     * Token instance that indicates NO_TOKEN.
     */
    Token NO_TOKEN = new Token() {
        @Override
        public String getRefreshToken() {
            return null;
        }

        @Override
        public String getAccessToken() {
            return null;
        }

        @Override
        public ErrorMessage getError() {
            return new ErrorMessage("NoToken", "NoToken");
        }

        @Override
        public String getUserid() {
            return null;
        }

        @Override
        public String toString() {
            return "NO_TOKEN";
        }
    };
}
