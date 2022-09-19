/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.PLAYLIST_NAME_MAX_LENGTH
import tm.alashow.datmusic.ui.library.R
import tm.alashow.datmusic.ui.library.playlists.PlaylistNameInput
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.i18n.ValidationErrorTooLong
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.theme.AppTheme

@Composable
fun CreatePlaylistRoute(isPreviewMode: Boolean = LocalIsPreviewMode.current) {
    when {
        isPreviewMode -> CreatePlaylistPreview()
        else -> CreatePlaylist()
    }
}

@Composable
private fun CreatePlaylist(viewModel: CreatePlaylistViewModel = hiltViewModel()) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
    CreatePlaylist(
        viewState = viewState,
        onNameChange = viewModel::setPlaylistName,
        onCreatePlaylist = viewModel::createPlaylist,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CreatePlaylist(
    viewState: CreatePlaylistViewState,
    onNameChange: (String) -> Unit,
    onCreatePlaylist: () -> Unit,
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(MaterialTheme.colorScheme.background)
                .padding(AppTheme.specs.padding)
        ) {
            Text(
                text = stringResource(R.string.playlist_create_label),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            PlaylistNameInput(
                name = viewState.name,
                onSetName = onNameChange,
                onDone = onCreatePlaylist,
                nameError = viewState.nameError,
            )

            val nameIsBlank = viewState.name.isBlank()
            val createText = if (nameIsBlank) R.string.playlist_create_skipName else R.string.playlist_create
            TextRoundedButton(
                text = stringResource(createText),
                onClick = onCreatePlaylist,
            )
        }
    }
}

@CombinedPreview
@Composable
fun CreatePlaylistPreview() = PreviewDatmusicCore {
    var viewState by remember { mutableStateOf(CreatePlaylistViewState.Empty) }
    CreatePlaylist(
        viewState = viewState,
        onNameChange = {
            viewState = viewState.copy(name = it)
            if (viewState.name.length > PLAYLIST_NAME_MAX_LENGTH) {
                viewState = viewState.copy(nameError = ValidationErrorTooLong().error)
            }
        },
        onCreatePlaylist = { viewState = viewState.copy(name = "", nameError = null) },
    )
}
