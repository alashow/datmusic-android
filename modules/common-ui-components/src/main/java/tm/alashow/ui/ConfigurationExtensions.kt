/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun isWideScreen(
    configuration: Configuration = LocalConfiguration.current,
) = remember {
    derivedStateOf {
        configuration.screenWidthDp >= 600
    }
}
