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

package com.gfycat.player;

import android.content.Context;

import com.gfycat.common.utils.Logging;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Hold current version of {@link GfycatPlayerFactory}.
 * <p>
 * By default will look for com.gfycat.webp.GfycatWebpPlayerFactory in classpath.
 * As fallback will look for com.gfycat.gif.GfycatGifPlayerFactory in classpath.
 */
public class MainPlayerFactory implements GfycatPlayerFactory {

    private static final String LOG_TAG = "MainPlayerFactory";

    private static final MainPlayerFactory mainFactory = new MainPlayerFactory();
    private GfycatPlayerFactory delegate;

    /**
     * Setup desired {@link GfycatPlayerFactory} class.
     */
    public static void setup(GfycatPlayerFactory delegate) {
        mainFactory.delegate = delegate;
    }

    /**
     * Return current version of GfycatPlayerFactory.
     */
    public static GfycatPlayerFactory get() {
        return mainFactory;
    }

    /**
     * See {@link GfycatPlayerFactory#create(Context)}
     */
    @Override
    public GfycatPlayer create(Context context) {
        if (delegate == null) {
            delegate = tryCreateWebpFactory();
            if (delegate == null) delegate = tryCreateGifFactory();
            if (delegate == null)
                throw new IllegalStateException("Can not create GfycatPlayerView, cause GfycatPlayerFactory was not provided.");
        }
        return delegate.create(context);
    }

    private GfycatPlayerFactory tryCreateFactory(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor();
            return (GfycatPlayerFactory) constructor.newInstance();
        } catch (ClassCastException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Logging.w(LOG_TAG, "Can not instantiate factory:", className, ".");
            return null;
        }
    }

    private GfycatPlayerFactory tryCreateWebpFactory() {
        return tryCreateFactory("com.gfycat.webp.GfycatWebpPlayerFactory");
    }

    private GfycatPlayerFactory tryCreateGifFactory() {
        return tryCreateFactory("com.gfycat.gif.GfycatGifPlayerFactory");
    }
}
