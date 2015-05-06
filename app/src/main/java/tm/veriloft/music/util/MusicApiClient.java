/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import tm.veriloft.music.android.ApplicationLoader;

public class MusicApiClient {
    private static AsyncHttpClient client = new AsyncHttpClient();


    public static void get( String url, RequestParams params, AsyncHttpResponseHandler responseHandler ) {
        client.setTimeout(400 * 1000);
        U.l("MusicApiClient", "Http request to : " + url);
        client.get(ApplicationLoader.applicationContext, url, params, responseHandler);
    }

    public static void post( String url, RequestParams params, AsyncHttpResponseHandler responseHandler ) {
        client.setTimeout(40 * 1000);
        U.l("MusicApiClient", "Http request to : " + url);
        client.post(ApplicationLoader.applicationContext, url, params, responseHandler);
    }

    public static void cancelRequests() {
        client.cancelRequests(ApplicationLoader.applicationContext, true);
    }
}
