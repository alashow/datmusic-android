/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import tm.alashow.base.util.millisToDuration

data class PlaybackProgressState(
    val total: Long = 0L,
    val lastPosition: Long = 0L,
    val elapsed: Long = 0L,
    val buffered: Long = 0L,
) {

    val progress get() = ((lastPosition.toFloat() + elapsed) / (total + 1).toFloat()).coerceIn(0f, 1f)
    val bufferedProgress get() = ((buffered.toFloat()) / (total + 1).toFloat()).coerceIn(0f, 1f)

    val currentDuration get() = (lastPosition + elapsed).millisToDuration()
    val totalDuration get() = total.millisToDuration()
}
