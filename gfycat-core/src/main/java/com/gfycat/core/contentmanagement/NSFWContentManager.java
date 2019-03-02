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

package com.gfycat.core.contentmanagement;

import com.gfycat.core.gfycatapi.pojo.Gfycat;

/**
 * Restricted content report manager
 */
public interface NSFWContentManager {

    /**
     * Report gfycat as not safe for work.
     * Gfycat will be reported to <a href="http://gfycat.com/">gfycat.com</a> and hidden locally.
     *
     * @param gfycat considered as not safe for work.
     * @return undo runnable that should be called if this gfycat was reported by mistake.
     */
    Runnable reportItem(Gfycat gfycat);

    /**
     * Report owner of gfycat.
     * <p>
     * Application user will not see any content from Gfycat owner anymore.
     *
     * @param gfycat                owner of this Gfycat to be blocked.
     * @param undoPossibilityTimeMs undo time in milliseconds.
     * @return undo runnable if owner was reported by mistake.
     */
    Runnable reportUser(Gfycat gfycat, long undoPossibilityTimeMs);
}
