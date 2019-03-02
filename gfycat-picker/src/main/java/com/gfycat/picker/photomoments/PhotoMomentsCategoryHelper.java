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

package com.gfycat.picker.photomoments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by oleksandrbalandin on 8/21/17
 */

@Deprecated
public interface PhotoMomentsCategoryHelper {

    <T extends RecyclerView.ViewHolder> RecyclerView.Adapter<T> getPhotoMomentsCategoryAdapter(Context context,
                                                                                               View.OnClickListener onClickListener,
                                                                                               float aspectRatio);

    void release();
}
