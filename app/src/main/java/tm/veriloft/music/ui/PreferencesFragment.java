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

package tm.veriloft.music.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;

import java.util.Locale;

import tm.veriloft.music.BuildConfig;
import tm.veriloft.music.R;


/**
 * Created by alashov on 18/01/15.
 */
public class PreferencesFragment extends android.support.v4.preference.PreferenceFragment {

    private Locale locale = null;

    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override public void onViewCreated( View view, Bundle savedInstanceState ) {

        Preference preference = findPreference("about");
        if (preference != null)
            preference.setTitle("music-android v" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

        Preference language = findPreference("language");
        if (language != null) {
            language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange( Preference preference, Object newValue ) {
                    Configuration config = getActivity().getBaseContext().getResources().getConfiguration();
                    String lang = (String) newValue;

                    if (! lang.equals("") && ! config.locale.getLanguage().equals(lang)) {
                        locale = new Locale(lang);
                        Locale.setDefault(locale);
                        config.locale = locale;
                        getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());
                        getActivity().onConfigurationChanged(config);
                    }
                    return true;
                }
            });
        }
        super.onViewCreated(view, savedInstanceState);
    }
}
