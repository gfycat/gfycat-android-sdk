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

import android.support.annotation.NonNull;

/**
 * Library loader class to provide an ability to override native {@link System#loadLibrary(String)}
 * method used in Gfycat SDK be default.
 * <p>
 * This may help reduce the amount of {@link UnsatisfiedLinkError} that might occur due to a not reliable native implementation of libraries loading
 * <p>
 * As an example of other library loaders, you can use <a href="https://github.com/KeepSafe/ReLinker">ReLinker</a> together with {@link SoLibraryLoader}
 */
public class SoLibraryLoader {
    public interface LibraryLoadHandler {
        void loadLibrary(String libname);
    }

    /**
     * A default {@link LibraryLoadHandler} implementation using native {@link System#loadLibrary(String)} call
     */
    public static final class NativeLibraryLoadHandler implements LibraryLoadHandler {
        @Override
        public void loadLibrary(String libname) {
            System.loadLibrary(libname);
        }
    }

    private static LibraryLoadHandler sLibraryLoadHandler = new NativeLibraryLoadHandler();

    /**
     *  Loads the dynamic library with the specified library name.
     *  More info at {@link Runtime#loadLibrary(String)}
     *
     * @param libname name of the library to load
     */
    public static void loadLibrary(String libname) {
        sLibraryLoadHandler.loadLibrary(libname);
    }

    /**
     * Set a custom {@link LibraryLoadHandler} implementation to load the libraries in your own way
     *
     * @param libraryLoadHandler
     */
    public static void setLibraryLoadHandler(@NonNull LibraryLoadHandler libraryLoadHandler) {
        sLibraryLoadHandler = libraryLoadHandler;
    }
}
