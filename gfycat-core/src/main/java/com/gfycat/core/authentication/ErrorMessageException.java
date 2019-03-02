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

package com.gfycat.core.authentication;

import com.gfycat.core.gfycatapi.pojo.ErrorMessage;
import com.gfycat.core.gfycatapi.pojo.GenericResponse;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import java.io.IOException;

import retrofit2.HttpException;

/**
 * Created by dekalo on 14.07.17.
 */

public class ErrorMessageException extends Throwable {

    public static ErrorMessageException fromRawThrowable(Throwable throwable) {
        if (throwable instanceof ErrorMessageException) {
            return (ErrorMessageException) throwable;
        } else if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            GenericResponse genericResponse = null;
            try {
                genericResponse = new Gson().fromJson(httpException.response().errorBody().charStream(), GenericResponse.class);
            } catch (JsonIOException e) {
                return new ErrorMessageException(new ErrorMessage(ErrorMessage.Client.WRONG_SERVER_RESPONSE, ErrorMessage.ClientMessage.WRONG_ERROR_MESSAGE_FORMAT));
            }
            if (genericResponse == null || genericResponse.getErrorMessage() == null || genericResponse.getErrorMessage().getCode() == null)
                return new ErrorMessageException(new ErrorMessage(ErrorMessage.Client.WRONG_SERVER_RESPONSE, ErrorMessage.ClientMessage.WRONG_ERROR_MESSAGE_FORMAT));
            else
                return new ErrorMessageException(genericResponse.getErrorMessage());
        } else if (throwable instanceof IOException) {
            return new ErrorMessageException(new ErrorMessage(ErrorMessage.Client.NO_RESPONSE_FROM_SERVER, ErrorMessage.ClientMessage.PLEASE_CHECK_INTERNET_CONNECTION));
        } else {
            return new ErrorMessageException(new ErrorMessage(ErrorMessage.Client.INTERNAL_APPLICATION_ERROR, ErrorMessage.ClientMessage.PLEASE_TRY_AGAIN_LATER));
        }
    }

    private final ErrorMessage errorMessage;

    public ErrorMessageException(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ErrorMessageException{" +
                "errorMessage=" + errorMessage +
                '}';
    }
}
