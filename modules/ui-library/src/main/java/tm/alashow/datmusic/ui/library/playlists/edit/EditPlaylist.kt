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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import tm.alashow.base.util.extensions.swap
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.repos.playlist.ArtworkImageFileType.Companion.isUserSetArtworkPath
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudioId
import tm.alashow.datmusic.domain.entities.PlaylistItems
import tm.alashow.datmusic.ui.audios.AudioRowItem
import tm.alashow.datmusic.ui.library.R
import tm.alashow.datmusic.ui.library.playlists.PlaylistNameInput
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.i18n.ValidationError
import tm.alashow.ui.ProvideScaffoldPadding
import tm.alashow.ui.SwipeDismissSnackbar
import tm.alashow.ui.adaptiveColor
import tm.alashow.ui.coloredRippleClickable
import tm.alashow.ui.components.CoverImage
import tm.alashow.ui.components.DraggableItemKey
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.components.textIconModifier
import tm.alashow.ui.simpleClickable
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.Orange
import tm.alashow.ui.theme.Theme

@Composable
fun EditPlaylistRoute(isPreviewMode: Boolean = LocalIsPreviewMode.current) {
    when {
        isPreviewMode -> EditPlaylistPreview()
        else -> EditPlaylist()
    }
}

@Composable
private fun EditPlaylist(viewModel: EditPlaylistViewModel = hiltViewModel()) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
    EditPlaylist(
        viewState = viewState,
        playlistItems = viewModel.playlistItemsState,
        onRemovePlaylistItem = viewModel::removePlaylistItem,
        onClearLastRemovedPlaylistItem = viewModel::clearLastRemovedPlaylistItem,
        onUndoLastRemovedPlaylistItem = viewModel::undoLastRemovedPlaylistItem,
        onSetPlaylistName = viewModel::setPlaylistName,
        onSetPlaylistArtwork = viewModel::setPlaylistArtwork,
        onClearPlaylistArtwork = viewModel::clearPlaylistArtwork,
        onShufflePlaylist = viewModel::shufflePlaylist,
        onDeletePlaylist = viewModel::deletePlaylist,
        onSave = viewModel::save,
        onMovePlaylistItem = viewModel::movePlaylistItem,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPlaylist(
    viewState: EditPlaylistViewState,
    playlistItems: PlaylistItems,
    onRemovePlaylistItem: (PlaylistAudioId) -> Unit,
    onClearLastRemovedPlaylistItem: () -> Unit,
    onUndoLastRemovedPlaylistItem: () -> Unit,
    onSetPlaylistName: (String) -> Unit,
    onSetPlaylistArtwork: (Uri) -> Unit,
    onClearPlaylistArtwork: () -> Unit,
    onShufflePlaylist: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onMovePlaylistItem: OnMovePlaylistItem
) {
    Scaffold(
        bottomBar = {
            PlaylistLastRemovedItemSnackbar(
                lastRemovedPlaylistItem = viewState.lastRemovedPlaylistItem,
                onDismiss = onClearLastRemovedPlaylistItem,
                onUndo = onUndoLastRemovedPlaylistItem,
                modifier = Modifier.navigationBarsPadding(),
            )
        },
    ) { paddings ->
        ProvideScaffoldPadding(paddings) {
            val itemsBeforeContent = 3
            val reorderableState = rememberReorderableLazyListState(
                onMove = { from, to -> onMovePlaylistItem(from.index - itemsBeforeContent, to.index - itemsBeforeContent) },
                canDragOver = { it.key is DraggableItemKey },
            )

            Box(modifier) {
                LazyColumn(
                    state = reorderableState.listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .reorderable(reorderableState)
                ) {
                    item {
                        Spacer(Modifier.statusBarsPadding())
                    }
                    editPlaylistHeader(
                        playlist = viewState.playlist,
                        name = viewState.name,
                        onSetName = onSetPlaylistName,
                        nameError = viewState.nameError,
                        onSetPlaylistArtwork = onSetPlaylistArtwork,
                        onSave = onSave,
                    )

                    editPlaylistExtraActions(
                        onClearArtwork = onClearPlaylistArtwork,
                        onShuffle = onShufflePlaylist,
                        onDelete = onDeletePlaylist,
                        shuffleEnabled = playlistItems.size > 1,
                        clearArtworkEnabled = viewState.playlist.artworkPath.isUserSetArtworkPath()
                    )

                    editablePlaylistAudioList(
                        reorderableState = reorderableState,
                        onRemove = onRemovePlaylistItem,
                        audios = playlistItems,
                    )
                    item {
                        Spacer(Modifier.systemBarsPadding())
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistLastRemovedItemSnackbar(
    lastRemovedPlaylistItem: RemovedFromPlaylist?,
    onDismiss: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = lastRemovedPlaylistItem != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        lastRemovedPlaylistItem?.let {
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
    name: String,
    onSetName: (String) -> Unit,
    onSetPlaylistArtwork: (Uri) -> Unit,
    onSave: () -> Unit,
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
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )

            EditablePlaylistArtwork(playlist, onSetPlaylistArtwork)

            PlaylistNameInput(
                name = name,
                onSetName = onSetName,
                onDone = onSave,
                nameError = nameError,
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
    val image = playlist.artworkFile()
    val adaptiveColor by adaptiveColor(image)
    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        if (it != null) onSetPlaylistArtwork(it)
    }

    CoverImage(
        data = image,
        size = 180.dp,
        modifier = Modifier
            .padding(AppTheme.specs.padding)
            .simpleClickable {
                if (image == null)
                    imagePickerLauncher.launch("image/*")
            },
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
    onClearArtwork: () -> Unit,
    onShuffle: () -> Unit,
    onDelete: () -> Unit,
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
                colors = textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.textIconModifier())
                Text(stringResource(R.string.playlist_edit_delete))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.editablePlaylistAudioList(
    reorderableState: ReorderableState<LazyListItemInfo>,
    onRemove: (PlaylistAudioId) -> Unit,
    audios: PlaylistItems,
) {
    items(audios, key = { DraggableItemKey(it.playlistAudio.id) }) { playlistItem ->
        val haptic = LocalHapticFeedback.current
        val itemKey = DraggableItemKey(playlistItem.playlistAudio.id)
        val isDragging = reorderableState.draggingItemKey == itemKey
        ReorderableItem(
            reorderableState, key = itemKey,
            if (isDragging) Modifier else Modifier.animateItemPlacement()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                modifier = Modifier
                    .padding(vertical = Theme.specs.paddingSmall)
                    .padding(start = Theme.specs.paddingSmall)
                    .padding(end = Theme.specs.padding),
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
                    maxLines = 1,
                )

                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = null,
                    modifier = Modifier
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

@CombinedPreview
@Composable
private fun EditPlaylistPreview() = PreviewDatmusicCore {
    val playlist = remember(Unit) { SampleData.playlist() }
    val playlistItems = remember { SampleData.list { playlistItem(playlist = playlist) }.toMutableStateList() }
    var viewState by remember { mutableStateOf(EditPlaylistViewState.Empty) }
    EditPlaylist(
        viewState = viewState,
        playlistItems = playlistItems,
        onClearLastRemovedPlaylistItem = {},
        onUndoLastRemovedPlaylistItem = {},
        onSetPlaylistArtwork = {},
        onClearPlaylistArtwork = {},
        onDeletePlaylist = {},
        onSave = {},
        onRemovePlaylistItem = { playlistItemId ->
            val playlistItemIndex = playlistItems.indexOfFirst { it.playlistAudio.id == playlistItemId }
            val playlistItem = playlistItems[playlistItemIndex]
            playlistItems.remove(playlistItem)
            viewState = viewState.copy(lastRemovedPlaylistItem = RemovedFromPlaylist(playlistItem, playlistItemIndex))
        },
        onSetPlaylistName = { viewState = viewState.copy(name = it) },
        onShufflePlaylist = { playlistItems.shuffle() },
        onMovePlaylistItem = { from, to -> playlistItems.swap(from, to) },
    )
}
