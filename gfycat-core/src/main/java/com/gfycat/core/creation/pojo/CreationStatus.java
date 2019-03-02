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
 * Created by dekalo on 14.12.15.
 */
public class CreationStatus {

    private String task;
    private String time;
    private String gfyname;
    private String description;

    public CreationStatus() {
    }

    public CreationStatus(String... args) {
        task = args[0];
        gfyname = args[1];
        time = args[2];
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getGfyname() {
        return gfyname;
    }

    public void setGfyname(String gfyname) {
        this.gfyname = gfyname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CreationStatus{" +
                "task='" + task + '\'' +
                ", time='" + time + '\'' +
                ", gfyname='" + gfyname + '\'' +
                '}';
    }
}
