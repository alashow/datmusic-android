/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import timber.log.Timber
import tm.alashow.base.util.extensions.now
import tm.alashow.base.util.extensions.nowNano

typealias TimerMap = MutableMap<String, Long>

object CodeTimer {

    private val msMap: TimerMap = mutableMapOf()
    private val nanoMap: TimerMap = mutableMapOf()

    private fun start(map: TimerMap, tag: String, time: Long) {
        map[tag] = time
        Timber.d("timer.$tag.start = $time")
    }

    private fun end(map: TimerMap, tag: String, time: Long) {
        val started = map[tag]
        if (started != null) {
            Timber.d("timer.$tag.end, elapsed = ${time - started}")
        } else {
            Timber.w("timer.$tag ended at $time, but was not started.")
        }
    }

    fun start(tag: String) = start(msMap, tag, now())
    fun end(tag: String) = end(msMap, tag, now())

    fun startNano(tag: String) = start(nanoMap, tag, nowNano())
    fun endNano(tag: String) = end(nanoMap, tag, nowNano())
}
