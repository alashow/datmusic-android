/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No LocalNavigator given")
}

@Composable
fun NavigatorHost(
    viewModel: NavigatorViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNavigator provides viewModel.navigator, content = content)
}

sealed class NavigationEvent(open val route: String) {
    object Back : NavigationEvent("Back")
    data class Destination(override val route: String) : NavigationEvent(route)

    override fun toString() = route
}

class Navigator {
    private val navigationQueue = Channel<NavigationEvent>(Channel.CONFLATED)

    fun navigate(route: String) {
        navigationQueue.trySend(NavigationEvent.Destination(route))
    }

    fun goBack() {
        navigationQueue.trySend(NavigationEvent.Back)
    }

    val queue = navigationQueue.receiveAsFlow()
}
