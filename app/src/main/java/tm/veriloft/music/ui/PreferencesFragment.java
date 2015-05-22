/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.ui;

import android.content.Intent;
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
                    }
                    Intent intent = new Intent(getActivity(), PreferencesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                }
            });
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig ) {
        super.onConfigurationChanged(newConfig);
    }
}
