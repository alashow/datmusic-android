/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.util;

import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * Created by alashov on 12/05/15.
 */
public class URLSpanNoUnderline extends URLSpan {
    public URLSpanNoUnderline( String url ) {
        super(url);
    }

    @Override public void updateDrawState( TextPaint ds ) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}
