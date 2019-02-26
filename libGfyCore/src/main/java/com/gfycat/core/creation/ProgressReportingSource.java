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

import java.io.IOException;

import okio.Buffer;
import okio.ForwardingSource;
import okio.Source;

/**
 * Created by dekalo on 28.03.17.
 */

public class ProgressReportingSource extends ForwardingSource {

    private long totalBytesRead;
    private final long contentLength;
    private final ProgressListener progressListener;
    private int lastProgress = Integer.MAX_VALUE;

    public ProgressReportingSource(Source delegate, long contentLength, ProgressListener progressListener) {
        super(delegate);
        this.contentLength = contentLength;
        this.progressListener = progressListener;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        long bytesRead = super.read(sink, byteCount);
        if (bytesRead > 0) {
            totalBytesRead += bytesRead;
            notifyIfChanged((int) (100 * totalBytesRead / contentLength));
        }
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        super.close();
        Logging.d("GfycatCreationService", "ProgressReportingSource::close()");
    }

    private void notifyIfChanged(int progress) {
        if (progress != lastProgress) {
            progressListener.onProgress(lastProgress = progress);
        }
    }

    public interface ProgressListener {
        void onProgress(int progress);
    }
}
