/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.ui;

import android.os.Bundle;
import android.preference.Preference;

import tm.veriloft.music.BuildConfig;
import tm.veriloft.music.R;


/**
 * Created by alashov on 18/01/15.
 */
public class PreferencesFragment extends android.support.v4.preference.PreferenceFragment {

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference preference = findPreference("about");
        preference.setTitle("music-android v" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
    }
}
