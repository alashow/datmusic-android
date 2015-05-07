package tm.veriloft.music;/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

public class Config {
    public static final String SERVER = "http://alashov.com/";
    public static final String ENDPOINT_API = SERVER + "music/app/";
    public static final String VK_SERVER = "https://api.vk.com/";
    public static final String VK_AUDIO_SEARCH = VK_SERVER + "method/audio.search";
    
    public static String GCM_SENDER_ID = "635425098901";
    public static final boolean DEBUG = true;


    //Activity Tags
    public static String ACTIVITY_TAG_MAIN = "main";
    public static String ACTIVITY_TAG_LOGOUT = "logout";
    public static String ACTIVITY_TAG_SETTINGS = "settings";

    //Activity Extras
    public static final String EXTRA_NAME = "tm.veriloft.music.NAME";

    //VK Search Api Config
    public static String VK_CONFIG_ACCESS_TOKEN = "4d45c6ebef3b05a910071c948bb1374015c9e47ad953fba2f631d8bc1fca425a0a0bffcb4955d3af90c07";
    public static String VK_CONFIG_SORT = "2";
    public static String VK_CONFIG_AUTOCOMPLETE = "1";
    public static String VK_CONFIG_COUNT = "1000";

}
