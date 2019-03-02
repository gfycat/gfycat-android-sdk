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

package com.gfycat.core.gfycatapi;

import com.gfycat.core.gfycatapi.pojo.BlockedUsers;
import com.gfycat.core.gfycatapi.pojo.GfycatCategoriesList;
import com.gfycat.core.gfycatapi.pojo.GfycatList;
import com.gfycat.core.gfycatapi.pojo.NSFWUpdateRequest;
import com.gfycat.core.gfycatapi.pojo.OneGfyItem;
import com.gfycat.core.gfycatapi.pojo.PublishedUpdateRequest;
import com.gfycat.core.gfycatapi.pojo.ResetPasswordRequest;
import com.gfycat.core.gfycatapi.pojo.SearchResult;
import com.gfycat.core.gfycatapi.pojo.TransferGfycatsRequest;
import com.gfycat.core.gfycatapi.pojo.UpdateUserInfo;
import com.gfycat.core.gfycatapi.pojo.UserInfo;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by dekalo on 08.09.15.
 */
public interface GfycatAPI {

    String TRENDING = "gfycats/trending";
    String SEARCH = "gfycats/search";


    String SEARCH_TEXT = "search_text";
    String MIN_LENGTH = "minLength";
    String MAX_LENGTH = "maxLength";
    String MIN_ASPECT = "minAspectRatio";
    String MAX_ASPECT = "maxAspectRatio";
    String CONTENT_RATING = "rating";

    @GET(SEARCH)
    Observable<SearchResult> search(
            @Query(SEARCH_TEXT) String searchText,
            @Query("cursor") String digest,
            @Query("count") int count,
            @Query(MIN_LENGTH) String minLength,
            @Query(MAX_LENGTH) String maxLength,
            @Query(MIN_ASPECT) String minAspect,
            @Query(MAX_ASPECT) String maxAspect,
            @Query(CONTENT_RATING) String contentRating);

    @GET(TRENDING)
    Observable<GfycatList> getTrendingGfycats(@Query("digest") String digest, @Query("count") int count);

    @GET(TRENDING)
    Observable<GfycatList> getListForTag(@Query("tagName") String tag, @Query("digest") String digest, @Query("count") int count);

    @GET("reactions/populated")
    Observable<GfycatList> getReactions(@Query("tagName") String tag, @Query("digest") String digest, @Query("gfyCount") int count);

    @GET("sound/search")
    Observable<SearchResult> soundSearch(
            @Query(SEARCH_TEXT) String searchText,
            @Query("cursor") String digest,
            @Query("count") int count,
            @Query(MIN_LENGTH) String minLength,
            @Query(MAX_LENGTH) String maxLength,
            @Query(MIN_ASPECT) String minAspect,
            @Query(MAX_ASPECT) String maxAspect,
            @Query(CONTENT_RATING) String contentRating);

    @GET("sound")
    Observable<GfycatList> getTrendingSoundGfycats(
            @Query("digest") String digest,
            @Query("count") int count,
            @Query(MIN_LENGTH) String minLength,
            @Query(MAX_LENGTH) String maxLength,
            @Query(MIN_ASPECT) String minAspect,
            @Query(MAX_ASPECT) String maxAspect,
            @Query(CONTENT_RATING) String contentRating);

    @GET("gfycats/{gfyId}")
    Observable<OneGfyItem> getOneGfycatItemObservable(@Path("gfyId") String gfyName);

    @GET("me")
    Observable<UserInfo> getMyInfo();

    @GET("users/{username}/gfycats")
    Observable<GfycatList> getListForUser(@Path("username") String username, @Query("cursor") String cursor, @Query("count") int count);

    @HEAD("users/{username}")
    Single<Response<Void>> checkAvailability(@Path("username") String username);

    @PATCH("users")
    Completable resetPassword(@Body ResetPasswordRequest resetPasswordRequest);

    @GET("me/gfycats")
    Observable<GfycatList> getMyGfycats(@Query("cursor") String digestOrCursor, @Query("count") int count);

    @POST("me/send_verification_email")
    Completable verifyUserEmail(@Body String body);

    @POST("users/{username}/report-user")
    Observable<Response<ResponseBody>> blockUser(@Path("username") String username);

    @DELETE("me/blocked-users/{userid}")
    Observable<Response<ResponseBody>> unBlockUser(@Path("userid") String userid);

    @POST("gfycats/{gfyid}/report-content")
    Observable<Response<ResponseBody>> blockContent(@Path("gfyid") String gfyid);

    @POST("me/blocked-users")
    Observable<Response<ResponseBody>> blockUsers(@Body BlockedUsers blockedUsers);

    @PUT("me/gfycats/{gfyid}/nsfw")
    Observable<Response<ResponseBody>> updateNSFW(@Path("gfyid") String gfyid, @Body NSFWUpdateRequest nsfwUpdateRequest);

    @PUT("me/gfycats/{gfyid}/published")
    Observable<Response<ResponseBody>> updatePublishedState(@Path("gfyid") String gfyid, @Body PublishedUpdateRequest publishedUpdateRequest);

    @DELETE("me/gfycats/{gfyid}")
    Observable<Response<ResponseBody>> delete(@Path("gfyid") String gfyid);

    @GET("reactions/populated?gfyCount=1")
    Observable<GfycatCategoriesList> getCategories(@Query("locale") String locale);

    @PATCH("me")
    Completable updateUserProfile(@Body UpdateUserInfo body);

    @POST("me/profile_image_url")
    Single<Response<ResponseBody>> getUploadURL();

    @GET("me/profile_image_url/status/{ticket}")
    Observable<Response<ResponseBody>> getAvatarStatus(@Path("ticket") String ticket);

    @PATCH("users/{username}/gfycats")
    Completable transferGfycats(@Path("username") String username, @Body TransferGfycatsRequest transferGfycatsRequets);
}
