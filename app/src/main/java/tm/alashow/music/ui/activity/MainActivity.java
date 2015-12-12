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

package tm.alashow.music.ui.activity;


import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

import butterknife.Bind;
import tm.alashow.music.Config;
import tm.alashow.music.R;
import tm.alashow.music.android.IntentManager;
import tm.alashow.music.model.Audio;
import tm.alashow.music.ui.adapter.AudioListAdapter;
import tm.alashow.music.util.ApiClient;
import tm.alashow.music.util.AudioWife;
import tm.alashow.music.util.U;
import tr.xip.errorview.ErrorView;


/**
 * Created by alashov on 21/03/15.
 */
public class MainActivity extends BaseActivity {

    private ArrayList<Audio> audioList = new ArrayList<>();
    private AudioListAdapter audioListAdapter;
    private MediaPlayer mMediaPlayer;
    private LayoutInflater layoutInflater;
    private SearchView mSearchView;
    private String oldQuery = "";

    //Preferences
    private String CONFIG_SORT;
    private String CONFIG_COUNT;
    private int CONFIG_PERFORMER_ONLY;

    @Bind(R.id.refreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.listView) ListView mListView;
    @Bind(R.id.progress) ProgressWheel progressBar;
    @Bind(R.id.errorView) ErrorView errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutInflater = LayoutInflater.from(this);

        setTitle(R.string.app_name);

