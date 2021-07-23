/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.common.compose

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.staticCompositionLocalOf
import com.google.firebase.analytics.FirebaseAnalytics

val LocalScaffoldState = staticCompositionLocalOf<ScaffoldState> { error("No LocalScaffoldState provided") }

val LocalAnalytics = staticCompositionLocalOf<FirebaseAnalytics> {
    error("No LocalAnalytics provided")
}
