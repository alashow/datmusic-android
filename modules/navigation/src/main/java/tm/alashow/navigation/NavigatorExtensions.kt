/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.navigation

import app.cash.turbine.test

suspend fun Navigator.assertNextRouteContains(vararg expectedValues: String?) = queue.test {
    val newRoute = awaitItem().route
    expectedValues.filterNotNull().forEach {
        assert(newRoute.contains(it))
    }
}
