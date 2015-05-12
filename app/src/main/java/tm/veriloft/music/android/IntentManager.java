/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.IntentCompat;

import tm.veriloft.music.ui.MainActivity;
import tm.veriloft.music.ui.PreferencesActivity;

/**
 * Intent Manager for starting activities
 */
public class IntentManager {
    private Context mContext;

    private IntentManager( Context context ) {
        this.mContext = context;
    }

    public static IntentManager with( Context _context ) {
        return new Builder(_context).build();
    }

    public static class Builder {
        private final Context context;

        /**
         * Start building a new {@link IntentManager} instance.
         */
        public Builder( Context context ) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context;
        }

        /**
         * Create the {@link IntentManager} instance.
         */
        public IntentManager build() {
            Context context = this.context;
            return new IntentManager(context);
        }
    }

    public void openIntentWithClear( Intent intent ) {
        ComponentName cn = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
        mContext.startActivity(mainIntent);
        try {
            ((Activity) mContext).finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open main activity of app
     */
    public void main() {
        openIntentWithClear(new Intent(mContext, MainActivity.class));
    }

    public void preferences() {
        open(new Intent(mContext, PreferencesActivity.class));
    }


    /**
     * Starts given intent with overriding transition
     *
     * @param _intent which we want open
     */
    private void open( Intent _intent ) {
        mContext.startActivity(_intent);
    }
}
