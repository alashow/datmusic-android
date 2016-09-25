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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

import tm.alashow.datmusic.Config;
import tm.alashow.datmusic.util.U;

/**
 * Created by alashov on 07/05/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Audio {

    @JsonProperty("aid")
    private long id;

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("owner_id")
    private long ownerId;

    @JsonProperty("artist")
    private String artist;

    @JsonProperty("title")
    private String title;

    private long bytes = - 1;

    public Audio() {
    }

    public String getEncodedAudioId() {
        return String.format(Locale.ROOT, "%s:%s", U.encode(ownerId), U.encode(id));
    }

    public String getStreamUrl() {
        return String.format(Locale.ROOT, "%sstream/%s", Config.MAIN_SERVER, getEncodedAudioId());
    }

    public String getDownloadUrl() {
        return String.format(Locale.ROOT, "%s%s", Config.MAIN_SERVER, getEncodedAudioId());
    }

    public String getDownloadUrl(int bitrate) {
        if (! Config.isBitrateAllowed(bitrate)) {
            return getDownloadUrl();
        }
        return String.format(Locale.ROOT, "%s/%d", getDownloadUrl(), bitrate);
    }

    public float getBitrate() {
        return getBytes() * 8 / getDuration() / 1000;
    }

    public long getBytesForBitrate(int bitrate) {
        return bitrate / 8 * getDuration() * 1000;
    }

    public String getFileSize(){
        return U.humanReadableByteCount(getBytes(), false);
    }

    public String getFileSizeForBitrate(int bitrate){
        return U.humanReadableByteCount(getBytesForBitrate(bitrate), false);
    }

    public String getSafeFileName(int bitrate) {
        String audioName = getFullName();
        if (audioName.length() > 100) {
            audioName = audioName.substring(0, 100);
        }

        if (bitrate > 0) {
            audioName += String.format(Locale.ROOT, " (%d)", bitrate);
        }
        audioName += ".mp3";

        return U.sanitizeFilename(audioName);
    }

    public String getFullName() {
        return String.format(Locale.ROOT, "%s - %s", getArtist(), getTitle());
    }

    public long getId() {
        return id;
    }

    public Audio setId(long id) {
        this.id = id;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public Audio setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public Audio setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public String getArtist() {
        return artist.replace("&amp;", "&");
    }

    public Audio setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getTitle() {
        return title.replace("&amp;", "&");
    }

    public Audio setTitle(String title) {
        this.title = title;
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
