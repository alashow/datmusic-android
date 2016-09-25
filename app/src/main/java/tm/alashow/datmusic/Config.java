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

package tm.alashow.datmusic;
/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

import tm.alashow.datmusic.util.ArrayUtils;

public class Config {
    public static final String MAIN_SERVER = "https://datmusic.xyz/";

    public static final String DOWNLOAD_FOLDER_NAME = "datmusic";

    public static String GCM_SENDER_ID = "635425098901";

    //Activity Tags
    public static String ACTIVITY_TAG_MAIN = "main";
    public static String ACTIVITY_TAG_PREFERENCES = "preferences";

    //Activity Extras
    public static final String EXTRA_QUERY = "tm.alashow.datmusic.QUERY";

    public static String DEFAULT_COUNT = "300";
    public static String DEFAULT_SORT = "2";

    public static Integer[] allowedBitrates = {64, 128, 192, 320};

    public static boolean isBitrateAllowed(int bitrate) {
        return ArrayUtils.contains(allowedBitrates, bitrate);
    }
}
