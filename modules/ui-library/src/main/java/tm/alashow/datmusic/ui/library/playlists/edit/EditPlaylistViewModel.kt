/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.edit

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.move
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.orBlank
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.interactors.playlist.ClearPlaylistArtwork
import tm.alashow.datmusic.data.interactors.playlist.DeletePlaylist
import tm.alashow.datmusic.data.interactors.playlist.RemovePlaylistItems
import tm.alashow.datmusic.data.interactors.playlist.SetCustomPlaylistArtwork
import tm.alashow.datmusic.data.interactors.playlist.UpdatePlaylist
import tm.alashow.datmusic.data.interactors.playlist.UpdatePlaylistItems
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylistDetails
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistAudioId
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.domain.entities.PlaylistItem
import tm.alashow.datmusic.domain.entities.PlaylistItems
import tm.alashow.i18n.ValidationError
import tm.alashow.i18n.asValidationError
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.PLAYLIST_ID_KEY

@HiltViewModel
class EditPlaylistViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val observePlaylist: ObservePlaylist,
    private val observePlaylistDetails: ObservePlaylistDetails,
    private val updatePlaylist: UpdatePlaylist,
    private val deletePlaylist: DeletePlaylist,
    private val reorderPlaylist: UpdatePlaylistItems,
    private val removePlaylistItems: RemovePlaylistItems,
    private val setCustomPlaylistArtwork: SetCustomPlaylistArtwork,
    private val clearPlaylistArtwork: ClearPlaylistArtwork,
    private val analytics: FirebaseAnalytics,
    private val navigator: Navigator,
) : ViewModel() {

    private val playlistId = requireNotNull(handle.get<PlaylistId>(PLAYLIST_ID_KEY))
    private val playlistDetailParams = ObservePlaylistDetails.Params(playlistId)

    private val nameState = MutableStateFlow(TextFieldValue())
    val name = nameState.asStateFlow()

    private val nameErrorState = MutableStateFlow<ValidationError?>(null)
    val nameError = nameErrorState.asStateFlow()

    val playlist = observePlaylist.flow
        .stateInDefault(viewModelScope, Playlist())

    private val playlistItems = MutableStateFlow<PlaylistItems>(emptyList())
    private val removedPlaylistItems = MutableStateFlow<Set<PlaylistItem>>(setOf())

    val playlistAudios = combine(playlistItems, removedPlaylistItems, ::Pair).map { (items, removed) ->
        items.filterNot { removed.contains(it) }
    }.stateInDefault(viewModelScope, emptyList())

    private val lastRemovedItemState = MutableStateFlow<RemovedFromPlaylist?>(null)
    val lastRemovedItem = lastRemovedItemState.asStateFlow()

    init {
        observePlaylist(playlistId)
        observePlaylistDetails(playlistDetailParams)

        viewModelScope.launch {
            val playlist = observePlaylist.get()
            nameState.value = TextFieldValue(playlist.name)

            observePlaylistDetails.flow.collectLatest {
                playlistItems.value = it
            }
        }
    }

    fun setPlaylistName(value: TextFieldValue) {
        nameState.value = value
        nameErrorState.value = null
    }

    fun removePlaylistItem(id: PlaylistAudioId) {
        var removedItem: PlaylistItem
        var removedIndex: Int
        playlistItems.value = playlistItems.value.toMutableList().also {
            removedIndex = it.indexOfFirst { item -> item.playlistAudio.id == id }
            removedItem = it.removeAt(removedIndex)
        }
        removedPlaylistItems.value = removedPlaylistItems.value.toMutableSet().also {
            it.add(removedItem)
        }

        lastRemovedItemState.value = RemovedFromPlaylist(removedItem, removedIndex)

        // clear last removed state with delay unless list is empty now
        if (playlistItems.value.isNotEmpty())
            clearLastRemovedPlaylistItem(delay = true)
        analytics.event("playlists.edit.remove", mapOf("playlistId" to playlistId, "audioId" to removedItem.audio.id))
    }

    fun undoLastRemovedPlaylistItem() {
        val removedItemMessage = lastRemovedItemState.value ?: error("Can't undo, no removed item")
        val removedItem = removedItemMessage.playlistItem
        val removedIndex = removedItemMessage.removedIndex

        playlistItems.value = playlistItems.value.toMutableList().also {
            it.add(removedIndex, removedItem)
        }
        removedPlaylistItems.value = removedPlaylistItems.value.toMutableSet().also {
            it.remove(removedItem)
        }
        clearLastRemovedPlaylistItem()
        analytics.event("playlists.edit.undoRemove", mapOf("playlistId" to playlistId, "audioId" to removedItem.audio.id))
    }

    private var delayedClearLastRemovedJob: Job? = null

    fun clearLastRemovedPlaylistItem(delay: Boolean = false) {
        if (delay) {
            delayedClearLastRemovedJob?.cancel()
            delayedClearLastRemovedJob = viewModelScope.launch {
                delay(RemovedFromPlaylist.SNACKBAR_DURATION_MILLIS)
                clearLastRemovedPlaylistItem(delay = false)
            }
        } else lastRemovedItemState.value = null
    }

    fun movePlaylistItem(from: Int, to: Int) {
        val updated = playlistItems.value.toMutableList().also { it.move(from, to) }
        playlistItems.value = updated
    }

    fun shufflePlaylist() {
        analytics.event("playlists.edit.shuffle", mapOf("playlistId" to playlistId))
        val updated = playlistItems.value.toMutableList().also { it.shuffle() }
        playlistItems.value = updated
    }

    fun deletePlaylist() = viewModelScope.launch {
        analytics.event("playlists.edit.delete", mapOf("playlistId" to playlistId))
        deletePlaylist(playlistId).collectLatest {
            navigator.goBack()
        }
    }

    fun setPlaylistArtwork(uri: Uri) = viewModelScope.launch {
        analytics.event("playlists.edit.setArtwork", mapOf("playlistId" to playlistId))
        setCustomPlaylistArtwork.execute(SetCustomPlaylistArtwork.Params(playlistId, uri))
    }

    fun clearPlaylistArtwork() = viewModelScope.launch {
        analytics.event("playlists.edit.clearArtwork", mapOf("playlistId" to playlistId))
        clearPlaylistArtwork.execute(playlistId)
    }

    fun save() {
        analytics.event("playlists.edit.save", mapOf("playlistId" to playlistId))
        viewModelScope.launch {
            removePlaylistItems.execute(removedPlaylistItems.value.map { it.playlistAudio.id })

            val repositionedItems = playlistItems.value.mapIndexed { index, it -> it.copy(playlistAudio = it.playlistAudio.copy(position = index)) }
            reorderPlaylist.execute(repositionedItems)

            val playlist = observePlaylist.get().copy(name = nameState.value.text.orBlank())
            updatePlaylist(playlist).catch {
                nameErrorState.value = it.asValidationError()
            }.collectLatest {
                navigator.goBack()
            }
        }
    }
}
