/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import tm.alashow.base.util.extensions.muteUntil
import tm.alashow.ui.components.AppBarNavigationIcon
import tm.alashow.ui.components.AppTopBar

open class MediaDetailTopBar {

    @Composable
    open operator fun invoke(
        title: String,
        collapsedProgress: State<Float>,
        onGoBack: () -> Unit,
    ) {
        AppTopBar(
            title = title,
            collapsedProgress = collapsedProgress.value.muteUntil(0.9f),
            navigationIcon = { AppBarNavigationIcon(onClick = onGoBack) },
        )
    }
}
