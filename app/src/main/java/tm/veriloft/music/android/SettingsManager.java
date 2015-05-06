/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.android;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by alashov on 20/12/14.
 */
public class SettingsManager {

    public static final String PREFERENCES_NAME = "preferences";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_BALANCE = "balance";
    public static final String KEY_USER_PHONE = "user_phone";
    public static final String KEY_TMCELL_PHONE = "tmcell_phone";
    public static final String KEY_MTS_PHONE = "mts_phone";
    public static final String KEY_FIRST_PAYMENT = "first_purchase";

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

    public int getUserId() {
        return getPreferences().getInt(KEY_USER_ID, - 1);
    }

    public void setUserId( int userId ) {
        getPreferences().edit().putInt(KEY_USER_ID, userId).commit();
    }

    public String getToken() {
        return getPreferences().getString(KEY_TOKEN, "");
    }

    public void setToken( String token ) {
        getPreferences().edit().putString(KEY_TOKEN, token).commit();
    }


    public String getBalance() {
        return getPreferences().getString(KEY_BALANCE, "0.0");
    }

    public void setBalance( String balance ) {
        getPreferences().edit().putString(KEY_BALANCE, balance).commit();
    }

    public String getUserPhone() {
        return getPreferences().getString(KEY_USER_PHONE, "");
    }

    public void setUserPhone( String userPhone ) {
        getPreferences().edit().putString(KEY_USER_PHONE, userPhone).commit();
    }

    public String getTmcellPhone() {
        return getPreferences().getString(KEY_TMCELL_PHONE, "+99365807127");//Setting default phone my phone :P
    }

    public void setTmcellPhone( String tmcellPhone ) {
        getPreferences().edit().putString(KEY_TMCELL_PHONE, tmcellPhone).commit();
    }

    public String getMtsPhone() {
        return getPreferences().getString(KEY_MTS_PHONE, "+99366807127");//Setting default phone my phone :P
    }

    public void setMtsPhone( String mtsPhone) {
        getPreferences().edit().putString(KEY_MTS_PHONE, mtsPhone).commit();
    }

    public boolean isFirstPayment() {
        return getPreferences().getBoolean(KEY_FIRST_PAYMENT, true);
    }

    public void setfirstPayment( boolean firstPayment ) {
        getPreferences().edit().putBoolean(KEY_FIRST_PAYMENT, firstPayment).commit();
    }

    public void resetSettings(){
        setToken("");
        setUserId(-1);
        setUserPhone("");
        setBalance("0.0");
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
