/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.library.R
import tm.alashow.datmusic.ui.library.playlists.PlaylistNameInput
import tm.alashow.ui.KeyboardSpacer
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.theme.AppTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CreatePlaylist(
    viewModel: CreatePlaylistViewModel = hiltViewModel()
) {
    val name by rememberFlowWithLifecycle(viewModel.name)
    val nameError by rememberFlowWithLifecycle(viewModel.nameError)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(AppTheme.specs.padding)
    ) {
        Text(
            text = stringResource(R.string.playlist_create_label),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )

        PlaylistNameInput(
            name = name,
            onSetName = viewModel::setPlaylistName,
            onDone = viewModel::createPlaylist,
            nameError = nameError,
        )

        val nameIsBlank = name.text.isBlank()
        val createText = if (nameIsBlank) R.string.playlist_create_skipName else R.string.playlist_create
        TextRoundedButton(
            text = stringResource(createText),
            onClick = viewModel::createPlaylist,
        )

        KeyboardSpacer()
    }
}
