/*
 * MIT License
 *
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Contains changes from (c) Gfycat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#pragma once

#include <jni.h>

namespace facebook {

/**
 * Instructs the JNI environment to throw an exception.
 *
 * @param pEnv JNI environment
 * @param szClassName class name to throw
 * @param szFmt sprintf-style format string
 * @param ... sprintf-style args
 * @return 0 on success; a negative value on failure
 */
jint throwException(JNIEnv* pEnv, const char* szClassName, const char* szFmt, va_list va_args);

/**
 * Instructs the JNI environment to throw a NoClassDefFoundError.
 *
 * @param pEnv JNI environment
 * @param szFmt sprintf-style format string
 * @param ... sprintf-style args
 * @return 0 on success; a negative value on failure
 */
jint throwNoClassDefError(JNIEnv* pEnv, const char* szFmt, ...);

/**
 * Instructs the JNI environment to throw a RuntimeException.
 *
 * @param pEnv JNI environment
 * @param szFmt sprintf-style format string
 * @param ... sprintf-style args
 * @return 0 on success; a negative value on failure
 */
jint throwRuntimeException(JNIEnv* pEnv, const char* szFmt, ...);

/**
 * Instructs the JNI environment to throw a IllegalArgumentException.
 *
 * @param pEnv JNI environment
 * @param szFmt sprintf-style format string
 * @param ... sprintf-style args
 * @return 0 on success; a negative value on failure
 */
jint throwIllegalArgumentException(JNIEnv* pEnv, const char* szFmt, ...);

/**
 * Instructs the JNI environment to throw a IllegalStateException.
 *
 * @param pEnv JNI environment
 * @param szFmt sprintf-style format string
 * @param ... sprintf-style args
 * @return 0 on success; a negative value on failure
 */
jint throwIllegalStateException(JNIEnv* pEnv, const char* szFmt, ...);

/**
 * Instructs the JNI environment to throw an IOException.
 *
 * @param pEnv JNI environment
 * @param szFmt sprintf-style format string
 * @param ... sprintf-style args
 * @return 0 on success; a negative value on failure
 */
jint throwIOException(JNIEnv* pEnv, const char* szFmt, ...);

/**
 * Instructs the JNI environment to throw an AssertionError.
 *
 * @param pEnv JNI environment
 * @param szFmt sprintf-style format string
 * @param ... sprintf-style args
 * @return 0 on success; a negative value on failure
 */
jint throwAssertionError(JNIEnv* pEnv, const char* szFmt, ...);

/**
 * Instructs the JNI environment to throw an OutOfMemoryError.
 *
 * @param pEnv JNI environment
 * @param szFmt sprintf-style format string
 * @param ... sprintf-style args
 * @return 0 on success; a negative value on failure
 */
jint throwOutOfMemoryError(JNIEnv* pEnv, const char* szFmt, ...);

/**
 * Finds the specified class. If it's not found, instructs the JNI environment to throw an
 * exception.
 *
 * @param pEnv JNI environment
 * @param szClassName the classname to find in JNI format (e.g. "java/lang/String")
 * @return the class or NULL if not found (in which case a pending exception will be queued). This
 *     returns a global reference (JNIEnv::NewGlobalRef).
 */
jclass findClassOrThrow(JNIEnv *pEnv, const char* szClassName);

/**
 * Finds the specified field of the specified class. If it's not found, instructs the JNI
 * environment to throw an exception.
 *
 * @param pEnv JNI environment
 * @param clazz the class to lookup the field in
 * @param szFieldName the name of the field to find
 * @param szSig the signature of the field
 * @return the field or NULL if not found (in which case a pending exception will be queued)
 */
jfieldID getFieldIdOrThrow(JNIEnv* pEnv, jclass clazz, const char* szFieldName, const char* szSig);

/**
 * Finds the specified method of the specified class. If it's not found, instructs the JNI
 * environment to throw an exception.
 *
 * @param pEnv JNI environment
 * @param clazz the class to lookup the method in
 * @param szMethodName the name of the method to find
 * @param szSig the signature of the method
 * @return the method or NULL if not found (in which case a pending exception will be queued)
 */
jmethodID getMethodIdOrThrow(
    JNIEnv* pEnv,
    jclass clazz,
    const char* szMethodName,
    const char* szSig);

} // namespace facebook
