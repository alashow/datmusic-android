/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.model;

/**
 * Left Side Slide Menu Item Object
 */
public class MenuActivity {

    public String mTitle;
    public int mIconResource;
    public String mActivityTag;

    /**
     * @param _title       Title of page
     * @param _icon        IconResource drawable
     * @param _activityTag Fragment tag
     */
    public MenuActivity( String _title, int _icon, String _activityTag ) {
        mTitle = _title;
        mIconResource = _icon;
        mActivityTag = _activityTag;
    }
}