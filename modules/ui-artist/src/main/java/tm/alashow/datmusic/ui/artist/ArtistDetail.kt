/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.artist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.detail.MediaDetail

@Composable
fun ArtistDetail() {
    ArtistDetail(viewModel = hiltViewModel())
}

@Composable
private fun ArtistDetail(viewModel: ArtistDetailViewModel) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = ArtistDetailViewState.Empty)

    MediaDetail(
        viewState = viewState,
        titleRes = R.string.artists_detail_title,
        onFailRetry = viewModel::refresh,
        onEmptyRetry = viewModel::refresh,
        mediaDetailContent = ArtistDetailContent()
    )
}
