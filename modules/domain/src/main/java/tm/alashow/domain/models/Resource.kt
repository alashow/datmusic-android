/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.models

/**
 * A generic class that holds a value with its loading status.
 */
data class Resource(val status: Status, val error: Throwable? = null)

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    REFRESHING,
    LOADING_MORE
}
