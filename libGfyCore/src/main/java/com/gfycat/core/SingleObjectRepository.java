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

import android.content.Context;
import android.content.SharedPreferences;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Persistent storage in shared preferences for single object that supports jackson serialization.
 * <p>
 * Created by dekalo on 01.02.16.
 */
public class SingleObjectRepository<T> {

    private static final Gson gson = new Gson();
    private final SharedPreferences preferences;
    private final String key;
    private final Class<? extends T> clazz;
    private final T defaultValue;

    /**
     * All preferences and lazyValue modification should be synchronized.
     */
    private static Map<String, AtomicReference<Object>> lazyValues = new HashMap<>();

    private final String logTag;

    public SingleObjectRepository(Context context, String key, Class<? extends T> clazz, @NonNull T defaultValue) {
        this.preferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        this.key = key;
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        logTag = "SOR_" + key;
    }

    public void putSync(@NonNull T value) {
        basePut(value, SharedPreferences.Editor::commit);
    }

    public void put(@NonNull T value) {
        basePut(value, SharedPreferences.Editor::apply);
    }

    private void basePut(@NonNull T value, Consumer<SharedPreferences.Editor> applyOrCommit) {
        Logging.d(logTag, "put(", value, ")");
        try {
            synchronized (clazz) {
                lazySet(value);
                applyOrCommit.accept(preferences.edit().putString(key, gson.toJson(value)));
            }
        } catch (JsonIOException e) {
            Assertions.fail(new Exception("Can not save value: " + value + " for class: " + clazz, e));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void remove() {
        synchronized (clazz) {
            lazyValues.remove(key);
            preferences.edit().remove(key).apply();
        }
    }

    public void removeSync() {
        baseRemove(SharedPreferences.Editor::commit);
    }

    private void baseRemove(Consumer<SharedPreferences.Editor> applyOrCommit) {
        synchronized (clazz) {
            lazyValues.remove(key);
            try {
                applyOrCommit.accept(preferences.edit().remove(key));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    public T get() {

        T result = lazyGet();

        if (result == null) {
            try {
                synchronized (clazz) {
                    if (preferences.contains(key)) {
                        result = gson.fromJson(preferences.getString(key, null), clazz);
                        lazySet(result);
                    }
                }
            } catch (JsonIOException e) {
                Assertions.fail(new Exception("Can not read value " + preferences.getString(key, null) + " for class: " + clazz, e));
                remove();
            }
        }

        return result == null ? defaultValue : result;
    }

    @NonNull
    private T lazyGet() {
        synchronized (clazz) {
            AtomicReference<T> ref = (AtomicReference<T>) lazyValues.get(key);
            return ref != null ? ref.get() : null;
        }
    }

    private void lazySet(@NonNull T value) {
        synchronized (clazz) {
            lazyValues.put(key, new AtomicReference<>(value));
        }
    }

    public Observable<T> observe() {
        return Observable.create(subscriber -> {

            subscriber.onNext(get());

            SharedPreferences.OnSharedPreferenceChangeListener internalListener = (sharedPreferences, key1) -> subscriber.onNext(get());

            preferences.registerOnSharedPreferenceChangeListener(internalListener);

            subscriber.setCancellable(() -> preferences.unregisterOnSharedPreferenceChangeListener(internalListener));
        });
    }
}
