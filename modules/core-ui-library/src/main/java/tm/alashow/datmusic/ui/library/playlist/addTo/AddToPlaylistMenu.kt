/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlist.addTo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.ui.coreLibrary.R
import tm.alashow.datmusic.ui.library.playlist.addTo.CreatePlaylistItem.isCreatePlaylistItem
import tm.alashow.datmusic.ui.library.playlist.addTo.CreatePlaylistItem.withCreatePlaylistItem
import tm.alashow.ui.theme.AppTheme

@Composable
fun AddToPlaylistMenu(
    audio: Audio,
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddToPlaylistViewModel = hiltViewModel()
) {
    val playlists by rememberFlowWithLifecycle(viewModel.playlists).collectAsState(emptyList())

    AddToPlaylistDropdownMenu(
        expanded = visible,
        onExpandedChange = onVisibleChange,
        playlists = playlists.withCreatePlaylistItem(),
        onPlaylistSelect = {
            viewModel.addTo(playlist = it, audio)
        },
        modifier = modifier,
    )
}

@Composable
private fun AddToPlaylistDropdownMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    playlists: List<Playlist> = emptyList(),
    onPlaylistSelect: (Playlist) -> Unit = {}
) {
    Box {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Max)
                .heightIn(max = 400.dp)
                .align(Alignment.Center)
        ) {
            Text(
                stringResource(R.string.playlist_addTo),
                style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.secondary),
                modifier = Modifier.padding(AppTheme.specs.inputPaddings)
            )
            playlists.forEach { item ->
                val label = item.name
                DropdownMenuItem(
                    onClick = {
                        onExpandedChange(false)
                        onPlaylistSelect(item)
                    }
                ) {
                    Text(
                        text = label,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (item.isCreatePlaylistItem()) FontWeight.Bold else null,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
