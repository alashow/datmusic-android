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

package tm.alashow.music.android.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.android.gcm.GCMBaseIntentService;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import tm.alashow.music.Config;
import tm.alashow.music.R;
import tm.alashow.music.ui.activity.MainActivity;
import tm.alashow.music.util.ApiClient;
import tm.alashow.music.util.U;


public class PushNotificationsService extends GCMBaseIntentService {
    public PushNotificationsService() {
        super(Config.GCM_SENDER_ID);
    }

    @Override
    protected void onRegistered(final Context context, String registrationId) {
        RequestParams params = new RequestParams();
        params.put("reg_id", registrationId);
        params.put("id", U.getDeviceId(this));
        ApiClient.get(Config.ENDPOINT_API + "reg_id.php", params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                U.showCenteredToast(getBaseContext(), R.string.network_error);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                U.showCenteredToast(getBaseContext(), R.string.network_error);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                U.showCenteredToast(getBaseContext(), R.string.network_error);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                U.l(response.toString());
            }
        });
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        try {
            String type = intent.getStringExtra("type");
            String value = intent.getStringExtra("value");
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            U.l("Title = " + title);
            U.l("Message = " + message);
            U.l("Type = " + type);
            U.l("Value = " + value);

            if (type.equals("text") || type.equals("url") || type.equals("suggest")) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notificationIntent = null;

                if (type.equals("text")) {
                    notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.setAction(type + "_" + value);
                } else if (type.equals("suggest")) {
                    notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.putExtra(Config.EXTRA_QUERY, value);
                    notificationIntent.setAction(type + "_" + value.hashCode());
                } else if (type.equals("url")) {
                    notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(value));
                }

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).
                    setContentTitle(title).
                    setContentText(message).
                    setStyle(new NotificationCompat.BigTextStyle().bigText(message)).
                    setSmallIcon(R.drawable.ic_notification).
                    setColor(context.getResources().getColor(R.color.primary)).
                    setContentIntent(pendingIntent).setAutoCancel(true);
                mBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND);
                notificationManager.notify(type.hashCode(), mBuilder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
    }

    @Override
    public void onError(Context context, String errorId) {

    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        return super.onRecoverableError(context, errorId);
    }
}
