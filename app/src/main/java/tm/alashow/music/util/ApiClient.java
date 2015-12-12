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

package tm.alashow.music.util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import tm.alashow.music.App;

public class ApiClient {
    private static AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);


    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(400 * 1000);
        U.l("MusicApiClient", "Http request to : " + url);
        client.get(App.applicationContext, url, params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(40 * 1000);
        U.l("MusicApiClient", "Http request to : " + url);
        client.post(App.applicationContext, url, params, responseHandler);
    }

    public static void cancelRequests() {
        client.cancelRequests(App.applicationContext, true);
    }
}
