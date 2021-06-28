/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.navigation

import kotlinx.coroutines.flow.MutableSharedFlow

class Navigator {

    private var navigationQueue = MutableSharedFlow<Screen>()

    fun navigate(screen: Screen) {
        navigationQueue.tryEmit(screen)
    }
}
