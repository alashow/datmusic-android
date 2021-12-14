/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.testing

import app.cash.turbine.FlowTurbine

/**
 * Waits for first item and then completion.
 * Probably needs a better name
 */
suspend fun <T> FlowTurbine<T>.awaitSingle(): T {
    val item = awaitItem()
    awaitComplete()
    return item
}
