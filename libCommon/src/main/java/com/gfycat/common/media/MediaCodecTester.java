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

package com.gfycat.common.media;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;

import com.gfycat.common.utils.ThreadUtils;
import com.gfycat.common.utils.Logging;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dekalo on 02.06.16.
 */
public class MediaCodecTester {

    private static final java.lang.String LOG_TAG = "MediaCodecTester";

    public static int testPossibleMediaCodecCount(Context context, Uri uri, int desired) throws DecodingException {

        Logging.d(LOG_TAG, "testPossibleMediaCodecCount(", context, ", ", uri, ", ", desired, ") start");
        long startTime = System.currentTimeMillis();

        MediaExtractor mediaExtractor = new MediaExtractor();
        List<MediaCodec> mediaCodecs = new LinkedList<>();

        int codecCount = 0;

        try {

            mediaExtractor.setDataSource(context, uri, null);
            int trackIndex = selectTrack(mediaExtractor);

            if (trackIndex < 0)
                throw new DecodingException("No video track in media file.");

            mediaExtractor.selectTrack(trackIndex);
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(trackIndex);


            while (codecCount < desired) {
                MediaCodec mediaCodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
                mediaCodecs.add(mediaCodec);
                mediaCodec.configure(mediaFormat, null, null, 0);
                codecCount++;
            }

        } catch (IOException ioe) {
            if (codecCount == 0) throw new DecodingException("io exception happens", ioe);
        } catch (IllegalStateException e) {
            // not able
        } finally {
            ThreadUtils.withSilently(mediaExtractor, MediaExtractor::release);
            for (MediaCodec mediaCodec : mediaCodecs) {
                ThreadUtils.withSilently(mediaCodec, MediaCodec::stop);
                ThreadUtils.withSilently(mediaCodec, MediaCodec::release);
            }
            Logging.c(LOG_TAG, "testPossibleMediaCodecCount() end codecCount = ", codecCount, " time = ", (System.currentTimeMillis() - startTime));
        }

        if(codecCount == 0) throw new DecodingException("Impossible to instantiate even 1 MediaCodec.");

        return codecCount;
    }

    /**
     * Selects the video track, if any.
     *
     * @return the track index, or -1 if no video track is found.
     */
    private static int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }

    public static class DecodingException extends Throwable {
        public DecodingException(String message, IOException ioe) {
            super(message, ioe);
        }

        public DecodingException(String message) {
            super(message);
        }
    }
}
