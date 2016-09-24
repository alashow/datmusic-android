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

package tm.alashow.datmusic.ui.activity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gcm.GCMRegistrar;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;
import com.tumblr.remember.Remember;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import butterknife.Bind;
import retrofit2.Call;
import retrofit2.Response;
import tm.alashow.datmusic.Config;
import tm.alashow.datmusic.R;
import tm.alashow.datmusic.android.IntentManager;
import tm.alashow.datmusic.interfaces.OnItemClickListener;
import tm.alashow.datmusic.interfaces.OnPreparedListener;
import tm.alashow.datmusic.model.Audio;
import tm.alashow.datmusic.model.Result;
import tm.alashow.datmusic.rest.ApiHelper;
import tm.alashow.datmusic.rest.ApiService;
import tm.alashow.datmusic.rest.ResponseHandler;
import tm.alashow.datmusic.rest.Summon;
import tm.alashow.datmusic.ui.adapter.AudioListAdapter;
import tm.alashow.datmusic.ui.view.EndlessRecyclerView;
import tm.alashow.datmusic.util.AudioWife;
import tm.alashow.datmusic.util.U;
import tr.xip.errorview.ErrorView;

/**
 * Created by alashov on 21/03/15.
 */
public class MainActivity extends BaseActivity implements EndlessRecyclerView.Pager, OnItemClickListener {

    private boolean isLoading = false;
    private boolean stopLoadMore = false;
    private int pagination = 0;

    private boolean showSearchView = true;

    private Handler mHandler;

    private ArrayList<Audio> audioArrayList = new ArrayList<>();
    private AudioListAdapter audioListAdapter;
    private MediaPlayer mMediaPlayer;
    private LayoutInflater layoutInflater;
    private SearchView mSearchView;
    private String oldQuery = "";

    //Preferences
    private int CONFIG_SORT;
    private int CONFIG_COUNT;
    private int CONFIG_PERFORMER_ONLY;

    @Bind(R.id.swipeRefreshLayout) SwipeRefreshLayout refreshLayout;
    @Bind(R.id.recyclerView) EndlessRecyclerView recyclerView;
    @Bind(R.id.fastScroller) RecyclerFastScroller fastScroller;
    @Bind(R.id.progress) ProgressWheel progressView;
    @Bind(R.id.errorView) ErrorView errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
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

        if (showSearchView) {
            showSearchView();
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        getConfig();
        super.onConfigurationChanged(newConfig);
    }

    private void init() {
        mHandler = new Handler();

        initViews();
        getConfig();

        registerGCM();

        initIntent();
    }

