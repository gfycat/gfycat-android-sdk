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

package com.gfycat.core.storage;

import android.os.ParcelFileDescriptor;

import com.gfycat.common.utils.Logging;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Utility class to send content via ContentProvider::openFile(...)
 */
public class ParcelFileDescriptorUtil {

    public static ParcelFileDescriptor pipeFrom(InputStream inputStream) throws IOException {

        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor writeSide = pipe[1];

        // start the transfer thread
        new TransferThread(inputStream, new ParcelFileDescriptor.AutoCloseOutputStream(writeSide))
                .start();

        return readSide;
    }

    public static ParcelFileDescriptor pipeTo(OutputStream outputStream) throws IOException {

        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor writeSide = pipe[1];

        new TransferThread(new ParcelFileDescriptor.AutoCloseInputStream(readSide), outputStream).start();

        return writeSide;
    }

    public static ParcelFileDescriptor pipeFrom(OkHttpClient client, String url) throws IOException {

        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor writeSide = pipe[1];

        // start the transfer thread
        new UrlTransferThread(client, url, new ParcelFileDescriptor.AutoCloseOutputStream(writeSide))
                .start();

        return readSide;
    }

    static class UrlTransferThread extends TransferThread {

        final String url;
        private final OkHttpClient client;

        UrlTransferThread(OkHttpClient client, String url, OutputStream out) {
            super(null, out);
            this.url = url;
            this.client = client;
        }

        @Override
        void prepareStreams() throws IOException {
            mIn = client.newCall(new Request.Builder().url(url).build()).execute().body().byteStream();
        }
    }

    static class TransferThread extends Thread {

        private static final String LOG_TAG = TransferThread.class.getSimpleName();

        protected InputStream mIn;
        protected OutputStream mOut;

        TransferThread(InputStream in, OutputStream out) {
            this();
            mIn = in;
            mOut = out;
        }

        public TransferThread() {
            super("TransferThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            Logging.d(LOG_TAG, "run()");
            try {
                long total = 0;
                prepareStreams();
                IOUtils.copy(mIn, mOut);
                Logging.d(LOG_TAG, "writing() end");
            } catch (IOException e) {
                Logging.e(LOG_TAG, "writing failed");
            } finally {
                IOUtils.closeQuietly(mIn);
                IOUtils.closeQuietly(mOut);
            }
        }

        void prepareStreams() throws IOException {
        }
    }
}