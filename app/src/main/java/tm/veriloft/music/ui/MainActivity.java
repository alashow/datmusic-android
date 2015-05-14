/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.ui;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import tm.veriloft.music.Config;
import tm.veriloft.music.R;
import tm.veriloft.music.adapter.AudioListAdapter;
import tm.veriloft.music.model.Audio;
import tm.veriloft.music.util.AudioWife;
import tm.veriloft.music.util.MusicApiClient;
import tm.veriloft.music.util.U;
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
    private boolean CONFIG_POPULAR_START;
    private String CONFIG_SORT;
    private String CONFIG_COUNT;
    private String CONFIG_PERFORMER_ONLY;

    @InjectView(R.id.refreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @InjectView(R.id.listView) ListView mListView;
    @InjectView(R.id.progress) ProgressWheel progressBar;
    @InjectView(R.id.errorView) ErrorView errorView;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        ButterKnife.inject(this);
        layoutInflater = LayoutInflater.from(this);

        U.setColorScheme(swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                search(oldQuery, true);
            }
        });
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override public void onRetry() {
                U.hideView(errorView);
                search(oldQuery);
            }
        });

        getConfig();

        if (CONFIG_POPULAR_START)
            search("");//empty query will return popular music

        //GCM Registration
        try {
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
        } catch (Exception e) {
        }
        updateToken();
    }

    @Override
    protected void onResume() {
        getConfig();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (mSearchView != null) {
            mSearchView.setQueryHint(getString(R.string.search_hint));

            if (!CONFIG_POPULAR_START){
                mSearchView.setFocusable(true);
                mSearchView.setIconified(false);
                mSearchView.requestFocusFromTouch();
            }

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
        search(query, false, - 1, null);
    }

    private void search( String query, boolean refresh ) {
        search(query, refresh, - 1, null);
    }

    private void search( String query, long captchaSid, String captchaKey ) {
        search(query, false, captchaSid, captchaKey);
    }

    private void search( String query, final boolean refresh, long captchaSid, String captchaKey ) {
        oldQuery = query;
        RequestParams params = new RequestParams();

        params.put("q", query);
        params.put("access_token", settingsManager.getVkToken());
        params.put("autocomplete", Config.VK_CONFIG_AUTOCOMPLETE);
        params.put("sort", CONFIG_SORT);
        params.put("count", CONFIG_COUNT);
        params.put("performer_only", CONFIG_PERFORMER_ONLY);

        if (captchaSid > 1) {
            params.put("captcha_sid", captchaSid);
            params.put("captcha_key", captchaKey);
        }

        //change search method to getPopular, if query empty. get popular music.
        String url = (TextUtils.getTrimmedLength(query) < 1) ? Config.VK_AUDIO_SEARCH.replaceAll("search", "getPopular") : Config.VK_AUDIO_SEARCH;

        MusicApiClient.get(url, params, new JsonHttpResponseHandler() {
            @Override public void onStart() {
                U.hideView(errorView);
                if (refresh) swipeRefreshLayout.setRefreshing(true);
                else {
                    U.showView(progressBar);
                    U.hideView(mListView);
                }
            }

            @Override
            public void onSuccess( int statusCode, Header[] headers, JSONObject response ) {
                try {
                    clearList();//clearing old data
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
                            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
                                final Audio audio = audioList.get(position);
                                final BottomSheet bottomSheet = new BottomSheet.Builder(MainActivity.this)
                                    .title(audio.getArtist() + " - " + audio.getTitle())
                                    .sheet(R.menu.audio_actions)
                                    .listener(new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick( DialogInterface dialog, int which ) {
                                            switch (which) {
                                                case R.id.download:
                                                    DownloadManager mgr = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                                    Uri downloadUri = Uri.parse(audio.getSrc());
                                                    DownloadManager.Request request = new DownloadManager.Request(downloadUri);

                                                    if (U.isAboveOfVersion(11))
                                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                                                        .setDestinationInExternalPublicDir("/AlashovMusic",
                                                            encodeFilename(audio.getArtist() + " - " + audio.getTitle()) + ".mp3");
                                                    mgr.enqueue(request);
                                                    break;
                                                case R.id.play:
                                                    playAudio(audio);
                                                    break;
                                                case R.id.copy:
                                                    String link = "http://alashov.com/music/download.php?audio_id=" + audio.getOwnerId() + "_" + audio.getId();
                                                    if (! U.isAboveOfVersion(11)) {
                                                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                        clipboard.setText(link);
                                                    } else {
                                                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                        android.content.ClipData clip = android.content.ClipData.newPlainText("Link", link);
                                                        clipboard.setPrimaryClip(clip);
                                                        U.showCenteredToast(MainActivity.this, R.string.audio_copied);
                                                    }
                                                    break;
                                                case R.id.share:
                                                    String shareText = "Heý! Şu aýdymy diňlemegi maslahat berýärin! \n http://alashov.com/music/download.php?audio_id=" + audio.getOwnerId() + "_" + audio.getId();
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
                                            @Override public void run() {
                                                try {
                                                    URLConnection ucon;
                                                    final URL uri = new URL(audio.getSrc());
                                                    ucon = uri.openConnection();
                                                    ucon.connect();
                                                    final long bytes = Long.parseLong(ucon.getHeaderField("content-length"));
                                                    runOnUiThread(new Runnable() {
                                                        @Override public void run() {
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

                        if (refresh) swipeRefreshLayout.setRefreshing(false);
                        else U.hideView(progressBar);
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
                if (refresh) swipeRefreshLayout.setRefreshing(false);
                else U.hideView(progressBar);
            }

            @Override public void onProgress( int bytesWritten, int totalSize ) {

            }
        });
    }

    /**
     * Replace illegal filename characters for android ? : " * | / \ < >
     *
     * @param string string to replace
     * @return replaced string
     */
    private String encodeFilename( String string ) {
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
    private void setSizeAndBitrate( BottomSheet bottomSheet, Audio audio ) {
        MenuItem menuItem = bottomSheet.getMenu().findItem(R.id.download);
        long bitrate = audio.getBytes() / audio.getDuration() / 120;
        menuItem.setTitle(menuItem.getTitle() + " (" + humanReadableByteCount(audio.getBytes(), false) + " ~ " + bitrate + " kbps)");
        bottomSheet.invalidate();
    }

    public static String humanReadableByteCount( long bytes, boolean si ) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Shows error by given error type
     *
     * @param error errors type or error message
     */
    private void showError( String error ) {
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
        MusicApiClient.get(Config.ENDPOINT_API + "get_token.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess( int statusCode, Header[] headers, JSONObject response ) {
                try {
                    settingsManager.setVkToken(response.getString("vkToken"));
                    settingsManager.setLastFmToken(response.getString("lastFmToken"));
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

    private void showCaptcha( final String captchaImage, final long captchaSid ) {
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
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.CaptchaDialogAnimation;
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow( DialogInterface dialog ) {
                Button submitButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button reloadButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick( View v ) {
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
                    @Override public void onClick( View v ) {
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

    private void getConfig(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        CONFIG_POPULAR_START = sharedPreferences.getBoolean("popularStart", true);
        CONFIG_COUNT = sharedPreferences.getString("searchCount", Config.VK_CONFIG_COUNT);
        CONFIG_SORT = sharedPreferences.getString("searchSort", Config.VK_CONFIG_SORT);
        CONFIG_PERFORMER_ONLY = sharedPreferences.getString("searchSort", "0");
    }

    @Override public void onConfigurationChanged( Configuration newConfig ) {
        getConfig();
        super.onConfigurationChanged(newConfig);
    }

    public void playAudio( final Audio audio ) {
        final LinearLayout rootView = new LinearLayout(this);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.Base_Theme_AppCompat_Light_Dialog);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(contextThemeWrapper);
        alertDialogBuilder.setView(rootView);
        alertDialogBuilder.setNegativeButton("Ýap", null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow( DialogInterface dialog ) {
                //change flat button color
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primary));
            }
        });
        //destroy mediaPlayer when dialog dismissed
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss( DialogInterface dialog ) {
                resetPlayer();
            }
        });

        new PrepareAudioTask(rootView, new OnPreparedListener() {
            @Override public void onPrepared( MediaPlayer mediaPlayer ) {
                mMediaPlayer = mediaPlayer;
                alertDialog.show();
                TextView nameView = (TextView) rootView.findViewById(R.id.name);
                if (nameView != null)
                    nameView.setText(audio.getArtist() + " - " + audio.getTitle());
            }

            @Override public void onError( Exception e ) {
                U.showCenteredToast(MainActivity.this, R.string.exception);
            }
        }).execute(Uri.parse(audio.getSrc()));
    }

    /**
     * Shows progress dialog while preparing mediaPlayer
     */
    public class PrepareAudioTask extends AsyncTask<Uri, Void, Void> {
        private ViewGroup rootView;
        private OnPreparedListener onPreparedListener;
        private ProgressDialog progressDialog;

        public PrepareAudioTask( ViewGroup rootView, OnPreparedListener onPreparedListener ) {
            this.rootView = rootView;
            this.onPreparedListener = onPreparedListener;
            progressDialog = U.createActionLoading(MainActivity.this);
        }

        @Override protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Void doInBackground( Uri... params ) {
            try {
                AudioWife.getInstance()
                    .init(MainActivity.this, params[0], new OnPreparedListener() {
                        @Override public void onPrepared( MediaPlayer mediaPlayer ) {
                            progressDialog.dismiss();
                            onPreparedListener.onPrepared(mediaPlayer);
                        }

                        @Override public void onError( Exception e ) {
                            onPreparedListener.onError(e);
                            progressDialog.dismiss();
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
         */
        void onPrepared( MediaPlayer mediaPlayer );

        /**
         * called when catch exception
         *
         * @param e exception
         */
        void onError( Exception e );
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
