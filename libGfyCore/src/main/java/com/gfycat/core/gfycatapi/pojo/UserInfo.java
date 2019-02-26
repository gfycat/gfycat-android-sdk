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

import android.text.TextUtils;


/**
 * User profile information
 */
public class UserInfo {

    String userid;
    String name;
    String username;
    int views;
    String description;

    String profileUrl;
    String url;
    boolean emailVerified;
    String profileImageUrl;
    String graphImageUrl;

    /**
     * @return Returns an url for user profile picture.
     */
    public String getUserProfilePictureUrl() {
        if (!TextUtils.isEmpty(profileImageUrl)) {
            return profileImageUrl;
        } else if (!TextUtils.isEmpty(graphImageUrl)) {
            return graphImageUrl;
        } else {
            return "";
        }
    }

    /**
     * @return Returns a userId.
     */
    public String getUserid() {
        if (userid != null) return userid;
        if (username != null) return username.toLowerCase();
        return null;
    }

    /**
     * Set userId.
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return Returns username.
     */
    public String getName() {
        return name;
    }

    /**
     * Set username.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns views count.
     */
    public int getViews() {
        return views;
    }

    /**
     * Set views count.
     */
    public void setViews(int views) {
        this.views = views;
    }

    /**
     * @return Returns description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Use {@link UserInfo#getUserProfilePictureUrl()} instead.
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Set profile image url.
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Use {@link UserInfo#getUserProfilePictureUrl()} instead.
     */
    public String getGraphImageUrl() {
        return graphImageUrl;
    }

    /**
     * Set graph image url.
     */
    public void setGraphImageUrl(String graphImageUrl) {
        this.graphImageUrl = graphImageUrl;
    }

    /**
     * @return Returns url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set url.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return Returns true if email is verified, false otherwise.
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Set email verified state.
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * Set username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Use {@link UserInfo#getVisibleUserName()} for UI or {@link UserInfo#getUserid()} for unique identifier.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return Returns user visible username.
     */
    public String getVisibleUserName() {
        if (username != null) return username;
        return userid;
    }

    /**
     * @return Returns profile url.
     */
    public String getProfileUrl() {
        return profileUrl;
    }

    /**
     * Set profile url.
     */
    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    /**
     * Generate {@link UserInfo} with userid only.
     */
    public static UserInfo from(String userid) {
        UserInfo result = new UserInfo();
        result.setUserid(userid);
        return result;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userid='" + userid + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
