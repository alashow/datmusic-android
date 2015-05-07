/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.gcm;

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

import tm.veriloft.music.Config;
import tm.veriloft.music.R;
import tm.veriloft.music.android.SettingsManager;
import tm.veriloft.music.ui.MainActivity;
import tm.veriloft.music.util.MusicApiClient;
import tm.veriloft.music.util.U;


public class PushNotificationsService extends GCMBaseIntentService {
    public PushNotificationsService() {
        super(Config.GCM_SENDER_ID);
    }

    @Override
    protected void onRegistered( final Context context, String registrationId ) {
        RequestParams params = new RequestParams();
        params.put("reg_id", registrationId);
        MusicApiClient.get(Config.ENDPOINT_API + "reg_id.php", params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure( int statusCode, Header[] headers, String responseString, Throwable throwable ) {
                U.showCenteredToast(getBaseContext(), R.string.network_error);
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse ) {
                U.showCenteredToast(getBaseContext(), R.string.network_error);
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse ) {
                U.showCenteredToast(getBaseContext(), R.string.network_error);
            }

            @Override
            public void onSuccess( int statusCode, Header[] headers, JSONObject response ) {
                U.l(response.toString());
            }
        });
    }

    @Override
    protected void onUnregistered( Context context, String registrationId ) {
    }

    @Override
    protected void onMessage( Context context, Intent intent ) {
        try {
            final SettingsManager settingsManager = SettingsManager.getInstance(context);
            String type = intent.getStringExtra("type");
            String value = intent.getStringExtra("value");
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            if (type.equals("text") || type.equals("url")) {

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notificationIntent = null;

                if (type.equals("text")) {
                    notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.setAction(type + "_" + value);
                } else if (type.equals("url"))
                    notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(value));

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).
                    setContentTitle(title).
                    setContentText(message).
                    setStyle(new NotificationCompat.BigTextStyle().bigText(message)).
                    setSmallIcon(R.mipmap.ic_launcher).
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
    protected void onDeletedMessages( Context context, int total ) {
    }

    @Override
    public void onError( Context context, String errorId ) {

    }

    @Override
    protected boolean onRecoverableError( Context context, String errorId ) {
        return super.onRecoverableError(context, errorId);
    }
}
