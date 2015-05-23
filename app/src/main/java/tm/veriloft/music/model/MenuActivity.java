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

    public MenuActivity( String _title, String _activityTag ) {
        mTitle = _title;
        mIconResource = -1;
        mActivityTag = _activityTag;
    }
}