/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tm.alashow.datmusic.domain.entities.PlaylistWithAudios
import tm.alashow.datmusic.ui.detail.MediaDetailEmpty
import tm.alashow.datmusic.ui.library.R
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Success
import tm.alashow.ui.components.EmptyErrorBox

class PlaylistDetailEmpty : MediaDetailEmpty<PlaylistWithAudios>() {
    override operator fun invoke(
        list: LazyListScope,
        details: Async<PlaylistWithAudios>,
        detailsEmpty: Boolean,
        onEmptyRetry: () -> Unit
    ) {
        if (details is Success && detailsEmpty) {
            list.item {
                EmptyErrorBox(
                    onRetryClick = onEmptyRetry,
                    message = stringResource(R.string.playlist_empty),
                    retryLabel = stringResource(R.string.playlist_empty_addSongs),
                    modifier = Modifier.fillParentMaxHeight(0.5f)
                )
            }
        }
    }
}
