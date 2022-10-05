/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui

import tm.alashow.i18n.UiMessage

data class SnackbarAction<T>(val label: UiMessage<*>, val argument: T)

open class SnackbarMessage<T>(val message: UiMessage<*>, val action: SnackbarAction<T>? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SnackbarMessage<*>) return false

        if (message != other.message) return false
        if (action != other.action) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + (action?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SnackbarMessage(message=$message, action=$action)"
    }
}
