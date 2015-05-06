/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.veriloft.music.android;

/**
 * Created by alashov on 16/01/15.
 */

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;

public class ApplicationLoader extends Application {
    public static volatile Context applicationContext = null;
    public static volatile Handler applicationHandler = null;
    private static volatile boolean applicationInited = false;

    public static void postInitApplication() {
        if (applicationInited) {
            return;
        }
        applicationInited = true;
        ApplicationLoader app = (ApplicationLoader) ApplicationLoader.applicationContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        postInitApplication();
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig ) {
        super.onConfigurationChanged(newConfig);
    }
}

