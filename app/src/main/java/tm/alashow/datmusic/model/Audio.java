/*
 * Copyright 2015. Alashov Berkeli
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tm.alashow.datmusic.model;

import org.json.JSONException;
import org.json.JSONObject;

import tm.alashow.datmusic.Config;
import tm.alashow.datmusic.util.U;

/**
 * Created by alashov on 07/05/15.
 */
public class Audio {
    private long id;
    private long ownerId;
    private int duration;
    private String artist;
    private String title;

    private String audioId = "";
    private String downloadUrl;
    private String secureDownloadUrl;
    private String streamUrl;

    private long bytes = -1;

    public Audio(JSONObject audioObject) {
        try {
            this.id = audioObject.getLong("aid");
            this.duration = audioObject.getInt("duration");
            this.ownerId = audioObject.getLong("owner_id");
            this.artist = audioObject.getString("artist").replace("&amp;", "&");
            this.title = audioObject.getString("title").replace("&amp;", "&");

            if (ownerId < 0) {
                ownerId *= -1;
                this.audioId += "-";
            }
            this.audioId += U.encode((int) ownerId) + ":" + U.encode((int) id);

            this.downloadUrl = Config.ENDPOINT_API + audioId;
            this.secureDownloadUrl = Config.SECURE_SERVER + audioId;
            this.streamUrl = Config.ENDPOINT_API + "stream/" + audioId;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public Audio setId(long id) {
        this.id = id;
        return this;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public Audio setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public Audio setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public Audio setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Audio setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAudioId() {
        return audioId;
    }

    public Audio setAudioId(String audioId) {
        this.audioId = audioId;
        return this;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public Audio setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public String getSecureDownloadUrl() {
        return secureDownloadUrl;
    }

    public Audio setSecureDownloadUrl(String secureDownloadUrl) {
        this.secureDownloadUrl = secureDownloadUrl;
        return this;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public Audio setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
        return this;
    }

    public long getBytes() {
        return bytes;
    }

    public Audio setBytes(long bytes) {
        this.bytes = bytes;
        return this;
    }
}
