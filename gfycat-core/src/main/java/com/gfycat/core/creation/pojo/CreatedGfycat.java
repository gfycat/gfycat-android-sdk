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

package com.gfycat.core.creation.pojo;


/**
 * Created by dekalo on 11.12.15.
 */
public class CreatedGfycat {

    private boolean isOk;
    private String gfyname;
    private String secret;

    public CreatedGfycat() {
    }

    public CreatedGfycat(String gfyname, String secret, boolean isOk) {
        this.gfyname = gfyname;
        this.secret = secret;
        this.isOk = isOk;
    }

    public String getGfyname() {
        return gfyname;
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public String toString() {
        return "CreatedGfycat{" +
                "isOk=" + isOk +
                ", gfyname='" + gfyname + '\'' +
                ", secret='" + secret + '\'' +
                '}';
    }
}
