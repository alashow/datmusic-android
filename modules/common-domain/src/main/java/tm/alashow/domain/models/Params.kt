/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.models

/**
 * Used with Calls for saving and pass call request params.
 */
data class Params(
    var page: Int = PAGE_START,
    var id: Any? = null,
    private val values: HashMap<String, Any> = hashMapOf()
) {

    companion object {
        const val PAGE_START = 1
    }

    /*******************
     * Page modifiers
     *******************/

    /**
     * Increments [page].
     */
    fun increment(): Params {
        ++this.page
        return this
    }

    /**
     * Resets [page] to zero.
     */
    fun reset(): Params {
        this.page = PAGE_START
        return this
    }

    fun isFirstPage() = this.page == PAGE_START

    /**
     * Kinda hash of this params.
     * Note: Doesn't include [page], only [values].
     */
    override fun toString(): String {
        return values.hashCode().toString()
    }

    /*******************
     * Id Functions
     *******************/

    fun id(): Long = id?.hashCode()?.toLong() ?: 0L

    fun idString(): String = id?.toString() ?: ""

    fun hasId() = id != null

    /**********************
     * Setter and getters
     **********************/

    fun set(key: String, value: Any) {
        values[key] = value
    }

    fun get(key: String): Any? = values[key]
    fun get(key: String, fallback: Any): Any = get(key) ?: fallback

    fun getString(key: String): String? = values[key] as String?
    fun getString(key: String, fallback: String): String = getString(key) ?: fallback

    fun getInt(key: String): Int? = values[key] as Int?
    fun getInt(key: String, fallback: Int): Int = getInt(key) ?: fallback

    fun getLong(key: String): Long? = values[key] as Long?
    fun getLong(key: String, fallback: Long): Long = getLong(key) ?: fallback

    fun getBoolean(key: String): Boolean = getInt(key) == 1

    /**
     * Toggles given [key] with given [first] and [second] values.
     *
     * @param key key to be toggled
     * @param defaultFirst true for [first] for default, else for [second]
     * @param first first value
     * @param second second value
     */
    fun toggle(key: String, defaultFirst: Boolean = true, first: String, second: String) {
        val current: String = getString(key, if (defaultFirst) first else second)
        set(key, if (current == first) second else first)
    }

    /**
     * Toggles given [key] with given [first] and [second] values.
     *
     * @param key key to be toggled
     * @param defaultFirst true for [first] for default, else for [second]
     * @param first first value
     * @param second second value
     */
    fun toggle(key: String, defaultFirst: Boolean = true, first: Int = 0, second: Int = 0) {
        val current: Int = getInt(key, if (defaultFirst) first else second)
        set(key, if (current == first) second else first)
    }
}
