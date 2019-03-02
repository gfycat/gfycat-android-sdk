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
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.MockRetrofit;
import retrofit2.mock.NetworkBehavior;

/**
 * Created by dekalo on 23.03.17.
 */

@RunWith(RobolectricTestRunner.class)
public class DefaultUploadManagerUnitTest extends TestCase {

    private UploadManager uploadManager;

    public void setupCreationManagerWith(CreationAPIMock creationAPIMock, MockInterceptor uploadInterceptor) {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gfycat.com") // any
                .build();

        NetworkBehavior networkBehavior = NetworkBehavior.create();
        networkBehavior.setFailurePercent(0);

        MockRetrofit mockRetrofit = new MockRetrofit.Builder(retrofit)
                .networkBehavior(networkBehavior).build();

        final BehaviorDelegate<CreationAPI> delegate = mockRetrofit.create(CreationAPI.class);

        creationAPIMock.setDelegate(delegate);

        uploadManager = new DefaultUploadManager(creationAPIMock, new OkHttpClient.Builder().addInterceptor(uploadInterceptor).build(), "https://localhost/", new GetGfycatMock(), 0, 0, 0);
    }

    protected void tearDown() {
        uploadManager = null;
    }

    private MockInterceptor defaultUploadInterceptor() {
        return new MockInterceptor().enqueueResponse(
                new okhttp3.Response.Builder()
                        .code(200)
                        .message("mock 200")
                        .body(ResponseBody.create(MediaType.parse("text/json"), "")));
    }

    @Test
    public void testSuccessfulGfycatCreation() {

        setupCreationManagerWith(new CreationAPIMock(), defaultUploadInterceptor());

        try {
            Gfycat gfycat = uploadManager.createGfycat(new CreateGfycatRequest.Builder().build(), getEmptyByteArray());
            assertNotNull(gfycat);
        } catch (UploadManager.CanNotCreateKeyException | UploadManager.CanNotGetGfycatStatusException | UploadManager.CanNotUploadGfycatException | UploadManager.FailedToCreateGfycatException | UploadManager.GfycatWasDeletedBeforeCompletionException e) {
            failWithException(e);
        }
    }

    @Test
    public void testSimulateIOExceptionWhileUploading() {

        setupCreationManagerWith(new CreationAPIMock(), new MockInterceptor().enqueueIOException(new IOException()));

        try {
            Gfycat gfycat = uploadManager.createGfycat(new CreateGfycatRequest.Builder().build(), getEmptyByteArray());
            assertNotNull(gfycat);
            fail("CanNotUploadGfycatException expected");
        } catch (UploadManager.CanNotCreateKeyException | UploadManager.CanNotGetGfycatStatusException | UploadManager.FailedToCreateGfycatException | UploadManager.GfycatWasDeletedBeforeCompletionException e) {
            failWithException(e);
        } catch (UploadManager.CanNotUploadGfycatException e) {
            // expected
        }
    }

    @Test
    public void testSimulateIOExceptionWhileCreationGfycat() {

        setupCreationManagerWith(new CreationAPIMock() {
            @Override
            public Call<CreatedGfycat> createGfycat(@Body CreateGfycatRequest createGfycatRequest) {
                return getDelegate().returning(new FailedTestCall("testSimulateIOExceptionWhileCreationGfycat()")).createGfycat(createGfycatRequest);
            }
        }, defaultUploadInterceptor());

        try {
            uploadManager.createGfycat(new CreateGfycatRequest.Builder().build(), getEmptyByteArray());
            fail("CanNotCreateKeyException expected");
        } catch (UploadManager.CanNotCreateKeyException e) {
            // expected
        } catch (UploadManager.CanNotGetGfycatStatusException | UploadManager.CanNotUploadGfycatException | UploadManager.FailedToCreateGfycatException | UploadManager.GfycatWasDeletedBeforeCompletionException e) {
            failWithException(e);
        }
    }

    @Test
    public void testSimulateTimeoutWhileStatusTracking() {

        setupCreationManagerWith(new CreationAPIMock() {
            @Override
            public Call<CreationStatus> getCreationStatus(@Path("gfyName") String gfyName) {
                return getDelegate().returningResponse(new CreationStatus("encoding", "", "")).getCreationStatus(gfyName);
            }
        }, defaultUploadInterceptor());

        try {
            uploadManager.createGfycat(new CreateGfycatRequest.Builder().build(), getEmptyByteArray());
            fail("CanNotGetGfycatStatusException expected");
        } catch (UploadManager.CreationTimeoutException e) {
            // expected
        } catch (UploadManager.CanNotCreateKeyException | UploadManager.CanNotGetGfycatStatusException | UploadManager.GfycatWasDeletedBeforeCompletionException | UploadManager.CanNotUploadGfycatException | UploadManager.FailedToCreateGfycatException e) {
            failWithException(e);
        }
    }

    @Test
    public void testSimulateIOExceptionWhileStatusTracking() {

        setupCreationManagerWith(new CreationAPIMock() {
            @Override
            public Call<CreationStatus> getCreationStatus(@Path("gfyName") String gfyName) {
                return getDelegate().returning(new FailedTestCall("testSimulateIOExceptionWhileStatusTracking()")).getCreationStatus(gfyName);
            }
        }, defaultUploadInterceptor());

        try {
            uploadManager.createGfycat(new CreateGfycatRequest.Builder().build(), getEmptyByteArray());
            fail("CanNotGetGfycatStatusException expected");
        } catch (UploadManager.CreationTimeoutException | UploadManager.GfycatWasDeletedBeforeCompletionException | UploadManager.CanNotCreateKeyException | UploadManager.CanNotUploadGfycatException | UploadManager.FailedToCreateGfycatException e) {
            failWithException(e);
        } catch (UploadManager.CanNotGetGfycatStatusException e) {
            // expected
        }
    }

    @Test
    public void testInternalCreationError() {

        setupCreationManagerWith(new CreationAPIMock() {
            @Override
            public Call<CreationStatus> getCreationStatus(@Path("gfyName") String gfyName) {
                return getDelegate().returningResponse(new CreationStatus("complete", gfyName + "wrong", "")).getCreationStatus(gfyName);
            }
        }, defaultUploadInterceptor());

        try {
            uploadManager.createGfycat(new CreateGfycatRequest.Builder().build(), getEmptyByteArray());
            fail("CanNotGetGfycatStatusException expected");
        } catch (UploadManager.InternalCreationException e) {
            // expected
        } catch (UploadManager.CanNotCreateKeyException | UploadManager.GfycatWasDeletedBeforeCompletionException | UploadManager.CanNotGetGfycatStatusException | UploadManager.CanNotUploadGfycatException | UploadManager.FailedToCreateGfycatException e) {
            failWithException(e);
        }
    }

    private void failWithException(Throwable e) {
        e.printStackTrace();
        fail(e.getClass().getSimpleName() + " " + e.getMessage());
    }

    public static InputStream getEmptyByteArray() {
        return new ByteArrayInputStream(new byte[]{});
    }
}
