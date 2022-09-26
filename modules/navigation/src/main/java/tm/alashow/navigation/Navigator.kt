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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import tm.alashow.navigation.screens.ROOT_SCREENS

interface Navigator {
    fun navigate(route: String)
    fun goBack()
    val queue: Flow<NavigationEvent>
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No LocalNavigator given")
}

@Composable
fun NavigatorHost(content: @Composable () -> Unit) {
    NavigatorHost(
        navigator = hiltViewModel<NavigatorViewModel>().navigator,
        content = content,
    )
}

@Composable
private fun NavigatorHost(
    navigator: Navigator,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNavigator provides navigator, content = content)
}

sealed class NavigationEvent(open val route: String) {
    object Back : NavigationEvent("Back")
    data class Destination(override val route: String, val root: String? = null) : NavigationEvent(route)

    override fun toString() = route
}

class NavigatorImpl : Navigator {
    private val navigationQueue = Channel<NavigationEvent>(Channel.CONFLATED)
    override val queue = navigationQueue.receiveAsFlow()

    override fun navigate(route: String) {
        val basePath = route.split("/").firstOrNull()
        val root = if (ROOT_SCREENS.any { it.route == basePath }) basePath else null
        navigationQueue.trySend(NavigationEvent.Destination(route, root))
    }

    override fun goBack() {
        navigationQueue.trySend(NavigationEvent.Back)
    }
}
