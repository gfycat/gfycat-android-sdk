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

package com.gfycat.common.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Pair;

import com.gfycat.common.ChainedException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by dekalo on 28.11.16.
 */

public class MediaUtils {

    private static final String LOG_TAG = "MediaUtils";

    private static final String PICTURES_DIR = "Gfycat Loops";
    private static final String VIDEOS_DIR = "Gfycat";

    public static final SimpleDateFormat VIDEO_FILE_NAME_SUFFIX = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);

    public static void registerPictureInDeviceMediaDB(Context context, String pictureFilePath) {
        registerFileInDeviceMediaDB(context,
                MediaStore.Images.Media.DATA,
                pictureFilePath,
                MediaStore.Images.Media.MIME_TYPE,
                MimeTypeUtils.GIF_MIME_TYPE,
                null,
                -1,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    public static long getVideoFileDurationMillis(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(path);
            return Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } finally {
            Sugar.doIfNotNull(mediaMetadataRetriever, MediaMetadataRetriever::release);
        }
    }

    public static Single<String> observeVideoFilePathFrom(ContentResolver contentResolver, Uri contentResolverUri) {
        return Single.fromCallable(() -> {
            String filePathColumn = MediaStore.Video.Media.DATA;
            Cursor cursor = contentResolver.query(contentResolverUri, new String[] {filePathColumn}, null, null, null);
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(filePathColumn));
        });
    }

    public static void registerVideoInDeviceMediaDB(Context context, String videoFilePath) {
        Single.fromCallable(() -> Pair.create(MediaStore.Video.Media.DURATION, getVideoFileDurationMillis(videoFilePath)))
                .doOnError(throwable -> Assertions.fail(new ChainedException(throwable)))
                .onErrorReturn(throwable -> Pair.create("", 0L))
                .subscribeOn(Schedulers.computation())
                .subscribe(keyDurationPair -> registerFileInDeviceMediaDB(context,
                        MediaStore.Video.Media.DATA,
                        videoFilePath,
                        MediaStore.Video.Media.MIME_TYPE,
                        MimeTypeUtils.MP4_VIDEO_MIME_TYPE,
                        keyDurationPair.first,
                        keyDurationPair.second,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI));
    }

    private static void registerFileInDeviceMediaDB(Context context,
                                                    String dataKey,
                                                    String filePath,
                                                    String mimeKey,
                                                    String mimeType,
                                                    String durationKey,
                                                    long durationMs,
                                                    Uri contentUri) {
        Logging.d(LOG_TAG, "registerFileInDeviceMediaDB(", filePath, ")");
        File file = new File(filePath);
        Assertions.assertTrue(file.exists(), () -> new IllegalStateException("videoFile.exists()"));
        Assertions.assertTrue(file.length() > 0, () -> new IllegalStateException("videoFile.length() == 0"));
        ContentValues values = new ContentValues();
        values.put(dataKey, filePath);
        values.put(mimeKey, mimeType);
        if (!TextUtils.isEmpty(durationKey) && durationMs > 0) values.put(durationKey, durationMs);
        context.getContentResolver().insert(contentUri, values);
    }

    public static File getOutputGifFile(String prefix) throws CanNotCreateMediaFile {
        return getOutputMediaFile(prefix, Environment.DIRECTORY_PICTURES, "", MimeTypeUtils.GIF_EXT, PICTURES_DIR);
    }

    public static File getOutputVideoFile(String prefix) throws CanNotCreateMediaFile {
        return getOutputMediaFile(prefix, Environment.DIRECTORY_MOVIES, "", MimeTypeUtils.MP4_EXT, VIDEOS_DIR);
    }

    public static File getOutputVideoFileForCreation(String prefix) throws CanNotCreateMediaFile {
        String timeStamp = VIDEO_FILE_NAME_SUFFIX.format(new Date());
        return getOutputMediaFile(prefix, Environment.DIRECTORY_MOVIES, timeStamp, MimeTypeUtils.MP4_EXT, VIDEOS_DIR);
    }

    /**
     * Creates a media file in the {@code Environment.DIRECTORY_PICTURES} directory. The directory
     * is persistent and available to other applications like gallery.
     *
     * @return A file object pointing to the newly created file.
     */
    private static File getOutputMediaFile(String prefix, String environment, String timeStamp, String ext, String directoryName) throws CanNotCreateMediaFile {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            throw new CanNotCreateMediaFile("Environment.getExternalStorageState() = " + Environment.getExternalStorageState() + " but expected is " + Environment.MEDIA_MOUNTED);
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(environment), directoryName);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
            if (!mediaStorageDir.mkdirs()) {
                Logging.d(LOG_TAG, "failed to create directory");
                throw new CanNotCreateMediaFile("failed to create directory mediaStorageDir = " + mediaStorageDir);
            }

        // Create a media file name
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + prefix + timeStamp + "." + ext);

        Logging.d(LOG_TAG, "getOutputMediaFile(", mediaFile.getAbsolutePath(), ")");

        return mediaFile;
    }

    public static class CanNotCreateMediaFile extends Exception {
        public CanNotCreateMediaFile(String message) {
            super(message);
        }
    }
}