/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.i18n

import java.util.Collections.emptyList

sealed class UiMessage<T : Any>(open val value: T) {
    data class Plain(override val value: String) : UiMessage<String>(value)
    data class Resource(override val value: Int, val formatArgs: List<Any> = emptyList()) : UiMessage<Int>(value)
    data class Error(override val value: Throwable) : UiMessage<Throwable>(value)
}

interface UiMessageConvertable {
    fun toUiMessage(): UiMessage<*>
}
