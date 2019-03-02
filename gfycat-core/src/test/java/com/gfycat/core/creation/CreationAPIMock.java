/*
 * Copyright (c) 2015-present, Gfycat, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Gfycat.
 *
 * As with any software that integrates with the Gfycat platform, your use of
 * this software is subject to the Gfycat Terms of Service [https://gfycat.com/terms]
 * and Partner Terms of Service [https://gfycat.com/partners/terms]. This copyright
 * notice shall be included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.mock.BehaviorDelegate;

class CreationAPIMock implements CreationAPI {

    private BehaviorDelegate<CreationAPI> delegate;

    public void setDelegate(BehaviorDelegate<CreationAPI> delegate) {
        this.delegate = delegate;
    }

    protected BehaviorDelegate<CreationAPI> getDelegate() {
        return delegate;
    }

    @Override
    public Call<CreatedGfycat> createGfycat(@Body CreateGfycatRequest createGfycatRequest) {
        return delegate.returningResponse(new CreatedGfycat("test", "", true)).createGfycat(createGfycatRequest);
    }

    @Override
    public Call<CreationStatus> getCreationStatus(@Path("gfyName") String gfyName) {
        return delegate.returningResponse(new CreationStatus("complete", gfyName, "")).getCreationStatus(gfyName);
    }

    @Override
    public Observable<Response<ResponseBody>> updatePublishState(@Path("gfyId") String gfyId, @Body UpdateGfycat publishStatus) {
        return null;
    }

    @Override
    public Observable<Response<ResponseBody>> updateTitle(@Path("gfyId") String gfyId, @Body UpdateGfycat publishStatus) {
        return null;
    }

    @Override
    public Observable<Response<ResponseBody>> updateDescription(@Path("gfyId") String gfyId, @Body UpdateGfycat publishStatus) {
        return null;
    }

    @Override
    public Observable<Response<ResponseBody>> addTags(@Path("gfyId") String gfyId, @Body UpdateGfycat publishStatus) {
        return null;
    }

    @Override
    public Observable<VideoInfo> getVideoInfo(@Query("url") String url) {
        return null;
    }
}
