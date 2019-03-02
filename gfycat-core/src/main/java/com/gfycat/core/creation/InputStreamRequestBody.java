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

import com.gfycat.common.utils.Logging;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by dekalo on 23.03.17.
 */

public class InputStreamRequestBody extends RequestBody {

    private final InputStream inputStream;
    private final MediaType mediaType;
    private final ProgressReportingSource.ProgressListener listener;


    public InputStreamRequestBody(MediaType mediaType, InputStream inputStream, ProgressReportingSource.ProgressListener listener) {
        this.inputStream = inputStream;
        this.mediaType = mediaType;
        this.listener = listener;
    }

    @Override
    public long contentLength() throws IOException {
        int result = Integer.MIN_VALUE;
        try {
            return result = inputStream.available();
        } catch (IOException e) {
            Logging.d("GfycatCreationService", e, "InputStreamRequestBody::contentLength() = IOException");
            throw e;
        } finally {
            Logging.d("GfycatCreationService", "InputStreamRequestBody::contentLength() = " + result);
        }
    }

    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Logging.d("GfycatCreationService", "InputStreamRequestBody::writeTo()");
        Source source = null;
        try {
            source = new ProgressReportingSource(Okio.source(inputStream), contentLength(), listener);
            sink.writeAll(source);
        } finally {
            Util.closeQuietly(source);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        IOUtils.closeQuietly(inputStream);
    }
}
