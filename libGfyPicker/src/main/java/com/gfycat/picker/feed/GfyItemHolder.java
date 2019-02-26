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

package com.gfycat.picker.feed;

import com.gfycat.common.Recyclable;
import com.gfycat.common.recycler.AutoPlayable;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

/**
 * Interface/Contract for Gfy item holders.
 *
 * Created by dekalo on 02.11.15.
 */
public interface GfyItemHolder extends Recyclable, AutoPlayable {

    /**
     * @return current Gfycat item associated with this instance.
     */
    Gfycat getItem();
}
