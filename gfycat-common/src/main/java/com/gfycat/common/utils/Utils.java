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

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.gfycat.common.ChainedException;
import com.gfycat.common.Func1;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by dekalo on 13.10.16.
 */

public class Utils {
    private final static String TAG = "Utils";

    public static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    public static final SimpleDateFormat SMALL_TIME = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    private static final String ENCODING_CHARSET = "UTF-8";

    public static final int MB = 1024 * 1024;

    private static final long SECOND = 1000;
    private static final long SECONDS_IN_MINUTE = 60;
    private static final long MINUTE = SECONDS_IN_MINUTE * SECOND;
    private static final long MINUTES_IN_HOUR = 60;
    private static final long HOUR = MINUTES_IN_HOUR * MINUTE;
    private static final long HOURS_IN_DAY = 24;
    private static final long DAY = HOURS_IN_DAY * HOUR;

    private static final Random random = new Random();

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static String safeEncode(String decoded) {
        try {
            return URLEncoder.encode(decoded, ENCODING_CHARSET);
        } catch (UnsupportedEncodingException e) {
            Assertions.fail(new IllegalArgumentException("Unsupported encoding exception.", e));
            return decoded;
        }
    }

    public static String safeDecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, ENCODING_CHARSET);
        } catch (UnsupportedEncodingException e) {
            Assertions.fail(new IllegalArgumentException("Failed to decode " + encoded + ".", e));
            return encoded;
        }
    }

    /**
     * Serialize List of Strings to byte[] via ObjectStream.
     */
    public static byte[] serializeListOfStrings(List<String> stringsList) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(new ArrayList<>(stringsList));
            return bos.toByteArray();
        } catch (IOException e) {
            Assertions.fail(new Exception("serializeListOfStrings(...)", e));
        } finally {
            IOUtils.closeQuietly(oos);
        }
        return null;
    }

    /**
     * Deserialize List of Strings to byte[] via ObjectStream.
     */
    public static List<String> deSerializeListOfStrings(byte[] bytes) {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(bais);
            return (List<String>) ois.readObject();
        } catch (ClassCastException | IOException | ClassNotFoundException e) {
            Assertions.fail(new Exception("deSerializeListOfStrings(...)", e));
        } finally {
            IOUtils.closeQuietly(ois);
        }
        return Collections.emptyList();
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static void startActivityIfConnected(Context context, Intent intent, String fallbackToastMessage) {
        if (isConnected(context)) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, fallbackToastMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String collectRuntimeMemoryUsage() {
        Runtime r = Runtime.getRuntime();
        return String.format(Locale.US, "total: %s free: %s max: %s",
                humanReadableByteCount(r.totalMemory()),
                humanReadableByteCount(r.freeMemory()),
                humanReadableByteCount(r.maxMemory()));
    }

    public static String collectDebugNativeMemoryUsage(Context context) {
        return String.format(Locale.US, "nativeAllocated: %s nativeFree: %s nativeHeap: %s",
                humanReadableByteCount(Debug.getNativeHeapAllocatedSize()),
                humanReadableByteCount(Debug.getNativeHeapFreeSize()),
                humanReadableByteCount(Debug.getNativeHeapSize()));
    }

    public static int getMemoryClass(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return manager.getMemoryClass();
    }

    public static String collectMemoryInfoUsage(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        return String.format(
                Locale.US,
                "availMem: %s totalMem: %s lowMemory: %b threshold: %s memoryClass: %d largeMemoryClass: %d",
                humanReadableByteCount(mi.availMem),
                humanReadableByteCount(mi.totalMem),
                mi.lowMemory,
                humanReadableByteCount(mi.threshold),
                activityManager.getMemoryClass(),
                activityManager.getLargeMemoryClass());
    }

    public static String formatTimeInterval(long time, long base, String higher, String lower) {
        return time / base + higher + " " + time % base + lower;
    }

    public static String humanReadableTimeInterval(long timeMs) {
        if (timeMs < SECOND) return timeMs + "ms";
        if (timeMs < MINUTE) return formatTimeInterval(timeMs, SECOND, "s", "ms");
        if (timeMs < HOUR) return formatTimeInterval(timeMs / SECOND, SECONDS_IN_MINUTE, "m", "s");
        if (timeMs < DAY) return formatTimeInterval(timeMs / MINUTE, MINUTES_IN_HOUR, "h", "m");
        return formatTimeInterval(timeMs / HOUR, DAY, "d", "h");
    }

    public static String humanReadableTimeSmall(long timeMs) {
        return SMALL_TIME.format(new Date(timeMs));
    }

    public static String humanReadableTime(long timeMs) {
        return ISO8601.format(new Date(timeMs));
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), "kMGTPE".charAt(exp - 1));
    }

    public static String humanReadableVideoDuration(int seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }

    public static String collectAllMemoryUsageInfo(Context context) {
        return Utils.collectRuntimeMemoryUsage() + " | " +
                Utils.collectDebugNativeMemoryUsage(context) + " | " +
                Utils.collectMemoryInfoUsage(context);
    }

    public static String getDeviceID(Context context) {
        String secure = "no-device-id";
        try {
            secure = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            Assertions.fail(e);
        }
        return secure;
    }

    public static String getApplicationId(Context context) {
        return context.getPackageName();
    }

    public static String getVersionName(Context context) {
        String result = "";

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            result = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        } catch (RuntimeException e) {
            // possible issue: Caused by java.lang.RuntimeException: Package manager has died
            Assertions.fail(e);
        }

        return result;
    }

    public static int getAndDrop(Intent intent, String key, int defaultValue) {
        try {
            return intent.getIntExtra(key, defaultValue);
        } finally {
            intent.removeExtra(key);
        }
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void dumpCursor(String logTag, String message, Cursor cursor) {
        Logging.d(logTag, ">>> Dumping ", message, " ", cursor.hashCode());
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            Logging.d(logTag, dumpRow(cursor));
        }
        Logging.d(logTag, "<<< ", cursor.hashCode());
    }

    public static void safeDeleteFileByUri(Uri uri) {
        try {
            Sugar.doIfNotNull(getFileFromUri(uri), File::delete);
        } catch (FileNotFoundException e) {
            Assertions.fail(e);
        }
    }

    public static void deleteFileByUri(Uri uri) throws FileNotFoundException {
        Sugar.doIfNotNull(getFileFromUri(uri), File::delete);
    }

    public static File getFileFromUri(@NonNull Uri uri) throws FileNotFoundException {
        if (uri.getScheme().equals("file")) {
            return new File(uri.getPath());
        } else {
            throw new FileNotFoundException("Uri scheme " + uri.getScheme() + " not supported.");
        }
    }


    public static Uri getUriFromStream(Intent intent) {
        if (intent.getExtras() == null || !intent.getExtras().containsKey(Intent.EXTRA_STREAM))
            return null;
        try {
            return intent.getParcelableExtra(Intent.EXTRA_STREAM);
        } catch (ClassCastException e) {
            Logging.e(TAG, e, "ClassCastException happens.");
            return null;
        }
    }

    public static Uri getUriFromData(Intent intent) {
        if (TextUtils.isEmpty(intent.getDataString())) return null;
        return intent.getData();
    }

    private static String dumpRow(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        String[] cols = cursor.getColumnNames();
        sb.append("").append(cursor.getPosition()).append(" {");
        int length = cols.length;
        for (int i = 0; i < length; i++) {
            String value;
            try {
                value = cursor.getString(i);
            } catch (SQLiteException e) {
                // assume that if the getString threw this exception then the column is not
                // representable by a string, e.g. it is a BLOB.
                value = "<unprintable>";
            }
            sb.append(" ").append(cols[i]).append('=').append(value);
        }
        sb.append("}");

        return sb.toString();
    }

    public static int getPixelSize(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else {
            throw new UnsupportedOperationException("Unknown config.");
        }
    }

    public static boolean hasExternalStoragePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPermission(Context context, String permissionName) {
        return ContextCompat.checkSelfPermission(context, permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * generates random string that contains next characters 0..9, a..z, A..Z
     */
    public static String randomString(int size) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append(randomChar());
        }
        return builder.toString();
    }

    private static char randomChar() {

        int numbersSize = '9' - '0';
        int smallAlphabetSize = 'z' - 'a';
        int bigAlphabetSize = 'Z' - 'A';

        int index = random.nextInt(numbersSize + smallAlphabetSize + bigAlphabetSize);

        if (index <= numbersSize) {
            return (char) ('0' + index);
        } else if (index > numbersSize && index <= numbersSize + smallAlphabetSize) {
            return (char) ('a' + index - numbersSize);
        } else if (index > numbersSize + smallAlphabetSize && index <= numbersSize + smallAlphabetSize + bigAlphabetSize) {
            return (char) ('A' + index - numbersSize - smallAlphabetSize);
        } else {
            Assertions.fail(new IllegalStateException("index(" + index + ") out of bounds"));
            return '0';
        }
    }

    /**
     * Logging info for http://crashes.to/s/1575d4c0424
     */
    public static File createFileRuntimeSafe(String path) throws IOException {
        try {
            return new File(path);
        } catch (NullPointerException e) {
            Assertions.fail(new ChainedException("NullPointerException while creating file from path = " + path, e));
            throw new IOException(e);
        }
    }

    public static <T, R> List<R> map(List<T> source, Func1<T, R> map) {
        List<R> result = new ArrayList<>(source.size());
        for (T item : source) {
            result.add(map.call(item));
        }
        return result;
    }

    public static boolean isExpired(long timeMs, long thresholdMs) {
        return timeMs + thresholdMs < System.currentTimeMillis();
    }

    public static Date parseDateSafe(String date, DateFormat format, Date defaultValue) {
        try {
            return format.parse(date);
        } catch (ParseException | IndexOutOfBoundsException e) {
            Log.e(TAG, "parseDateSafe(" + date + ", " + format + ") FAILED");
            return defaultValue;
        }
    }

    /**
     * Searches values inside array.
     * Returns true if there are such, false otherwise.
     */
    public static <T> boolean contains(T[] array, T value) {
        for (T item : array) {
            if (equals(item, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches values inside array.
     * Returns true if there are such, false otherwise.
     */
    public static <T> int find(T[] array, T value) {
        for (int index = 0; index < array.length; index++) {
            if (equals(array[index], value)) {
                return index;
            }
        }
        return -1;
    }

    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

    /**
     * Collects items from iterator to list.
     */
    public static <T> List<T> collect(Iterator<T> iterator) {
        ArrayList<T> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }
}
