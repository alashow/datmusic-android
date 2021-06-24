/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.calls

import javax.inject.Inject
import timber.log.Timber
import tm.alashow.base.util.LocalConfig
import tm.alashow.base.util.extensions.now

class DebounceRequest @Inject constructor(private val localConfig: LocalConfig) {

    private var maxCacheTime: Long = 1000

    private var key: String = ""
        set(key) {
            field = "last_requests_$key"
        }

    fun init(key: String, maxCacheTimeSeconds: Long = 10) {
        this.key = key
        this.maxCacheTime = maxCacheTimeSeconds * 1000
    }

    fun initDaily(key: String) = init(key, 24 * 60 * 60)
    fun initBiDaily(key: String) = init(key, 2 * 24 * 60 * 60)
    fun initWeekly(key: String) = init(key, 7 * 24 * 60 * 60)
    fun initBiWeekly(key: String) = init(key, 14 * 24 * 60 * 60)
    fun initMonthly(key: String) = init(key, 30 * 24 * 60 * 60)

    operator fun invoke(force: Boolean = false, onInvalid: () -> Unit = {}, onValid: () -> Unit) {
        if (force) return onValid()

        checkIfInitialized()
        val lastRequestTime = localConfig.getLong(key)
        val now = now()

        val diff = now - lastRequestTime
        when (now - lastRequestTime > maxCacheTime) {
            true -> {
                Timber.i("Allowing debounced request for '$key', seconds since last request: ${diff / 1000}")
                onValid()
            }
            else -> {
                Timber.i("Skipping debounced request for '$key', seconds since last request: ${diff / 1000}")
                onInvalid()
            }
        }
    }

    fun save() {
        Timber.i("Saving debounced request for '$key'")
        checkIfInitialized()
        localConfig.put(key, now())
    }

    fun clear() {
        Timber.i("Clearing debounced request for '$key'")
        checkIfInitialized()
        localConfig.remove(key)
    }

    private fun checkIfInitialized() {
        if (key.isBlank()) error("Called before initialization")
    }
}
