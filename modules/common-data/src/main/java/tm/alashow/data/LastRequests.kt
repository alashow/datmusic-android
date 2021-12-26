/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data

import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import timber.log.Timber

class LastRequests(
    private val name: String,
    private val config: PreferencesStore,
    private val expiration: Duration = Duration.ofHours(24),
) {
    companion object {
        private const val defaultParams = "default"
        private const val keyPrefix = "last_requests_"

        suspend fun clearAll(config: PreferencesStore) {
            val preferences = config.getStore().data.first()
            preferences.asMap().forEach { (key, _) ->
                if (key.name.startsWith(keyPrefix)) {
                    config.remove(key)
                }
            }
        }
    }

    private fun getPreferenceKey(params: String) = longPreferencesKey("last_requests_${name}_$params")

    suspend fun isExpired(params: String = defaultParams): Boolean {
        val key = getPreferenceKey(params)

        val lastRequestTime = Instant.ofEpochMilli(config.get(key, 0).first())
        val now = Instant.now()

        val expired = lastRequestTime.plus(expiration).isBefore(now)
        Timber.i("Checking last requests expired for ${key.name}: expired=$expired, last=$lastRequestTime, now=$now")
        return expired
    }

    suspend fun save(params: String = defaultParams, instant: Instant = Instant.now()) {
        val key = getPreferenceKey(params)
        Timber.i("Saving last requests for ${key.name}: $instant")
        config.save(key, instant.toEpochMilli())
    }

    suspend fun clear(params: String = defaultParams) {
        val key = getPreferenceKey(params)
        Timber.i("Clearing last requests for ${key.name}")
        config.remove(key)
    }
}
