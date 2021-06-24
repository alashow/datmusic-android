/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.Observable
import io.reactivex.Single
import org.reactivestreams.Publisher

fun <T> Publisher<T>.asLive(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)
fun <T> Observable<T>.asLive(): LiveData<T> = asFlowable().asLive()
fun <T> Single<T>.asLive(): LiveData<T> = asFlowable().asLive()
