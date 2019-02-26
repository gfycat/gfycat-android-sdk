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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.ThreadUtils;
import com.gfycat.core.GfyCore;
import com.gfycat.core.GfyPrivate;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Implementation of {@link VideoProviderContract}
 * <p/>
 * Created by dekalo on 22.09.15.
 */
public class VideoFileProvider extends ContentProvider {

    private static final String LOG_TAG = "VideoFileProvider";

    private static final int SUPPORTED_FORMAT_CODE = 1; // */*/*

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(VideoProviderContract.AUTHORITY, "*/*/*", SUPPORTED_FORMAT_CODE);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        switch (URI_MATCHER.match(uri)) {
            case SUPPORTED_FORMAT_CODE:

                MediaType mediaType = MediaType.valueOf(uri.getPathSegments().get(1));
                String gfyId = uri.getPathSegments().get(2);

                return cursorFor(mediaType, gfyId);

            default:
                Assertions.fail(new IllegalArgumentException("Unknown uri: " + uri));
                return null;
        }
    }

    private Cursor cursorFor(MediaType mediaType, String gfyId) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, 1);
        cursor.addRow(new Object[]{gfyId, mediaType.getSizeHint()});
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case SUPPORTED_FORMAT_CODE:
                MediaType mediaType = MediaType.valueOf(uri.getPathSegments().get(1));
                Logging.d(LOG_TAG, "::getType(", uri, ") return ", mediaType.getMimeType());
                return mediaType.getMimeType();
            default:
                Assertions.fail(new IllegalArgumentException("Unknown uri: " + uri));
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Assertions.fail(new UnsupportedOperationException(LOG_TAG + "::insert(" + uri + ")"));
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Assertions.fail(new UnsupportedOperationException(LOG_TAG + "::delete(" + uri + ")"));
        return -1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Assertions.fail(new UnsupportedOperationException(LOG_TAG + "::update(" + uri + ")"));
        return -1;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {

        Logging.d(LOG_TAG, "::openFile(", uri, ", ", mode, ")");

        guard(uri, mode);

        Params params = paramsFromUri(uri);

        if (VideoProviderContract.SharingType.CACHED.equals(params.sharingType)) {
            return getCachedParcelFileDescriptor(params);
        } else if (VideoProviderContract.SharingType.REMOTE.equals(params.sharingType)) {
            return getRemoteParcelFileDescriptor(params);
        } else {
            logAndThrowFileNotFound("Unknown sharing type for uri = " + uri + " s sharing type = " + params.sharingType);
        }
        return null;
    }

    private ParcelFileDescriptor getCachedParcelFileDescriptor(Params params) throws FileNotFoundException {

        DiskCache diskCache = DefaultDiskCache.get();

        if (!diskCache.isAvailable()) {
            logAndThrowFileNotFound("VideoFileProvider::openFile() diskCache.isAvailable = false, can not return anything.");
        }

        File videoFile = diskCache.get(params.gfyId);

        if (videoFile == null) {
            logAndThrowFileNotFound("VideoFileProvider::openFile() videoFile == null.");
        }

        Logging.d(LOG_TAG, "::openFile(...) success gfyId = ", params.gfyId, " file = ", videoFile.getAbsolutePath());

        return ParcelFileDescriptor.open(videoFile, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    /**
     * @throws FileNotFoundException always.
     */
    private static void logAndThrowFileNotFound(String message) throws FileNotFoundException {
        Logging.d(LOG_TAG, message);
        Assertions.fail(new FileNotFoundException(message));
        throw new FileNotFoundException(message);
    }

    private void guard(Uri uri, String mode) throws FileNotFoundException {
        Assertions.assertNotUIThread(IllegalAccessException::new);

        if (!"r".equals(mode)) {
            logAndThrowFileNotFound("VideoFileProvider::openFile() trying to access " + uri + " in mode = " + mode);
        }
    }

    private ParcelFileDescriptor getRemoteParcelFileDescriptor(final Params params) throws FileNotFoundException {

        Gfycat gfycat = getGfycatSync(params.gfyId);

        if (gfycat == null) throw new FileNotFoundException("Can not get gfycat item.");
        String url = ThreadUtils.withClearIdentitySafe(() -> params.mediaType.getUrl(gfycat));

        try {
            return ParcelFileDescriptorUtil.pipeFrom(GfyPrivate.get().getVideoDownloadingClient(), url);
        } catch (IOException e) {
            logAndThrowFileNotFound("IOException occurred : " + e);
            return null;
        }
    }

    private Gfycat getGfycatSync(String gfyId) {
        return GfyCore.getFeedManager().getGfycat(gfyId).blockingGet();
    }

    private static Params paramsFromUri(Uri uri) throws FileNotFoundException {

        if (URI_MATCHER.match(uri) == UriMatcher.NO_MATCH)
            logAndThrowFileNotFound("Unsupported uri = " + uri);

        VideoProviderContract.SharingType sharingType = VideoProviderContract.SharingType.valueOf(uri.getPathSegments().get(0));
        MediaType mediaType = MediaType.valueOf(uri.getPathSegments().get(1));
        String gfyId = uri.getPathSegments().get(2);

        if (TextUtils.isEmpty(gfyId)) {
            logAndThrowFileNotFound("Can not parse params from uri = " + uri + " (gfyId = " + gfyId + " mediaType = " + mediaType + " sharingType = " + sharingType + ")");
        }

        return new Params(sharingType, mediaType, gfyId);
    }

    private static class Params {

        private final VideoProviderContract.SharingType sharingType;
        private final MediaType mediaType;
        private final String gfyId;

        public Params(VideoProviderContract.SharingType sharingType, MediaType mediaType, String gfyId) {
            this.sharingType = sharingType;
            this.mediaType = mediaType;
            this.gfyId = gfyId;
        }
    }
}
