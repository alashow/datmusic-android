/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.SharedPreferences

class LocalConfig constructor(private val sharedPreferences: SharedPreferences) {

    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun remove(key: String) = editor.remove(key).commit()

    @Synchronized
    fun <T> get(dataType: Class<T>, key: String): Any? =
        when (dataType) {
            Int::class.java -> sharedPreferences.getInt(key, 0)
            String::class.java -> sharedPreferences.getString(key, null)
            Long::class.java -> sharedPreferences.getLong(key, 0)
            Boolean::class.java -> sharedPreferences.getBoolean(key, false)
            else -> null
        }

    @Synchronized
    fun <T> put(data: T, dataType: Class<*>, key: String) {
        when (dataType) {
            Int::class.java -> editor.putInt(key, data as Int)
            String::class.java -> editor.putString(key, data as String)
            Long::class.java -> editor.putLong(key, data as Long)
            Boolean::class.java -> editor.putBoolean(key, data as Boolean)
        }
        editor.commit()
    }

    fun has(key: String) = sharedPreferences.contains(key)

    fun getInt(key: String, fallback: Int = 0): Int {
        return get(Int::class.java, key)?.toString()?.toInt() ?: fallback
    }

    fun put(key: String, value: Int) {
        put(value, Int::class.java, key)
    }

    fun getString(key: String, fallback: String = ""): String {
        return get(String::class.java, key)?.toString() ?: fallback
    }

    fun put(key: String, value: String) {
        put(value, String::class.java, key)
    }

    fun getLong(key: String, fallback: Long = 0L): Long {
        return get(Long::class.java, key)?.toString()?.toLong() ?: fallback
    }

    fun put(key: String, value: Long) {
        put(value, Long::class.java, key)
    }
}
