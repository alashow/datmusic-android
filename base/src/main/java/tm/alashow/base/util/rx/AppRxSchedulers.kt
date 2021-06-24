/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.rx

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

data class AppRxSchedulers(
    val database: Scheduler = Schedulers.single(),
    val io: Scheduler = Schedulers.io(),
    val network: Scheduler = Schedulers.io(),
    val main: Scheduler = AndroidSchedulers.mainThread()
)
