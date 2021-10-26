/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ButtonDefaults.textButtonColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import org.burnoutcrew.reorderable.ReorderableState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderState
import org.burnoutcrew.reorderable.reorderable
import tm.alashow.base.imageloading.ImageLoading
import tm.alashow.base.util.extensions.Callback
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.repos.playlist.ArtworkImageFileType.Companion.isUserSetArtworkPath
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudioId
import tm.alashow.datmusic.domain.entities.PlaylistItems
import tm.alashow.datmusic.ui.audios.AudioRowItem
import tm.alashow.datmusic.ui.library.R
import tm.alashow.datmusic.ui.library.playlists.PlaylistNameInput
import tm.alashow.i18n.ValidationError
import tm.alashow.ui.SwipeDismissSnackbar
import tm.alashow.ui.adaptiveColor
import tm.alashow.ui.coloredRippleClickable
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.components.DraggableItemSurface
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.components.textIconModifier
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.Orange

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditPlaylist(
    viewModel: EditPlaylistViewModel = hiltViewModel(),
) {
    val playlist by rememberFlowWithLifecycle(viewModel.playlist).collectAsState(Playlist())
    val playlistItems by rememberFlowWithLifecycle(viewModel.playlistAudios).collectAsState(emptyList())
    val lastRemovedItem by rememberFlowWithLifecycle(viewModel.lastRemovedItem).collectAsState(null)

    val name by rememberFlowWithLifecycle(viewModel.name).collectAsState(TextFieldValue())
    val nameError by rememberFlowWithLifecycle(viewModel.nameError).collectAsState(null)

    val reorderableState = rememberReorderState()
    val itemsBeforeContent = 2

    Scaffold(
        bottomBar = {
            PlaylistLastRemovedItemSnackbar(
                lastRemovedItem = lastRemovedItem,
                onDismiss = viewModel::clearLastRemovedPlaylistItem,
                onUndo = viewModel::undoLastRemovedPlaylistItem,
                modifier = Modifier.navigationBarsWithImePadding()
            )
        }
    ) {
        Box {
            LazyColumn(
                state = reorderableState.listState,
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyTop = true,
                    applyBottom = true,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
                    .reorderable(
                        state = reorderableState,
                        onMove = { from, to -> viewModel.movePlaylistItem(from - itemsBeforeContent, to - itemsBeforeContent) },
                        canDragOver = {
                            it >= itemsBeforeContent && (it - itemsBeforeContent) < (playlistItems.size)
                        }
                    ),
            ) {
                editPlaylistHeader(
                    playlist = playlist,
                    name = name,
                    onSetName = viewModel::setPlaylistName,
                    onSetPlaylistArtwork = viewModel::setPlaylistArtwork,
                    onSave = viewModel::save,
                    nameError = nameError
                )

                editPlaylistExtraActions(
                    onClearArtwork = viewModel::clearPlaylistArtwork,
                    onShuffle = viewModel::shufflePlaylist,
                    onDelete = viewModel::deletePlaylist,
                    shuffleEnabled = playlistItems.size > 1,
                    clearArtworkEnabled = playlist.artworkPath.isUserSetArtworkPath()
                )

                editablePlaylistAudioList(
                    reorderableState = reorderableState,
                    onRemove = viewModel::removePlaylistItem,
                    audios = playlistItems
                )
            }
        }
    }
}

@Composable
fun PlaylistLastRemovedItemSnackbar(
    lastRemovedItem: RemovedFromPlaylist?,
    onDismiss: Callback,
    onUndo: Callback,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = lastRemovedItem != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        lastRemovedItem?.let {
            val context = LocalContext.current
            SwipeDismissSnackbar(
                data = it.asSnackbar(context, onUndo = onUndo),
                onDismiss = onDismiss
            )
        }
    }
}

private fun LazyListScope.editPlaylistHeader(
    playlist: Playlist,
    name: TextFieldValue,
    onSetName: (TextFieldValue) -> Unit,
    onSetPlaylistArtwork: (Uri) -> Unit,
    onSave: Callback,
    nameError: ValidationError?
) {
    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppTheme.specs.padding)
        ) {
            Text(
                text = stringResource(R.string.playlist_edit_label),
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
            )

            EditablePlaylistArtwork(playlist, onSetPlaylistArtwork)

            PlaylistNameInput(
                name = name,
                onSetName = onSetName,
                onDone = onSave,
                nameError = nameError,
                modifier = Modifier.padding(horizontal = AppTheme.specs.padding)
            )

            TextRoundedButton(
                text = stringResource(R.string.playlist_edit_done),
                onClick = onSave,
            )
        }
    }
}

@Composable
private fun EditablePlaylistArtwork(
    playlist: Playlist,
    onSetPlaylistArtwork: (Uri) -> Unit,
) {
    val imagePainter = rememberImagePainter(playlist.artworkFile(), builder = ImageLoading.defaultConfig)
    val adaptiveColor = adaptiveColor(playlist.artworkFile())
    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        if (it != null) onSetPlaylistArtwork(it)
    }

    CoverImage(
        painter = imagePainter,
        size = 180.dp,
        modifier = Modifier.padding(AppTheme.specs.padding),
        imageModifier = Modifier.coloredRippleClickable(
            color = adaptiveColor.color,
            rippleRadius = Dp.Unspecified,
            onClick = {
                imagePickerLauncher.launch("image/*")
            }
        )

    )
}

private fun LazyListScope.editPlaylistExtraActions(
    onClearArtwork: Callback,
    onShuffle: Callback,
    onDelete: Callback,
    clearArtworkEnabled: Boolean,
    shuffleEnabled: Boolean,
) {
    item {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                AppTheme.specs.paddingTiny,
                Alignment.CenterHorizontally
            ),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            if (clearArtworkEnabled) {
                TextButton(
                    onClick = onClearArtwork,
                    colors = textButtonColors(contentColor = Orange),
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.textIconModifier())
                    Text(stringResource(R.string.playlist_edit_clearArtwork))
                }
            }
            if (shuffleEnabled) {
                TextButton(
                    onClick = onShuffle,
                    colors = textButtonColors(contentColor = Orange),
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.textIconModifier())
                    Text(stringResource(R.string.playlist_edit_shuffle))
                }
            }
            TextButton(
                onClick = onDelete,
                colors = textButtonColors(contentColor = MaterialTheme.colors.error),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.textIconModifier())
                Text(stringResource(R.string.playlist_edit_delete))
            }
        }
    }
}

private fun LazyListScope.editablePlaylistAudioList(
    reorderableState: ReorderableState,
    onRemove: (PlaylistAudioId) -> Unit,
    audios: PlaylistItems,
) {
    items(audios, key = { it.playlistAudio.id }) { playlistItem ->
        val haptic = LocalHapticFeedback.current
        DraggableItemSurface(reorderableState.offsetByKey(playlistItem.playlistAudio.id)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                modifier = Modifier
                    .padding(AppTheme.specs.inputPaddings),
            ) {
                IconButton(onClick = { onRemove(playlistItem.playlistAudio.id) }) {
                    Icon(
                        Icons.Default.RemoveCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.weight(2f)
                    )
                }

                AudioRowItem(
                    audio = playlistItem.audio,
                    modifier = Modifier.weight(19f),
                    includeCover = false,
                    observeNowPlayingAudio = false,
                )

                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )
                        }
                        .detectReorder(reorderableState)
                )
            }
        }
    }
}
