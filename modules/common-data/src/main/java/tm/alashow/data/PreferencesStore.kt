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
import java.io.Serializable
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import timber.log.Timber
import tm.alashow.base.util.extensions.decodeAsBase64Object
import tm.alashow.base.util.extensions.encodeAsBase64String
import tm.alashow.domain.models.DEFAULT_JSON_FORMAT
import tm.alashow.domain.models.None
import tm.alashow.domain.models.Optional
import tm.alashow.domain.models.some

private const val STORE_NAME = "app_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = STORE_NAME)

private val json = DEFAULT_JSON_FORMAT

class PreferencesStore @Inject constructor(@ApplicationContext private val context: Context) {

    fun getStore() = context.dataStore

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

    suspend fun <T> save(keyName: String, value: T, serializer: KSerializer<T>) {
        val key = stringPreferencesKey(keyName)
        save(key, json.encodeToString(serializer, value))
    }

    fun <T> optional(keyName: String, serializer: KSerializer<T>): Flow<Optional<T>> {
        val key = stringPreferencesKey(keyName)
        return optional(key).map {
            when (it) {
                is Optional.Some<String> ->
                    try {
                        some(json.decodeFromString(serializer, it.value))
                    } catch (e: SerializationException) {
                        Timber.e(e)
                        None
                    }
                else -> Optional.None
            }
        }
    }

    fun <T> get(keyName: String, serializer: KSerializer<T>, defaultValue: T): Flow<T> {
        return optional(keyName, serializer).map {
            when (it) {
                is Optional.None -> defaultValue
                else -> it.value()
            }
        }
    }

    suspend inline fun <T : Serializable> save(keyName: String, value: T) {
        val key = stringPreferencesKey(keyName)
        save(key, value.encodeAsBase64String() ?: error("Failed to encode: $value"))
    }

    inline fun <reified T : Serializable> optional(keyName: String): Flow<Optional<T>> {
        val key = stringPreferencesKey(keyName)
        return optional(key).map {
            when (it) {
                is Optional.Some<String> ->
                    try {
                        some(it.value.decodeAsBase64Object<T>())
                    } catch (e: Exception) {
                        Timber.e(e)
                        None
                    }
                else -> Optional.None
            }
        }
    }

    inline fun <reified T : Serializable> get(keyName: String, defaultValue: T): Flow<T> {
        return optional<T>(keyName).map {
            when (it) {
                is Optional.None -> defaultValue
                else -> it.value()
            }
        }
    }

    fun <T> getStateFlow(
        keyName: Preferences.Key<T>,
        scope: CoroutineScope,
        initialValue: T,
        saveDebounce: Long = 0,
    ): MutableStateFlow<T> {
        val state = MutableStateFlow(initialValue)
        scope.launch {
            state.value = get(keyName, initialValue).first()
            state.debounce(saveDebounce)
                .collectLatest { save(keyName, it) }
        }
        return state
    }

    fun <T> getStateFlow(
        keyName: String,
        serializer: KSerializer<T>,
        scope: CoroutineScope,
        initialValue: T,
        saveDebounce: Long = 0,
    ): MutableStateFlow<T> {
        val state = MutableStateFlow(initialValue)
        scope.launch {
            state.value = get(keyName, serializer, initialValue).first()
            state.debounce(saveDebounce)
                .collectLatest { save(keyName, it, serializer) }
        }
        return state
    }

    inline fun <reified T : Serializable> getStateFlow(
        keyName: String,
        scope: CoroutineScope,
        initialValue: T,
        saveDebounce: Long = 0,
    ): MutableStateFlow<T> {
        val state = MutableStateFlow(initialValue)
        scope.launch {
            state.value = get(keyName, initialValue).first()
            state.debounce(saveDebounce)
                .collectLatest { save(keyName, it) }
        }
        return state
    }
}
