/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.base.util.extensions.decodeAsBase64Object
import tm.alashow.base.util.extensions.encodeAsBase64String
import tm.alashow.datmusic.data.observers.playlist.PlaylistItemSortOption
import tm.alashow.datmusic.data.observers.playlist.PlaylistItemSortOptions

@HiltAndroidTest
class PlaylistItemSortOptionTest : BaseTest() {

    @Test
    fun `test serialization and deserialization of sort options`() {
        PlaylistItemSortOptions.ALL.forEach { sortOption ->
            val sortOptionBase64 = sortOption.encodeAsBase64String()
            assertThat(sortOptionBase64)
                .isNotEmpty()

            val decodedSortOption = sortOptionBase64?.decodeAsBase64Object<PlaylistItemSortOption>()
            assertThat(decodedSortOption)
                .isEqualTo(sortOption)
        }
    }

    @Test
    fun `toggleDescending returns copy of reverted descending sort option`() {
        PlaylistItemSortOptions.ALL.forEach { sortOption ->
            assertThat(sortOption.toggleDescending().isDescending)
                .isEqualTo(sortOption.isDescending.not())
        }
    }
}