    private void initViews() {
        setTitle(R.string.app_name);

        layoutInflater = LayoutInflater.from(this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setProgressView(R.layout.view_list_loading);
        recyclerView.setPager(this);

        audioListAdapter = new AudioListAdapter(this, this);
        recyclerView.setAdapter(audioListAdapter);

        fastScroller.attachRecyclerView(recyclerView);

        U.setColorScheme(refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                search(new OnLoadListener(TYPE_REFRESH), oldQuery);
            }
        });
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                U.hideView(errorView);
                search(oldQuery);
            }
        });
    }

    private void initIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String queryExtra = getIntent().getExtras().getString(Config.EXTRA_QUERY);
            if (queryExtra != null && TextUtils.getTrimmedLength(queryExtra) > 1) {
                search(queryExtra);
                showSearchView();
            } else {
                searchWithRandomArtist();
                showSearchView();
            }
        } else {
            searchWithRandomArtist();
            showSearchView();
        }
    }

    /**
     * Sets searchview iconifed to false and sets oldQuery as searchQuery
     */
    private void showSearchView() {
        if (mSearchView != null && ! oldQuery.isEmpty()) {
            mSearchView.setIconified(false);
            mSearchView.setQuery(oldQuery, false);
            showSearchView = false;
        }
    }

    private void searchWithRandomArtist() {
        String[] artists = {
            "2 Cellos", "Agnes Obel", "Aloe Black", "Andrew Belle", "Angus Stone", "Aquilo", "Arctic Monkeys",
            "Avicii", "Balmorhea", "Barcelona", "Bastille", "Ben Howard", "Benj Heard", "Birdy", "Broods",
            "Calvin Harris", "Charlotte OC", "City of The Sun", "Civil Twilight", "Clint Mansell", "Coldplay",
            "Daft Punk", "Damien Rice", "Daniela Andrade", "Daughter", "David O'Dowda", "Dawn Golden", "Dirk Maassen",
            "Ed Sheeran", "Eminem", "Fabrizio Paterlini", "Fink", "Fleurie", "Florence and The Machine", "Gem club",
            "Glass Animals", "Greg Haines", "Greg Maroney", "Groen Land", "Halsey", "Hans Zimmer", "Hozier",
            "Imagine Dragons", "Ingrid Michaelson", "Jamie XX", "Jarryd James", "Jasmin Thompson", "Jaymes Young",
            "Jessie J", "Josef Salvat", "Julia Kent", "Kai Engel", "Keaton Henson", "Kendra Logozar", "Kina Grannis",
            "Kodaline", "Kygo", "Kyle Landry", "Lana Del Rey", "Lera Lynn", "Lights & Motion", "Linus Young", "Lo-Fang",
            "Lorde", "Ludovico Einaudi", "M83", "MONO", "MS MR", "Macklemore", "Mammals", "Maroon 5", "Martin Garrix",
            "Mattia Cupelli", "Max Richter", "Message To Bears", "Mogwai", "Mumford & Sons", "Nils Frahm", "ODESZA", "Oasis",
            "Of Monsters and Men", "Oh Wonder", "Philip Glass", "Phoebe Ryan", "Rachel Grimes", "Radiohead", "Ryan Keen",
            "Sam Smith", "Seinabo Sey", "Sia", "Takahiro Kido", "The Irrepressibles", "The Neighbourhood", "The xx",
            "VLNY", "Wye Oak", "X ambassadors", "Yann Tiersen", "Yiruma", "Young Summer", "Zack Hemsey", "Zinovia",
            "deadmau5", "pg.lost", "Ólafur Arnalds"};

        String artist = artists[new Random().nextInt(artists.length)];

        search(new OnLoadListener(TYPE_NEW), artist, true);
    }

    private void search(String query) {
        search(new OnLoadListener(TYPE_NEW), query, false, - 1, null);
    }

    private void search(OnLoadListener onLoadListener, String query) {
        search(onLoadListener, query, false, - 1, null);
    }

    private void search(OnLoadListener onLoadListener, String query, boolean performerOnly) {
        search(onLoadListener, query, performerOnly, - 1, null);
    }

    private void search(OnLoadListener onLoadListener, String query, long captchaSid, String captchaKey) {
        search(onLoadListener, query, false, captchaSid, captchaKey);
    }

    private void search(final OnLoadListener onLoadListener, String query, boolean performerOnly, long captchaSid, String captchaKey) {
        oldQuery = query;

        if (onLoadListener.getType() != TYPE_PAGINATION) {
            setPage(0);
        }

        Call<Result> resultCall = ApiService.getClientJackson().search(
            query,
            CONFIG_SORT,
            CONFIG_COUNT,
            (performerOnly) ? 1 : CONFIG_PERFORMER_ONLY,
            getPage() * CONFIG_COUNT
        );

        if (captchaSid > 1) {
            resultCall = ApiService.getClientJackson().search(
                query,
                CONFIG_SORT,
                CONFIG_COUNT,
                (performerOnly) ? 1 : CONFIG_PERFORMER_ONLY,
                getPage() * CONFIG_COUNT,
                captchaKey,
                captchaSid
            );
        }

        resultCall.enqueue(new Summon<Result>() {
            @Override
            public void onStart() {
                onLoadListener.onStart();
            }

            @Override
            public void onSuccess(Call<Result> call, Response<Result> response) {
                new ResponseHandler(MainActivity.this, this, call, response) {
                    @Override
                    public void onTokenError(Result result) {
                        showError("token");
                    }

                    @Override
                    public void onTooManyRequestsError(Result result) {
                        search(oldQuery);
                    }

                    @Override
                    public void onCaptchaError(Result result) {
                        showCaptcha(result.getError().getCaptchaImage(), result.getError().getCaptchaSid());
                        showError("captcha");
                    }

                    @Override
                    public void onNoResultError(Result result) {
                        onLoadListener.onEmptyResult();
                    }

                    @Override
                    public void onUnknownError(Result result) {
                        showError(result.getError().getMessage());
                    }

                    @Override
                    public void onSuccess(Result result) {
                        onLoadListener.onSuccess(result.getAudios());
                    }
                };
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                onLoadListener.onNetworkError(t);
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        final Audio audio = audioArrayList.get(position);
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

                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                            mgr.enqueue(request);
                            break;
                        case R.id.play:
                            playAudio(audio);
                            break;
                        case R.id.copy:
                            U.copyToClipboard(MainActivity.this, audio.getDownloadUrl());
                            break;
                        case R.id.share:
                            String shareText = getString(R.string.share_text) + audio.getDownloadUrl();
                            U.shareTextIntent(MainActivity.this, shareText);
                    }
                }
            }).show();

        //If file size already set, show it
        if (audio.getBytes() > 1) {
            setSizeAndBitrate(bottomSheet, audio);
        } else {
            ApiService.getClientScalars().getBytes(audio.getEncodedAudioId()).enqueue(new Summon<String>() {
                @Override
                public void onSuccess(Call<String> call, Response<String> response) {
                    if (ApiHelper.isSuccess(response)) {
                        long bytes = Long.parseLong(response.body());
                        audio.setBytes(bytes);
                        setSizeAndBitrate(bottomSheet, audio);
                    }
                }
            });
        }
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
        menuItem.setTitle(String.format(Locale.US, "%s (%s ~ %d kbps)", menuItem.getTitle(), U.humanReadableByteCount(audio.getBytes(), false), bitrate));
        bottomSheet.invalidate();
    }

    /**
     * Shows error by given error type
     *
     * @param error errors type or error message
     */
    private void showError(String error) {
        setPageStatus(2);
        switch (error) {
            case "network":
                errorView.setSubtitle(R.string.network_error);
                break;
            case "token":
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

    private void clearData() {
        setPage(0);
        stopLoadMore = false;
        audioArrayList.clear();
    }

    private void showCaptcha(final String captchaImage, final long captchaSid) {
        View rootView = layoutInflater.inflate(R.layout.layout_captcha, null);
        final ImageView captchaImageView = (ImageView) rootView.findViewById(R.id.captcha);
        final EditText captchaKeyView = (EditText) rootView.findViewById(R.id.key);

        Picasso.with(this)
            .load(captchaImage + "&v=" + System.currentTimeMillis())
            .placeholder(getResources().getColor(R.color.image_placeholder))
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
                            search(new OnLoadListener(TYPE_NEW), oldQuery, captchaSid, captchaKey);
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
                            .placeholder(getResources().getColor(R.color.image_placeholder))
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
        CONFIG_COUNT = Integer.parseInt(sharedPreferences.getString("searchCount", Config.DEFAULT_COUNT));
        CONFIG_SORT = Integer.parseInt(sharedPreferences.getString("searchSort", Config.DEFAULT_SORT));
        CONFIG_PERFORMER_ONLY = (sharedPreferences.getBoolean("performerOnly", false)) ? 1 : 0;
    }

    /**
     * @param status 0 loading, 1 loaded, 2 error
     */
    private void setPageStatus(int status) {
        switch (status) {
            case 0:
                U.showView(progressView);
                U.hideView(refreshLayout);
                U.hideView(errorView);
                break;
            case 1:
                U.showView(refreshLayout);
                U.hideView(progressView);
                U.hideView(errorView);

                refreshLayout.setRefreshing(false);
                break;
            case 2:
                U.showView(errorView);
                U.hideView(refreshLayout);
                U.hideView(progressView);

                refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public boolean shouldLoad() {
        return ! isLoading && ! stopLoadMore;
    }

    @Override
    public void loadNextPage() {
        search(new OnLoadListener(TYPE_PAGINATION), oldQuery);
    }

    private int getPage() {
        return pagination;
    }

    private void setPage(int page) {
        this.pagination = page;
    }

    private void incrementPage() {
        setPage(pagination + 1);
    }

    public class OnLoadListener {

        private int type;

        public OnLoadListener(int type) {
            this.type = type;
        }

        void onStart() {
            isLoading = true;
            switch (type) {
                case TYPE_NEW:
                    setPageStatus(0);
                    break;
                case TYPE_REFRESH:
                    refreshLayout.setRefreshing(true);
                    break;
                case TYPE_PAGINATION:
                    recyclerView.setRefreshing(true);
                    break;
            }
        }

        void onNetworkError(Throwable e) {
            e.printStackTrace();

            isLoading = false;

            if (type == TYPE_REFRESH && audioArrayList.isEmpty() || type == TYPE_NEW) {
                showError("network");
            }

            if (type == TYPE_PAGINATION) {
                stopLoadMore = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopLoadMore = false;
                    }
                }, 5 * 1000); //maybe after 5 seconds connection will available
            }
        }

        void onSuccess(ArrayList<Audio> newList) {
            //Clear if not paginating
            if (type != TYPE_PAGINATION) {
                clearData();
            }

            audioArrayList.addAll(newList);
            //TODO: refactor
            audioListAdapter.setList(audioArrayList);
            fastScroller.attachAdapter(audioListAdapter);

            isLoading = false;
            setPageStatus(1);
            incrementPage();
        }

        void onEmptyResult() {
            isLoading = false;
            if (type == TYPE_REFRESH && audioArrayList.isEmpty() || type == TYPE_NEW) {
                showError("notFound");
            }
            if (type == TYPE_PAGINATION) {
                stopLoadMore = true;
                recyclerView.setRefreshing(false);
            }
        }

        public int getType() {
            return type;
        }
    }

    /**
     * Registers GCM and sends to server
     */
    private void registerGCM() {
        try {
            GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);
            final String regId = GCMRegistrar.getRegistrationId(this);
            if (regId.equals("")) {
                GCMRegistrar.register(this, Config.GCM_SENDER_ID);
            } else {
                if (Remember.getBoolean("gcmSent", false)) {
                    ApiService.getClientJackson().register(
                        regId,
                        U.getDeviceId(this)
                    ).enqueue(new Summon<Result>() {
                        @Override
                        public void onSuccess(Call<Result> call, Response<Result> response) {
                            Remember.putBoolean("gcmSent", true);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Audio Player stuff

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        U.showCenteredToast(MainActivity.this, R.string.exception);
                    }
                });
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
