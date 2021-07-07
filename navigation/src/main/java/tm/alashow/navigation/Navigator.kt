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

data class NavigationEvent(val route: String, val instant: Instant = Instant.now()) {
    companion object {
        val None = NavigationEvent("empty", Instant.ofEpochMilli(0))
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No LocalNavigator given")
}

class Navigator {
    private val navigationQueue = Channel<NavigationEvent>(1, BufferOverflow.DROP_OLDEST)

    suspend fun navigate(route: String) {
        navigationQueue.send(NavigationEvent(route))
    }

    val queue = flow {
        navigationQueue.receiveAsFlow().collect {
            emit(it)
        }
    }
}
