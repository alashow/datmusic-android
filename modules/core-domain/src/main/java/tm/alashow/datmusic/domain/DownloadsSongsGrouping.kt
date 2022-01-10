/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain

import tm.alashow.coreDomain.R

enum class DownloadsSongsGrouping(val labelRes: Int, val exampleRes: Int) {
    ByAlbum(R.string.settings_downloads_songsGrouping_byAlbum, R.string.settings_downloads_songsGrouping_byAlbum_example),
    ByArtist(R.string.settings_downloads_songsGrouping_byArtist, R.string.settings_downloads_songsGrouping_byArtist_example),
    Flat(R.string.settings_downloads_songsGrouping_flat, R.string.settings_downloads_songsGrouping_flat_example);

    companion object {
        val Default = ByAlbum
        val map = values().associateBy { it.name }

        fun from(value: String) = map[value] ?: Default
    }
}
