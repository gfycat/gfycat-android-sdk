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

import com.gfycat.core.creation.pojo.CreateGfycatRequest;
import com.gfycat.core.creation.pojo.CreatedGfycat;
import com.gfycat.core.creation.pojo.CreationStatus;
import com.gfycat.core.creation.pojo.UpdateGfycat;
import com.gfycat.core.creation.pojo.VideoInfo;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Gfycat creation API wrapper.
 * See <a href="http://developers.gfycat.com/api/#creating-gfycats">http://developers.gfycat.com/api/#creating-gfycats</a>
 */
public interface CreationAPI {

    @POST("gfycats")
    Call<CreatedGfycat> createGfycat(@Body CreateGfycatRequest createGfycatRequest);

    @GET("gfycats/fetch/status/{gfyName}")
    Call<CreationStatus> getCreationStatus(@Path("gfyName") String gfyName);

    @PUT("me/gfycats/{gfyId}/published")
    Observable<Response<ResponseBody>> updatePublishState(@Path("gfyId") String gfyId, @Body UpdateGfycat publishStatus);

    @PUT("me/gfycats/{gfyId}/title")
    Observable<Response<ResponseBody>> updateTitle(@Path("gfyId") String gfyId, @Body UpdateGfycat publishStatus);

    @PUT("me/gfycats/{gfyId}/description")
    Observable<Response<ResponseBody>> updateDescription(@Path("gfyId") String gfyId, @Body UpdateGfycat publishStatus);

    @PUT("me/gfycats/{gfyId}/tags")
    Observable<Response<ResponseBody>> addTags(@Path("gfyId") String gfyId, @Body UpdateGfycat publishStatus);

    @GET("gfycats/fetch/remoteurlinfo")
    Observable<VideoInfo> getVideoInfo(@Query("url") String url);
}
