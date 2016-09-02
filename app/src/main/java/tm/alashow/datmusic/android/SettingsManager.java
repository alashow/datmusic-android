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

package tm.alashow.datmusic.android;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by alashov on 20/12/14.
 */
public class SettingsManager {

    public static final String PREFERENCES_NAME = "preferences";
    public static final String KEY_VK_TOKEN = "vkToken";

    private static SettingsManager instance = null;
    private Context mContext;

    /**
     * @param context App Context
     */
    protected SettingsManager(Context context) {
        mContext = context;
    }

    // Lazy Initialization (If required then only)
    public static SettingsManager getInstance(Context context) {
        if (context == null) {
            throw new IllegalStateException("Context must not be null");
        }
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

    public void setVkToken(String token) {
        getPreferences().edit().putString(KEY_VK_TOKEN, token).commit();
    }

    /**
     * @return {@link SharedPreferences}
     */
    public SharedPreferences getPreferences() {
        if (this.mContext != null) {
            return this.mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        } else {
            return null;
        }
    }
}
