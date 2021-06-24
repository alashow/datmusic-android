/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.extensions

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

fun <T> Observable<T>.asFlowable(strategy: BackpressureStrategy = BackpressureStrategy.LATEST): Flowable<T> {
    return toFlowable(strategy)
}

fun <T> Single<T>.asFlowable(): Flowable<T> = toFlowable()