        U.setColorScheme(swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                search(oldQuery, true);
            }
        });
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                U.hideView(errorView);
                search(oldQuery);
            }
        });

        getConfig();

        //GCM Registration
        try {
            GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);
            final String regId = GCMRegistrar.getRegistrationId(this);
            if (regId.equals("")) {
                GCMRegistrar.register(this, Config.GCM_SENDER_ID);
            } else {
                U.l("RegId = " + regId);
                U.l("AndroidId = " + U.getDeviceId(this));

                RequestParams params = new RequestParams();
                params.put("reg_id", regId);
                params.put("id", U.getDeviceId(this));
                ApiClient.get(Config.ENDPOINT_API + "reg_id.php", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        U.l(response.toString());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateToken();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String queryExtra = getIntent().getExtras().getString(Config.EXTRA_QUERY);
            if (queryExtra != null && TextUtils.getTrimmedLength(queryExtra) > 1) {
                search(queryExtra);
                if (mSearchView != null) {
                    mSearchView.setIconified(false);
                    mSearchView.setQuery(queryExtra, false);
                }
            } else {
                searchWithRandomArtist();
            }
        } else {
            searchWithRandomArtist();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getConfig();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (mSearchView != null) {
            mSearchView.setQueryHint(getString(R.string.search_hint));

            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    mSearchView.clearFocus(); //Hide keyboard
                    search(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    return true;
                }
            });
        }
        return true;
    }

    private void searchWithRandomArtist() {
        String[] artists = {"Kygo", "Ed Sheeran", "Toe",
            "Coldplay", "The xx", "MS MR", "Macklemore",
            "Lorde", "Birdy", "Seinabo Sey", "Sia", "M83",
            "Hans Zimmer", "Keaton Henson", "Yiruma", "Martin Garrix",
            "Calvin Harris", "Zinovia", "Avicii", "Of Monsters and Men",
            "Josef Salvat", "Sam Smith", "deadmau5", "Yann Tiersen",
            "Jessie J", " Maroon 5", "X ambassadors", "Fink",
            "Young Summer", "Lana Del Rey", "Arctic Monkeys",
            "Ludovico Einaudi", "Lera Lynn", "Bastille",
            "Nils Frahm", "Ben Howard", "Andrew Belle",
            "Mumford & Sons", "Ryan Keen", "Zes", "Greg Haines",
            "Max Richter"};

        String artist = artists[new Random().nextInt(artists.length)];

        search(artist);
    }

    private void search(String query) {
        search(query, false, - 1, null, false);
    }

    private void search(String query, boolean refresh) {
        search(query, refresh, - 1, null, false);
    }

    private void search(String query, long captchaSid, String captchaKey) {
        search(query, false, captchaSid, captchaKey, false);
    }

    private void search(String query, final boolean refresh, long captchaSid, String captchaKey, boolean performerOnly) {
        oldQuery = query;
        RequestParams params = new RequestParams();

        U.l("Search function, searching with query = " + query);
        params.put("q", query);
        params.put("access_token", settingsManager.getVkToken());
        params.put("autocomplete", Config.VK_CONFIG_AUTOCOMPLETE);
        params.put("sort", CONFIG_SORT);
        params.put("count", CONFIG_COUNT);

        params.put("performer_only", (performerOnly) ? 1 : CONFIG_PERFORMER_ONLY);

        if (captchaSid > 1) {
            params.put("captcha_sid", captchaSid);
            params.put("captcha_key", captchaKey);
        }

        //change search method to getPopular, if query empty. get popular music.

        ApiClient.get(Config.SEARCH, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                U.hideView(errorView);
                if (refresh) {
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    U.showView(progressBar);
                    U.hideView(mListView);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    clearList();//clear old data
                    if (response.has("error")) { //if we have error
                        //Parsing errors
                        JSONObject errorObject = response.getJSONObject("error");
                        int errorCode = errorObject.getInt("error_code");
                        //showing error
                        switch (errorCode) {
                            case 5:
                                showError("token");
                                break;
                            case 6: // "Too many requests per second" error, retry
                                search(oldQuery);
                                break;
                            case 14:
                                showCaptcha(errorObject.getString("captcha_img"), errorObject.getLong("captcha_sid"));
                                showError("captcha");
                                break;
                            default:
                                showError(errorObject.getString("error_msg"));
                                break;
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

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                final Audio audio = audioList.get(position);
                                final BottomSheet bottomSheet = new BottomSheet.Builder(MainActivity.this)
                                    .title(audio.getArtist() + " - " + audio.getTitle())
                                    .sheet(R.menu.audio_actions)
                                    .listener(new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case R.id.download:
                                                    DownloadManager mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                                    Uri downloadUri = Uri.parse(audio.getDownloadUrl());
                                                    DownloadManager.Request request = new DownloadManager.Request(downloadUri);

                                                    if (U.isAboveOfVersion(11)) {
                                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                    }

                                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                                                        .setDestinationInExternalPublicDir(Config.DOWNLOAD_FOLDER,
                                                            encodeFilename(audio.getArtist() + " - " + audio.getTitle()) + ".mp3");
                                                    mgr.enqueue(request);
                                                    break;
                                                case R.id.play:
                                                    playAudio(audio);
                                                    break;
                                                case R.id.copy:
                                                    if (! U.isAboveOfVersion(11)) {
                                                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                                        clipboard.setText(audio.getDownloadUrl());
                                                    } else {
                                                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                                        android.content.ClipData clip = android.content.ClipData.newPlainText("Link", audio.getDownloadUrl());
                                                        clipboard.setPrimaryClip(clip);
                                                        U.showCenteredToast(MainActivity.this, R.string.audio_copied);
                                                    }
                                                    break;
                                                case R.id.share:
                                                    String shareText = getString(R.string.share_text) + audio.getDownloadUrl();
                                                    ;
                                                    Intent sendIntent = new Intent();
                                                    sendIntent.setAction(Intent.ACTION_SEND);
                                                    sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                                                    sendIntent.setType("text/plain");
                                                    startActivity(sendIntent);
                                            }
                                        }
                                    }).show();

                                //If file size already set, show it
                                if (audio.getBytes() > 1) {
                                    setSizeAndBitrate(bottomSheet, audio);
                                } else {
                                    try {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    URLConnection ucon;
                                                    final URL uri = new URL(audio.getDownloadUrl());
                                                    ucon = uri.openConnection();
                                                    ucon.connect();
                                                    final long bytes = Long.parseLong(ucon.getHeaderField("content-length"));
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            audio.setBytes(bytes);
                                                            setSizeAndBitrate(bottomSheet, audio);
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                    } catch (final Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        if (refresh) {
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            U.hideView(progressBar);
                        }
                    } else {
                        showError("notFound");
                    }
                } catch (Exception e) {
                    U.showCenteredToast(MainActivity.this, R.string.exception);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError("network");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError("network");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                showError("network");
            }

            @Override
            public void onFinish() {
                U.showView(mListView);
                if (refresh) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    U.hideView(progressBar);
                }
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {

            }
        });
    }

    /**
     * Replace illegal filename characters for android ? : " * | / \ < >
     *
     * @param string string to replace
     * @return replaced string
     */
    private String encodeFilename(String string) {
        String[] illegalCharacters = {"\\x3F", "\\x3A", "\\x22", "\\x2A", "\\x7C", "\\x2F", "\\x5C", "\\x3C", "\\x3E"};
        for(String s : illegalCharacters)
            string = string.replaceAll(s, " ");
        return string;
    }

    /**
     * Set file size and audio bitrate to download menu
     *
     * @param bottomSheet menu where located download button
     * @param audio       file for get info
     */
    private void setSizeAndBitrate(BottomSheet bottomSheet, Audio audio) {
        MenuItem menuItem = bottomSheet.getMenu().findItem(R.id.download);
        long bitrate = audio.getBytes() / audio.getDuration() / 120;
        menuItem.setTitle(menuItem.getTitle() + " (" + U.humanReadableByteCount(audio.getBytes(), false) + " ~ " + bitrate + " kbps)");
        bottomSheet.invalidate();
    }

    /**
     * Shows error by given error type
     *
     * @param error errors type or error message
     */
    private void showError(String error) {
        U.showView(errorView);
        switch (error) {
            case "network":
                errorView.setSubtitle(R.string.network_error);
                break;
            case "token":
                updateToken();//updating tokens quickly
                errorView.setSubtitle(R.string.error_token);
                break;
            case "notFound":
                errorView.setSubtitle(R.string.error_not_found);
                break;
            case "captcha":
                errorView.setSubtitle(R.string.error_captcha);
                break;
            default:
                errorView.setSubtitle(getString(R.string.error) + ": " + error);
                break;
        }
    }

    /**
     * Getting tokens from server and storing them to shared preferences
     */
    private void updateToken() {
        ApiClient.get(Config.ENDPOINT_API + "get_token.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    settingsManager.setVkToken(response.getString("vkToken"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Clear array and listview
     */
    private void clearList() {
        mListView.setAdapter(null);
        audioList.clear();
        mListView.setFastScrollEnabled(false);
    }

    /**
     * stop playing audio
     */
    private void resetPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void showCaptcha(final String captchaImage, final long captchaSid) {
        View rootView = layoutInflater.inflate(R.layout.layout_captcha, null);
        final ImageView captchaImageView = (ImageView) rootView.findViewById(R.id.captcha);
        final EditText captchaKeyView = (EditText) rootView.findViewById(R.id.key);

        Picasso.with(this)
            .load(captchaImage + "&v=" + System.currentTimeMillis())
            .placeholder(U.imagePlaceholder())
            .into(captchaImageView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.captcha_submit, null);
        builder.setNegativeButton(R.string.captcha_reload, null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button submitButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button reloadButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String captchaKey = captchaKeyView.getText().toString();
                        if (TextUtils.getTrimmedLength(captchaKey) >= 1) {
                            search(oldQuery, captchaSid, captchaKey);
                            alertDialog.dismiss();
                        } else {
                            U.showCenteredToast(MainActivity.this, R.string.captcha_empty);
                        }
                    }
                });

                reloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(MainActivity.this)
                            .load(captchaImage + "&v=" + System.currentTimeMillis())
                            .placeholder(U.imagePlaceholder())
                            .into(captchaImageView);
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void getConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("languageChanged", false)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("languageChanged", false).apply();
            IntentManager.with(this).main();
            finish();
        }
        CONFIG_COUNT = sharedPreferences.getString("searchCount", Config.VK_CONFIG_COUNT);
        CONFIG_SORT = sharedPreferences.getString("searchSort", Config.VK_CONFIG_SORT);
        CONFIG_PERFORMER_ONLY = (sharedPreferences.getBoolean("performerOnly", false)) ? 1 : 0;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        getConfig();
        super.onConfigurationChanged(newConfig);
    }

    public void playAudio(final Audio audio) {
        final LinearLayout rootView = new LinearLayout(this);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.Base_Theme_AppCompat_Light_Dialog);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(contextThemeWrapper);
        alertDialogBuilder.setView(rootView);
        alertDialogBuilder.setNegativeButton(R.string.audio_player_close, null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //change flat button color
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primary));
            }
        });
        //destroy mediaPlayer when dialog dismissed
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                resetPlayer();
            }
        });

        new PrepareAudioTask(rootView, new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer, AudioWife audioWife) {
                mMediaPlayer = mediaPlayer;
                alertDialog.show();
                TextView nameView = (TextView) rootView.findViewById(R.id.name);
                if (nameView != null) {
                    nameView.setText(audio.getArtist() + " - " + audio.getTitle());
                }
                audioWife.play();
            }

            @Override
            public void onError(Exception e) {
                U.showCenteredToast(MainActivity.this, R.string.exception);
            }
        }).execute(Uri.parse(audio.getStreamUrl()));
    }

    /**
     * Shows progress dialog while preparing mediaPlayer
     */
    public class PrepareAudioTask extends AsyncTask<Uri, Void, Void> {
        private AudioWife audioWife;
        private ViewGroup rootView;
        private OnPreparedListener onPreparedListener;
        private ProgressDialog progressDialog;
        private boolean cancelled = false;

        public PrepareAudioTask(ViewGroup rootView, OnPreparedListener onPreparedListener) {
            this.rootView = rootView;
            this.onPreparedListener = onPreparedListener;
            progressDialog = U.createActionLoading(MainActivity.this);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.audio_player_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelled = true;
                    progressDialog.dismiss();
                    if (audioWife != null) {
                        audioWife.release();
                    }
                }
            });
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Uri... params) {
            try {
                audioWife = AudioWife.getInstance()
                    .init(MainActivity.this, params[0], new OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer, AudioWife audioWife) {
                            if (! cancelled) {
                                progressDialog.dismiss();
                                onPreparedListener.onPrepared(mediaPlayer, audioWife);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            if (! cancelled) {
                                onPreparedListener.onError(e);
                                progressDialog.dismiss();
                            }
                        }
                    }).useDefaultUi(rootView, layoutInflater);
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
                onPreparedListener.onError(e);
            }
            return null;
        }
    }

    public interface OnPreparedListener {
        /**
         * called when audio prepared
         *
         * @param mediaPlayer mediaPlayer
         * @param audioWife   instance
         */
        void onPrepared(MediaPlayer mediaPlayer, AudioWife audioWife);

        /**
         * called when catch exception
         *
         * @param e exception
         */
        void onError(Exception e);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected Boolean isChildActivity() {
        return false;
    }

    @Override
    protected String getActivityTag() {
        return Config.ACTIVITY_TAG_MAIN;
    }
}
