/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import tm.veriloft.music.Config;
import tm.veriloft.music.R;
import tm.veriloft.music.util.U;


/**
 * Created by alashov on 18/01/15.
 */
public class PreferencesActivity extends BaseActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        Fragment preferencesFragment = new PreferencesFragment();
        U.attachFragment(this, preferencesFragment);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected Boolean isChildActivity() {
        return true;
    }

    @Override
    protected String getActivityTag() {
        return Config.ACTIVITY_TAG_PREFERENCES;
    }
}
