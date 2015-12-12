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

public class Config {
    public static final String SERVER = "https://datmusic.xyz/";
    public static final String SEARCH = SERVER + "search/";
    public static final String ENDPOINT_API = SERVER + "app/";

    public static final String DOWNLOAD_FOLDER = "/datmusic";

    public static String GCM_SENDER_ID = "635425098901";
    public static final boolean DEBUG = true;

    //Activity Tags
    public static String ACTIVITY_TAG_MAIN = "main";
    public static String ACTIVITY_TAG_LOGOUT = "logout";
    public static String ACTIVITY_TAG_PREFERENCES = "preferences";

    //Activity Extras
    public static final String EXTRA_QUERY = "tm.veriloft.music.QUERY";

    //VK Search Api Config
    public static String VK_CONFIG_ACCESS_TOKEN = "e9dbafe947e48136f15bbaf1184095282f53bb146441910421e180b46fa6cf6cf8c37f7de3f525d2c121d";
    public static String VK_CONFIG_AUTOCOMPLETE = "1";
    public static String VK_CONFIG_COUNT = "300";
    public static String VK_CONFIG_SORT = "2";
}
