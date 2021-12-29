/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = Audio::class)
@Entity(tableName = "audios_fts")
data class AudioFts(
    val id: String,
    val artist: String,
    val title: String,
    val album: String?,
)
