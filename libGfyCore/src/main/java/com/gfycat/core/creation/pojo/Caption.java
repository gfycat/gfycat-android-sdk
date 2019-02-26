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


import java.io.Serializable;

/**
 * Text to be drawn over Gfycat. Server will add this text to Gfycat animation.
 */
public class Caption implements Serializable {

    public final String text;
    public final int x;
    public final int y;
    public final int startSeconds;
    public final int duration;
    public final int fontHeight;

    public Caption() {
        text = null;
        x = 0;
        y = 0;
        startSeconds = 0;
        duration = 0;
        fontHeight = 0;
    }

    /**
     * @param text         to be displayed.
     * @param x            position of the text's top left corner.
     * @param y            position of the text's top left corner.
     * @param startSeconds when the caption should be displayed.
     * @param duration     display duration.
     * @param fontHeight   caption font size.
     */
    public Caption(String text, int x, int y, int startSeconds, int duration, int fontHeight) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.startSeconds = startSeconds;
        this.duration = duration;
        this.fontHeight = fontHeight;
    }

    @Override
    public String toString() {
        return "Caption{x=" + x + ", y=" + y + ", duration=" + duration + ", textSize=" + fontHeight + "}";
    }
}