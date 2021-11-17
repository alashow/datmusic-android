/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import tm.alashow.base.util.RemoteLogger
import tm.alashow.domain.models.*

private const val STORE_NAME = "app_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = STORE_NAME)

private val json = DEFAULT_JSON_FORMAT

class PreferencesStore @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun <T> remove(key: Preferences.Key<T>) {
        context.dataStore.edit { settings ->
            settings.remove(key)
        }
    }

    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    fun <T> get(key: Preferences.Key<T>, defaultValue: T): Flow<T> = context.dataStore.data
        .map { preferences -> preferences[key] ?: return@map defaultValue }

    fun <T> optional(key: Preferences.Key<T>): Flow<Optional<T>> = context.dataStore.data
        .map { preferences -> some(preferences[key]) }

    suspend fun <T> save(name: String, value: T, serializer: KSerializer<T>) {
        val key = stringPreferencesKey(name)
        save(key, json.encodeToString(serializer, value))
    }

    fun <T> optional(name: String, serializer: KSerializer<T>): Flow<Optional<T>> {
        val key = stringPreferencesKey(name)
        return optional(key).map {
            when (it) {
                is Optional.Some<String> ->
                    try {
                        some(json.decodeFromString(serializer, it.value))
                    } catch (e: SerializationException) {
                        RemoteLogger.exception(e)
                        None
                    }
                else -> Optional.None
            }
        }
    }

    fun <T> get(name: String, serializer: KSerializer<T>, defaultValue: T): Flow<T> {
        return optional(name, serializer).map {
            when (it) {
                is Optional.None -> defaultValue
                else -> it.value()
            }
        }
    }
}
