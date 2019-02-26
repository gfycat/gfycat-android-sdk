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

package com.gfycat.core.creation;

/**
 * Callback from {@link UploadManager} with current creation progress.
 */
public interface UploadListener {

    /**
     * Stage of creation.
     */
    enum Stage {
        REQUESTING_KEY,
        UPLOADING,
        SERVER_PROCESSING
    }

    /**
     * @param stage    of creation
     * @param progress progress as int from [-1 to 100],
     *                 where values [0, 100] indicates progress and -1 indicate progress not supported for this stage.
     */
    void onUpdate(Stage stage, int progress);
}
