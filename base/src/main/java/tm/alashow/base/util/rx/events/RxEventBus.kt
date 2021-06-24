/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.rx.events

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * For making basic event buses based on RxJava subjects.
 */
interface RxEventBus<T> {
    fun new(event: T)

    fun listen(): Observable<T>
}

/**
 * [RxEventBus] backed by [PublishSubject]. Just declare an object extending this with [T] type.
 */
open class RxEventBusPublish<T : Any> : RxEventBus<T> {
    private val events = PublishSubject.create<T>()

    override fun new(event: T) = events.onNext(event)
    override fun listen() = events
}
