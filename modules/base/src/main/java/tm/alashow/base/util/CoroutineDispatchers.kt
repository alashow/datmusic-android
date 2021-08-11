/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import kotlinx.coroutines.CoroutineDispatcher

data class CoroutineDispatchers(
    val network: CoroutineDispatcher,
    val io: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val main: CoroutineDispatcher,
)
