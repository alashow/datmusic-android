/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tm.alashow.datmusic.domain.entities.PlaylistItems
import tm.alashow.datmusic.ui.detail.MediaDetailEmpty
import tm.alashow.datmusic.ui.library.R
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Success
import tm.alashow.ui.Delayed
import tm.alashow.ui.components.EmptyErrorBox

internal class PlaylistDetailEmpty : MediaDetailEmpty<PlaylistItems>() {
    override operator fun invoke(
        list: LazyListScope,
        details: Async<PlaylistItems>,
        isHeaderVisible: Boolean,
        detailsEmpty: Boolean,
        onEmptyRetry: () -> Unit
    ) {
        if (details is Success && detailsEmpty) {
            list.item {
                Delayed {
                    EmptyErrorBox(
                        onRetryClick = onEmptyRetry,
                        message = stringResource(R.string.playlist_empty),
                        retryLabel = stringResource(R.string.playlist_empty_addSongs),
                        modifier = Modifier.fillParentMaxHeight(if (isHeaderVisible) 0.5f else 1f)
                    )
                }
            }
        }
    }
}
