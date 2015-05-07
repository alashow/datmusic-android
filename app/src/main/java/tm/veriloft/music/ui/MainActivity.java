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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import tm.veriloft.music.Config;
import tm.veriloft.music.R;
import tm.veriloft.music.adapter.AudioListAdapter;
import tm.veriloft.music.model.Audio;
import tm.veriloft.music.util.MusicApiClient;
import tm.veriloft.music.util.U;
import tr.xip.errorview.ErrorView;


/**
 * Created by alashov on 21/03/15.
 */
public class MainActivity extends BaseActivity {

    private ArrayList<Audio> audioList = new ArrayList<>();
    private AudioListAdapter audioListAdapter;
    private SearchView mSearchView;
    private String oldQuery = "";

    @InjectView(R.id.listView) ListView mListView;
    @InjectView(R.id.progress) ProgressWheel progressBar;
    @InjectView(R.id.errorView) ErrorView errorView;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        ButterKnife.inject(this);

        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override public void onRetry() {
                U.hideView(errorView);
                search(oldQuery);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {

            }
        });

        //GCM Registration
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, Config.GCM_SENDER_ID);
        } else {
            RequestParams params = new RequestParams();
            params.put("reg_id", regId);
            MusicApiClient.get(Config.ENDPOINT_API + "reg_id.php", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess( int statusCode, Header[] headers, JSONObject response ) {
                    U.l(response.toString());
                }
            });
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
                    return true;
                }
            });
        }
        return true;
    }

    private void search( String query ) {
        clearList();//clearing old data
        oldQuery = query;
        RequestParams params = new RequestParams();

        params.put("q", query);
        params.put("access_token", Config.VK_CONFIG_ACCESS_TOKEN);
        params.put("autocomplete", Config.VK_CONFIG_AUTOCOMPLETE);
        params.put("sort", Config.VK_CONFIG_SORT);
        params.put("count", Config.VK_CONFIG_COUNT);

        MusicApiClient.get(Config.VK_AUDIO_SEARCH, params, new JsonHttpResponseHandler() {
            @Override public void onStart() {
                U.hideView(mListView);
                U.hideView(errorView);
                U.showView(progressBar);
            }

            @Override
            public void onSuccess( int statusCode, Header[] headers, JSONObject response ) {
                try {
                    if (response.has("error")) { //if we have error
                        //Parsing errors
                        JSONObject errorObject = response.getJSONObject("error");
                        int errorCode = errorObject.getInt("error_code");
                        //showing error
                        if (errorCode == 5) {
                            showError("token");
                        } else {
                            showError(response.getString("error_msg"));
                        }
                        return;
                    }
                    JSONArray audios = response.getJSONArray("response");
                    if (audios.length() >= 2) {
                        for(int i = 1; i < audios.length(); i++)
                            audioList.add(new Audio((JSONObject) audios.get(i)));
                        audioListAdapter = new AudioListAdapter(MainActivity.this, audioList);
                        mListView.setAdapter(audioListAdapter);
                        mListView.setFastScrollEnabled(audioList.size() > 10);
                    } else showError("notFound");
                } catch (Exception e) {
                    U.showCenteredToast(MainActivity.this, R.string.exception);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, String responseString, Throwable throwable ) {
                showError("network");
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse ) {
                showError("network");
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse ) {
                showError("network");
            }

            @Override public void onFinish() {
                U.showView(mListView);
                U.hideView(progressBar);
            }

            @Override public void onProgress( int bytesWritten, int totalSize ) {

            }
        });
    }

    private void showError( String error ) {
        U.showView(errorView);
        if (error.equals("network")) {
            errorView.setSubtitle(R.string.network_error);
        } else if (error.equals("token")) {
            errorView.setSubtitle(R.string.error_token);
        } else if (error.equals("notFound")) {
            errorView.setSubtitle(R.string.error_not_found);
        } else {
            errorView.setSubtitle(getString(R.string.error) + ": " + error);
        }
    }

    private void clearList() {
        mListView.setAdapter(null);
        audioList.clear();
        mListView.setFastScrollEnabled(false);
    }

    @Override protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override protected Boolean isChildActivity() {
        return false;
    }

    @Override protected String getActivityTag() {
        return Config.ACTIVITY_TAG_MAIN;
    }
}
