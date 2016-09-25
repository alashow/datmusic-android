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

package tm.alashow.datmusic.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import tm.alashow.datmusic.R;
import tm.alashow.datmusic.ui.activity.BaseActivity;

public class U {

    /**
     * Hide Given view, by setting visibility View.GONE
     *
     * @param view view for hide
     */
    public static void hideView(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * Hide view if it's visibility equals android.view.View.VISIBLE
     */
    public static void hideViewIfVisible(View view) {
        if (view != null) {
            if (view.getVisibility() == View.VISIBLE) {
                hideView(view);
            }
        }
    }

    /**
     * Show given view, by setting visibility View.VISIBLE
     *
     * @param view view for show
     */
    public static void showView(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hides if visible, show if hidden given view
     *
     * @param view view for toggle visibility
     */
    public static void toggleView(View view) {
        if (view != null) {
            if (view.getVisibility() == View.VISIBLE) {
                hideView(view);
            } else {
                showView(view);
            }
        }
    }

    /**
     * Attach fragment to custom container
     *
     * @param baseActivity BaseActivity activity of caller
     * @param newFragment  new Fragment
     */
    public static void attachFragment(BaseActivity baseActivity, Fragment newFragment) {
        baseActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, newFragment).commit();
    }

    /**
     * Constructing progress dialog for action loading
     *
     * @param context context
     * @return configured progress dialog
     */
    public static ProgressDialog createActionLoading(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.loading));
        return dialog;
    }

    /**
     * Setting colors to swiperefreshlayout
     *
     * @param swipeRefreshLayout swprl
     */
    public static void setColorScheme(SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.sea_green, R.color.yellow, R.color.red);
    }

    public static boolean isAboveOfVersion(int version) {
        return android.os.Build.VERSION.SDK_INT >= version;
    }

    /**
     * Copy given text to clipboard
     *
     * @param context context for access system services
     * @param string  text to copy
     */
    public static void copyToClipboard(Context context, String string) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Link", string);
            clipboard.setPrimaryClip(clip);
            U.showCenteredToast(context, R.string.audio_copied);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Share given text with intent
     *
     * @param activity activity
     * @param text     text to share
     */
    public static void shareTextIntent(Activity activity, String text) {
        String shareTitle = activity.getResources().getString(R.string.share);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(sharingIntent, shareTitle));
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
        return String.format(Locale.ENGLISH, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Replace illegal filename characters for android ? : " * | / \ < >
     *
     * @param string string to replace
     * @return replaced string
     */
    public static String sanitizeFilename(String string) {
        String[] illegalCharacters = {"\\x3F", "\\x3A", "\\x22", "\\x2A", "\\x7C", "\\x2F", "\\x5C", "\\x3C", "\\x3E", "\\~", "\\`", "\\!", "\\@", "\\#", "\\$", "\\%", "\\^", "\\|", "\\;", "\\:", "&#8216;", "&#8217;", "&#8220;", "&#8221;", "&#8211;", "&#8212;", "\\—", "\\–",};
        for(String s : illegalCharacters)
            string = string.replaceAll(s, " ");
        return string;
    }

    //from gist https://gist.github.com/alashow/07d9ef9c02ee697ab47d
    public static Character[] characters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
        'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
        'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'x', 'y', 'z', '1', '2', '3'};

    public static String encode(long input) {
        int length = characters.length;
        String encoded = "";

        if (input == 0) {
            return String.valueOf(characters[0]);
        } else if (input < 0) {
            input *= - 1;
            encoded += "-";
        }

        while (input > 0) {
            long val = input % length;
            input = input / length;
            encoded += characters[(int) val];
        }

        return encoded;
    }

    public static long decode(String encoded) {
        int length = characters.length;

        long decoded = 0;

        for(int i = encoded.length() - 1; i >= 0; i--) {
            char ch = encoded.charAt(i);
            long val = indexOf(ch, characters);
            decoded = (decoded * length) + val;
        }

        return decoded;
    }

    public static <T> int indexOf(T needle, T[] haystack) {
        for(int i = 0; i < haystack.length; i++) {
            if (haystack[i] != null && haystack[i].equals(needle)
                || needle == null && haystack[i] == null) {
                return i;
            }
        }

        return - 1;
    }
    //end from gist
}
