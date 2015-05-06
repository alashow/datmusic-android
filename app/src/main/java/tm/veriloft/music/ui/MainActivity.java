/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.ui;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import tm.veriloft.music.Config;
import tm.veriloft.music.R;
import tm.veriloft.music.util.MusicApiClient;
import tm.veriloft.music.util.U;


/**
 * Created by alashov on 21/03/15.
 */
public class MainActivity extends BaseActivity {
    private SearchView mSearchView;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        if (settingsManager.getToken() != null) {
            GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);
            final String regId = GCMRegistrar.getRegistrationId(this);
            if (regId.equals("")) {
                GCMRegistrar.register(this, Config.GCM_SENDER_ID);
            } else {
                RequestParams params = new RequestParams();
                params.put("token", settingsManager.getToken());
                params.put("reg_id", regId);
                MusicApiClient.get(Config.ENDPOINT_API + "reg_id.php", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess( int statusCode, Header[] headers, JSONObject response ) {
                        U.l(response.toString());
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (mSearchView != null) {
            mSearchView.setQueryHint(getString(R.string.search_hint));
            mSearchView.setFocusable(true);
            mSearchView.setIconified(false);
            mSearchView.requestFocusFromTouch();
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit( String query ) {
                    mSearchView.clearFocus(); //Hide keyboard
                    search(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange( String query ) {
                    search(query);
                    return false;
                }
            });
        }
        return true;
    }

    private void search( String query ) {

    }

    @Override protected int getLayoutResourceId() {
        return R.layout.activity_fragment;
    }

    @Override protected Boolean isChildActivity() {
        return false;
    }

    @Override protected String getActivityTag() {
        return Config.ACTIVITY_TAG_MAIN;
    }
}
