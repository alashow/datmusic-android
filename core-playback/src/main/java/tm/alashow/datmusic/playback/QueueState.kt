/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import kotlinx.serialization.Serializable

@Serializable
data class QueueState(
    val queue: List<String>,
    val currentId: String = "",
    val name: String = "",
    val repeatMode: Int = 0,
    val shuffleMode: Int = 0,
    val seekPosition: Long = 0,
    val state: Int = 0
)
