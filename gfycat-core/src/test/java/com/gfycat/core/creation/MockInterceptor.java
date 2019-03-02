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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static okhttp3.Protocol.HTTP_1_1;

final class MockInterceptor implements Interceptor {
    private Deque<Object> events = new ArrayDeque<Object>();
    private Deque<Request> requests = new ArrayDeque<Request>();

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        requests.addLast(request);

        Object event = events.removeFirst();
        if (event instanceof IOException) {
            throw (IOException) event;
        }
        if (event instanceof RuntimeException) {
            throw (RuntimeException) event;
        }
        if (event instanceof Response.Builder) {
            Response.Builder response = (Response.Builder) event;
            return response.request(request).protocol(HTTP_1_1).build();
        }
        throw new IllegalStateException("Unknown event " + event.getClass());
    }

    public MockInterceptor enqueueResponse(Response.Builder response) {
        events.addLast(response);
        return this;
    }

    public MockInterceptor enqueueUnexpectedException(RuntimeException exception) {
        events.addLast(exception);
        return this;
    }

    public MockInterceptor enqueueIOException(IOException exception) {
        events.addLast(exception);
        return this;
    }

    public Request takeRequest() {
        return requests.removeFirst();
    }
}