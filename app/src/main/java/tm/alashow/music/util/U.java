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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import tm.alashow.music.Config;
import tm.alashow.music.R;
import tm.alashow.music.android.ApplicationLoader;
import tm.alashow.music.ui.activity.BaseActivity;

public class U {

    private static final Pattern PATTERN = Pattern.compile("[^A-Za-z0-9_]");
    private static final int MAX_FILENAME_LENGTH = 127;

    /**
     * Hide Given view, by setting visibility View.GONE
     *
     * @param _view view for hide
     */
    public static void hideView(View _view) {
        if (_view != null) {
            _view.setVisibility(View.GONE);
        }
    }

    /**
     * Hide view if it's visibility equals android.view.View.VISIBLE
     */
    public static void hideViewIfVisible(View _view) {
        if (_view != null) {
            if (_view.getVisibility() == View.VISIBLE) {
                hideView(_view);
            }
        }
    }

    /**
     * Show given view, by setting visibility View.VISIBLE
     *
     * @param _view view for show
     */
    public static void showView(View _view) {
        if (_view != null) {
            _view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hides if visible, show if hidden given view
     *
     * @param _view view for toggle visibility
     */
    public static void toggleView(View _view) {
        if (_view != null) {
            if (_view.getVisibility() == View.VISIBLE) {
                hideView(_view);
            } else {
                showView(_view);
            }
        }
    }

    /**
     * Shows aplication error alert dialog
     *
     * @param _activity activity of caller
     */
    public static void applicationError(Activity _activity) {
        try {
            if (_activity != null) {
                new AlertDialog.Builder(_activity).setMessage(_activity.getString(R.string.exception)).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Attach fragment to custom container
     *
     * @param _activity   BaseActivity activity of caller
     * @param newFragment new fragment
     */
    public static void attachFragment(ActionBarActivity _activity, Fragment newFragment) {
        if (_activity.findViewById(R.id.fragmentContainer) != null) {
            _activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, newFragment).commit();
        }
    }

    /**
     * Attach fragment to custom container
     *
     * @param _activity   BaseActivity activity of caller
     * @param newFragment new Fragment
     * @param _container  placeholder for fragment
     */
    public static void attachFragment(BaseActivity _activity, Fragment newFragment, int _container) {
        _activity.getSupportFragmentManager().beginTransaction().replace(_container, newFragment).commit();
    }

    /**
     * Quick  access to log
     *
     * @param message
     */
    public static void l(Object object, String message) {
        if (! Config.DEBUG) {
            return;
        }
        if (message != null && message.equals("")) {
            return;
        }
        Log.d("MusicApp / " + object.getClass().getSimpleName(), message);
    }

    public static void l(String tag, String message) {
        if (! Config.DEBUG) {
            return;
        }
        if (message != null && message.equals("")) {
            return;
        }
        Log.d("MusicApp / " + tag, message);
    }

    public static void l(String message) {
        if (! Config.DEBUG) {
            return;
        }
        if (message != null && message.equals("")) {
            return;
        }
        Log.d("MusicApp", message);
    }

    public static void l(Object object, int message) {
        l(object, "Integer = " + message);
    }

    public static void e(String message) {
        Log.e("Error", message);
    }

    public static String getRealPathFromURI(Activity context, Uri contentUri) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    /**
     * For picasso image loadings
     *
     * @return #f2f2f2 colored drawable
     */
    public static Drawable imagePlaceholder() {
        return new ColorDrawable(ApplicationLoader.applicationContext.getResources().getColor(R.color.image_placeholder));
    }

    /**
     * Constructing progress dialog for action loading
     *
     * @param _context context
     * @return configured progress dialog
     */
    public static ProgressDialog createActionLoading(Context _context) {
        ProgressDialog dialog = new ProgressDialog(_context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage(_context.getString(R.string.loading));
        return dialog;
    }

    /**
     * Setting colors to swiperefreshlayout
     *
     * @param _s swprl
     */
    public static void setColorScheme(SwipeRefreshLayout _s) {
        _s.setColorSchemeResources(R.color.primary, R.color.sea_green, R.color.yellow, R.color.red);
    }

    public static boolean isAboveOfVersion(int version) {
        return android.os.Build.VERSION.SDK_INT >= version;
    }

    public static void showCenteredToast(Context context, String string) {
        if (context == null) {
            return;
        }
        if (string == null || string.equals("")) {
            string = "null";
        }
        Toast toast = Toast.makeText(context, string, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showCenteredToast(Context context, @StringRes int stringRes) {
        showCenteredToast(context, context.getString(stringRes));
    }

    public static void stripUnderlines(TextView textView) {
        Spannable s = (Spannable) textView.getText();
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for(URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
