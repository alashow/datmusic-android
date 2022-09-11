/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.audios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tm.alashow.datmusic.ui.media.R
import tm.alashow.ui.components.MoreVerticalIcon

private val defaultMenuActionLabels = listOf(
    R.string.audio_menu_play,
    R.string.audio_menu_playNext,
    R.string.audio_menu_download,
    R.string.audio_menu_copyLink,
    R.string.playlist_addTo,
)

val currentPlayingMenuActionLabels = listOf(
    R.string.audio_menu_download,
    R.string.audio_menu_copyLink,
    R.string.playlist_addTo,
)

@Composable
fun AudioDropdownMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    actionLabels: List<Int> = defaultMenuActionLabels,
    extraActionLabels: List<Int> = emptyList(),
    onDropdownSelect: (Int) -> Unit = {}
) {
    MoreVerticalIcon(onClick = { onExpandedChange(true) }, modifier = modifier)

    Box {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .align(Alignment.Center)
        ) {
            (actionLabels + extraActionLabels).forEach { item ->
                val label = stringResource(item)
                DropdownMenuItem(
                    onClick = {
                        onExpandedChange(false)
                        onDropdownSelect(item)
                    },
                    text = { Text(text = label) }
                )
            }
        }
    }
}
