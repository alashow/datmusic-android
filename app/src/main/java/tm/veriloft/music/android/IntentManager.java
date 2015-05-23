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
         * Building a new {@link IntentManager} instance.
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
