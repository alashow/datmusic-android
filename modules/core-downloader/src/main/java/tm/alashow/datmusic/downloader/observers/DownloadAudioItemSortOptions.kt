/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.observers

import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.R
import tm.alashow.domain.models.SortOption
import tm.alashow.domain.models.compareByDescendingSerializable
import tm.alashow.domain.models.compareBySerializable
import tm.alashow.i18n.UiMessage

abstract class DownloadAudioItemSortOption(
    override val labelRes: Int,
    override val isDescending: Boolean,
    override val comparator: Comparator<AudioDownloadItem>,
) : SortOption<AudioDownloadItem>(labelRes, isDescending, comparator) {
    abstract override fun toggleDescending(): DownloadAudioItemSortOption

    override fun toUiMessage() = UiMessage.Resource(labelRes)

    fun isSameOption(other: DownloadAudioItemSortOption) = labelRes == other.labelRes
}

object DownloadAudioItemSortOptions {

    val ALL = listOf(ByDate(), ByTitle(), ByArtist(), ByAlbum(), BySize(), ByDuration())

    data class ByDate(override val isDescending: Boolean = true) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byDate,
        isDescending,
        if (isDescending) compareByDescendingSerializable { it.downloadRequest.createdAt }
        else compareBySerializable { it.downloadRequest.createdAt }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class ByTitle(override val isDescending: Boolean = false) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byTitle,
        isDescending,
        if (isDescending) compareByDescendingSerializable { it.audio.title }
        else compareBySerializable { it.audio.title }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class ByArtist(override val isDescending: Boolean = false) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byArtist,
        isDescending,
        if (isDescending) compareByDescendingSerializable { it.audio.artist }
        else compareBySerializable { it.audio.artist }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class ByAlbum(override val isDescending: Boolean = false) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byAlbum,
        isDescending,
        if (isDescending) compareByDescendingSerializable { it.audio.album }
        else compareBySerializable { it.audio.album }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class BySize(override val isDescending: Boolean = true) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_bySize,
        isDescending,
        if (isDescending) compareByDescendingSerializable { it.downloadInfo.total }
        else compareBySerializable { it.downloadInfo.total }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class ByDuration(override val isDescending: Boolean = true) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byDuration,
        isDescending,
        if (isDescending) compareByDescendingSerializable { it.audio.duration }
        else compareBySerializable { it.audio.duration }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }
}
