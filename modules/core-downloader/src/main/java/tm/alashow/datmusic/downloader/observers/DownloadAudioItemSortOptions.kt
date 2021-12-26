/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.observers

import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.R
import tm.alashow.domain.models.SortOption
import tm.alashow.i18n.UiMessage

abstract class DownloadAudioItemSortOption(
    private val labelRes: Int,
    override val isDescending: Boolean,
    override val comparator: Comparator<AudioDownloadItem>? = null,
) : SortOption<AudioDownloadItem>(isDescending, comparator) {
    abstract override fun toggleDescending(): DownloadAudioItemSortOption

    override fun toUiMessage() = UiMessage.Resource(labelRes)

    fun isSameOption(other: DownloadAudioItemSortOption) = labelRes == other.labelRes
}

object DownloadAudioItemSortOptions {

    val ALL = listOf(ByDate(), ByTitle(), ByArtist(), ByAlbum(), BySize(), ByDuration())

    data class ByDate(override val isDescending: Boolean = true) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byDate,
        isDescending,
        if (isDescending) null // default sort in database
        else compareBy { it.downloadRequest.createdAt }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class ByTitle(override val isDescending: Boolean = false) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byTitle,
        isDescending,
        if (isDescending) compareByDescending { it.audio.title }
        else compareBy { it.audio.title }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class ByArtist(override val isDescending: Boolean = false) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byArtist,
        isDescending,
        if (isDescending) compareByDescending { it.audio.artist }
        else compareBy { it.audio.artist }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class ByAlbum(override val isDescending: Boolean = false) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byAlbum,
        isDescending,
        if (isDescending) compareByDescending { it.audio.album }
        else compareBy { it.audio.album }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class BySize(override val isDescending: Boolean = true) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_bySize,
        isDescending,
        if (isDescending) compareByDescending { it.downloadInfo.total }
        else compareBy { it.downloadInfo.total }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }

    data class ByDuration(override val isDescending: Boolean = true) : DownloadAudioItemSortOption(
        R.string.downloads_filter_sort_byDuration,
        isDescending,
        if (isDescending) compareByDescending { it.audio.duration }
        else compareBy { it.audio.duration }
    ) {
        override fun toggleDescending() = copy(isDescending = !isDescending)
    }
}
