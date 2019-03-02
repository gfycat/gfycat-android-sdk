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

package com.gfycat.core.notifications;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;

import com.gfycat.common.utils.Logging;
import com.gfycat.core.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Khloponin
 */
public class NotificationManager {
    private static final String LOG_TAG = "NotificationManager";

    public static final String CHANNEL_PUSH_UPDATES = "channel_push_updates";
    public static final String CHANNEL_VIDEO_READY = "channel_video_ready";
    public static final String CHANNEL_VIDEO_PROCESSING = "channel_video_processing";

    private static NotificationManager sInstance;

    public static NotificationManager getInstance() {
        if (sInstance == null) {
            sInstance = new NotificationManager();
        }
        return sInstance;
    }


    private Map<Integer, NotificationCompat.Builder> notifications = new HashMap<>();
    private Map<Pair<String, Integer>, NotificationCompat.Builder> taggedNotifications = new HashMap<>();


    private NotificationManager() {
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void prepareChannels(Context context) {
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(prepareChannelVideoReady(context));
        notificationManager.createNotificationChannel(prepareChannelProcessing(context));
        notificationManager.createNotificationChannel(prepareChannelPushUpdates(context));
    }

    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel prepareChannelVideoReady(Context context) {
        String name = context.getString(R.string.channel_video_ready_name);
        String description = context.getString(R.string.channel_video_ready_description);
        int importance = android.app.NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(CHANNEL_VIDEO_READY, name, importance);
        channel.setDescription(description);
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        return channel;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel prepareChannelProcessing(Context context) {
        String name = context.getString(R.string.channel_gif_processing_name);
        String description = context.getString(R.string.channel_gif_processing_description);
        int importance = android.app.NotificationManager.IMPORTANCE_MIN;

        NotificationChannel channel = new NotificationChannel(CHANNEL_VIDEO_PROCESSING, name, importance);
        channel.setDescription(description);
        return channel;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel prepareChannelPushUpdates(Context context) {
        String name = context.getString(R.string.channel_push_updates_name);
        String description = context.getString(R.string.channel_push_updates_description);
        int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(CHANNEL_PUSH_UPDATES, name, importance);
        channel.setDescription(description);
        return channel;
    }

    public boolean remove(int id) {
        Logging.i(LOG_TAG, "remove id: " + id);
        return notifications.remove(id) != null;
    }

    public void cancel(Context context, int id) {
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    public void addAndSend(Context context, String tag, int id, @NonNull String channelId, UpdateNotificationInterface update) {
        Logging.i(LOG_TAG, "addAndSend id: " + id);
        NotificationCompat.Builder builder = taggedNotifications.get(Pair.create(tag, id));
        Logging.d(LOG_TAG, "builder: " + builder);
        if (builder == null) builder = new NotificationCompat.Builder(context, channelId);
        if (update != null) update.updateNotification(builder);
        taggedNotifications.put(Pair.create(tag, id), builder);
        sendNotification(context, id, builder);
    }

    public void addAndSend(Context context, int id, @NonNull String channelId, UpdateNotificationInterface update) {
        Logging.i(LOG_TAG, "addAndSend id: " + id);
        NotificationCompat.Builder builder = notifications.get(id);
        Logging.d(LOG_TAG, "builder: " + builder);
        if (builder == null) builder = new NotificationCompat.Builder(context, channelId);
        if (update != null) update.updateNotification(builder);
        notifications.put(id, builder);
        sendNotification(context, id, builder);
    }

    public void update(int id, UpdateNotificationInterface update) {
        Logging.i(LOG_TAG, "update id: " + id);
        NotificationCompat.Builder builder = notifications.get(id);
        Logging.d(LOG_TAG, "builder: " + builder);
        if (builder == null) return;
        if (update != null) update.updateNotification(builder);
    }

    public void updateAndSend(Context context, int id, UpdateNotificationInterface update) {
        Logging.i(LOG_TAG, "updateAndSend id: " + id);
        NotificationCompat.Builder builder = notifications.get(id);
        Logging.d(LOG_TAG, "builder: " + builder);
        if (builder == null) return;
        if (update != null) update.updateNotification(builder);
        sendNotification(context, id, builder);
    }


    private void sendNotification(Context context, int id, NotificationCompat.Builder builder) {
        Logging.i(LOG_TAG, "sendNotification id: " + id + ", builder: " + builder);
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, builder.build());
    }


    public interface UpdateNotificationInterface {
        void updateNotification(@NonNull NotificationCompat.Builder builder);
    }
}