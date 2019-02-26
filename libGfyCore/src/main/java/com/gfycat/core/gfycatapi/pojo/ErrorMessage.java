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

package com.gfycat.core.gfycatapi.pojo;

/**
 * Gfycat SDK error messages
 */
public class ErrorMessage {

    /**
     * Server generated error codes.
     */
    public final class Server {
        public static final String INVALID_USERNAME = "InvalidUsername";
        public static final String USERNAME_TAKEN = "UsernameTaken";
        public static final String USERNAME_UNAVAILABLE = "UsernameUnavailable";
        public static final String INVALID_PASSWORD = "InvalidPassword";
        public static final String INVALID_EMAIL = "InvalidEmail";
        public static final String EMAIL_TAKEN = "EmailTaken";
        public static final String USER_NOT_EXISTS = "UserNotExists";
        public static final String INVALID_ACCESS_TOKEN = "InvalidAccessToken";
    }

    /**
     * Client generated error codes.
     */
    public final class Client {
        public static final String FACEBOOK_CREATE_USER = "FacebookCreateUser";
        public static final String FACEBOOK_SIGN_IN_ERROR = "FacebookSignInError";
        public static final String WRONG_SERVER_RESPONSE = "WrongServerResponse";
        public static final String NO_RESPONSE_FROM_SERVER = "NoResponseFromServer";
        public static final String INTERNAL_APPLICATION_ERROR = "InternalApplicationError";
        public static final String EMAIL_NOT_VERIFIED = "EmailNotVerified";
    }

    /**
     * Client generated messages.
     */
    public class ClientMessage {
        public static final String WRONG_ERROR_MESSAGE_FORMAT = "Wrong error message format.";
        public static final String PLEASE_CHECK_INTERNET_CONNECTION = "Please check internet connection.";
        public static final String PLEASE_TRY_AGAIN_LATER = "Please try again later.";
        public static final String CAN_NOT_SIGN_IN_WITH_FACEBOOK = "Can not sign in with Facebook.";
        public static final String USERNAME_IS_INVALID = "The username was invalid.";
        public static final String USERNAME_IS_UNAVAILABLE = "Username is unavailable.";
        public static final String EMAIL_UNABLE_TO_VERIFY = "Unable to send verification email.";
        public static final String EMAIL_DOESNT_REGISTERED = "Sorry, that user does not have an email address registered.";
    }

    public ErrorMessage() {
    }

    public ErrorMessage(String code) {
        this.code = code;
    }

    public ErrorMessage(String code, String description) {
        this.code = code;
        this.description = description;
    }

    String code;
    String description;

    /**
     * @return Returns error code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Set error code.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return Returns error description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set error description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
