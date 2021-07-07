/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.threeten.bp.Instant

sealed class NavigationEvent(open val route: String, open val instant: Instant = Instant.now()) {

    object Empty : NavigationEvent("Empty", Instant.ofEpochMilli(0))
    data class Back(override val instant: Instant = Instant.now()) : NavigationEvent("Back", instant)
    data class Destination(override val route: String, override val instant: Instant = Instant.now()) : NavigationEvent(route, instant)
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No LocalNavigator given")
}

class Navigator {
    private val navigationQueue = Channel<NavigationEvent>(1, BufferOverflow.DROP_OLDEST)

    suspend fun navigate(route: String) {
        navigationQueue.send(NavigationEvent.Destination(route))
    }

    suspend fun back() {
        navigationQueue.send(NavigationEvent.Back())
    }

    val queue = flow {
        navigationQueue.receiveAsFlow().collect {
            emit(it)
        }
    }
}
