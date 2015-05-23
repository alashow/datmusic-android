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

package tm.alashow.music;/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

import tm.alashow.music.model.Audio;

public class Config {
    public static final String SERVER = "http://alashov.com/";
    public static final String ENDPOINT_API = SERVER + "music/app/";
    public static final String VK_SERVER = "https://api.vk.com/";
    public static final String VK_AUDIO_SEARCH = VK_SERVER + "method/audio.search";

    public static final String DOWNLOAD_FOLDER = "/AlashovMusic";

    public static String GCM_SENDER_ID = "635425098901";
    public static final boolean DEBUG = true;


    //Activity Tags
    public static String ACTIVITY_TAG_MAIN = "main";
    public static String ACTIVITY_TAG_LOGOUT = "logout";
    public static String ACTIVITY_TAG_PREFERENCES = "preferences";

    //Activity Extras
    public static final String EXTRA_QUERY = "tm.veriloft.music.QUERY";

    //VK Search Api Config
    public static String VK_CONFIG_ACCESS_TOKEN = "3d66c01979d977874ab8fe42166c8963fe496b52774b07d4edd7d35a2c29565424b3a0a381bd8c791715b";
    public static String VK_CONFIG_AUTOCOMPLETE = "1";
    public static String VK_CONFIG_COUNT = "300";
    public static String VK_CONFIG_SORT = "2";
    public static String VK_CONFIG_VERSION= "5.31";

    //LastFm Artist Search Api Config
    public static String LASTFM_CONFIG_ACCESS_TOKEN = "8b7af513f19366e766af02c85879b0ac";


    public static String getDownloadAudioLink(Audio audio){
        return "http://alashov.com/music/download.php?audio_id=" + audio.getOwnerId() + "_" + audio.getId();
    }
}
