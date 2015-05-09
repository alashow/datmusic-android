/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.android;

import android.content.Context;
import android.content.SharedPreferences;

import tm.veriloft.music.Config;

/**
 * Created by alashov on 20/12/14.
 */
public class SettingsManager {

    public static final String PREFERENCES_NAME = "preferences";
    public static final String KEY_VK_TOKEN = "vkToken";
    public static final String KEY_LASTFM_TOKEN = "lastFmToken";

    private static SettingsManager instance = null;
    private Context mContext;

    /**
     * @param _context App Context
     */
    protected SettingsManager( Context _context ) {
        mContext = _context;
    }

    // Lazy Initialization (If required then only)
    public static SettingsManager getInstance( Context context ) {
        if (context == null) throw new IllegalStateException("Context must not be null");
        if (instance == null) {
            // Thread Safe. Might be costly operation in some case
            synchronized (SettingsManager.class) {
                if (instance == null) {
                    instance = new SettingsManager(context);
                }
            }
        }
        return instance;
    }

    public static SettingsManager getInstance() {
        return getInstance(ApplicationLoader.applicationContext);
    }


    public String getVkToken() {
        return getPreferences().getString(KEY_VK_TOKEN, Config.VK_CONFIG_ACCESS_TOKEN);
    }

    public void setVkToken( String token ) {
        getPreferences().edit().putString(KEY_VK_TOKEN, token).commit();
    }

    public String getLastFmToken() {
        return getPreferences().getString(KEY_LASTFM_TOKEN, Config.LASTFM_CONFIG_ACCESS_TOKEN);
    }

    public void setLastFmToken( String token ) {
        getPreferences().edit().putString(KEY_LASTFM_TOKEN, token).commit();
    }

    /**
     * @return {@link SharedPreferences}
     */
    public SharedPreferences getPreferences() {
        if (this.mContext != null)
            return this.mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        else
            return null;
    }

}
