/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alashov on 07/05/15.
 */
public class Audio {
    private long id;
    private int duration;
    private long ownerId;
    private String src;
    private String artist;
    private String title;

    public Audio( JSONObject audioObject ) {
        try {
            setId(audioObject.getLong("aid"));
            setDuration(audioObject.getInt("duration"));
            setOwnerId(audioObject.getLong("owner_id"));
            setSrc(audioObject.getString("url"));
            setArtist(audioObject.getString("artist"));
            setTitle(audioObject.getString("title"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public Audio setId( long id ) {
        this.id = id;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public Audio setDuration( int duration ) {
        this.duration = duration;
        return this;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public Audio setOwnerId( long ownerId ) {
        this.ownerId = ownerId;
        return this;
    }

    public String getSrc() {
        return src;
    }

    public Audio setSrc( String src ) {
        this.src = src;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public Audio setArtist( String artist ) {
        this.artist = artist;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Audio setTitle( String title ) {
        this.title = title;
        return this;
    }
}
