/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.common.compose

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalScaffoldState = staticCompositionLocalOf<ScaffoldState> { error("No LocalScaffoldState provided") }
