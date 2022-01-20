/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback.components

import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToLong
import tm.alashow.base.util.extensions.toFloat
import tm.alashow.base.util.millisToDuration
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.PLAYBACK_PROGRESS_INTERVAL
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.isBuffering
import tm.alashow.datmusic.playback.models.PlaybackProgressState
import tm.alashow.ui.Delayed
import tm.alashow.ui.material.Slider
import tm.alashow.ui.material.SliderDefaults

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun PlaybackProgress(
    playbackState: PlaybackStateCompat,
    contentColor: Color,
    thumbRadius: Dp = 4.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    val progressState by rememberFlowWithLifecycle(playbackConnection.playbackProgress)
    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }

    Box {
        PlaybackProgressSlider(
            playbackState,
            progressState,
            draggingProgress,
            setDraggingProgress,
            thumbRadius,
            contentColor
        )
        PlaybackProgressDuration(progressState, draggingProgress, thumbRadius)
    }
}
@Composable
internal fun PlaybackProgressSlider(
    playbackState: PlaybackStateCompat,
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    setDraggingProgress: (Float?) -> Unit,
    thumbRadius: Dp,
    contentColor: Color,
    bufferedProgressColor: Color = contentColor.copy(alpha = 0.25f),
    height: Dp = 44.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    val updatedProgressState by rememberUpdatedState(progressState)
    val updatedDraggingProgress by rememberUpdatedState(draggingProgress)

    val sliderColors = SliderDefaults.colors(
        thumbColor = contentColor,
        activeTrackColor = contentColor,
        inactiveTrackColor = contentColor.copy(alpha = ContentAlpha.disabled)
    )
    val linearProgressMod = Modifier
        .fillMaxWidth(fraction = .99f) // reduce linearProgressIndicators width to match Slider's
        .clip(CircleShape) // because Slider is rounded

    val bufferedProgress by animatePlaybackProgress(progressState.bufferedProgress)
    val isBuffering = playbackState.isBuffering
    val sliderProgress = progressState.progress

    Box(
        modifier = Modifier.height(height),
        contentAlignment = Alignment.Center
    ) {
        if (!isBuffering)
            LinearProgressIndicator(
                progress = bufferedProgress,
                color = bufferedProgressColor,
                backgroundColor = Color.Transparent,
                modifier = linearProgressMod
            )

        Slider(
            value = draggingProgress ?: sliderProgress,
            onValueChange = {
                if (!isBuffering) setDraggingProgress(it)
            },
            thumbRadius = thumbRadius,
            colors = sliderColors,
            modifier = Modifier.alpha(isBuffering.not().toFloat()),
            onValueChangeFinished = {
                playbackConnection.transportControls?.seekTo(
                    (
                        updatedProgressState.total.toFloat() * (
                            updatedDraggingProgress
                                ?: 0f
                            )
                        ).roundToLong()
                )
                setDraggingProgress(null)
            }
        )

        if (isBuffering) {
            LinearProgressIndicator(
                progress = 0f,
                color = contentColor,
                modifier = linearProgressMod
            )
            Delayed(
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(linearProgressMod)
            ) {
                LinearProgressIndicator(
                    color = contentColor,
                )
            }
        }
    }
}
@Composable
internal fun BoxScope.PlaybackProgressDuration(
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    thumbRadius: Dp
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = thumbRadius)
            .align(Alignment.BottomCenter)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            val currentDuration = when (draggingProgress != null) {
                true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
                else -> progressState.currentDuration
            }
            Text(currentDuration, style = MaterialTheme.typography.caption)
            Text(progressState.totalDuration, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
internal fun animatePlaybackProgress(
    targetValue: Float,
) = animateFloatAsState(
    targetValue = targetValue,
    animationSpec = tween(
        durationMillis = PLAYBACK_PROGRESS_INTERVAL.toInt(),
        easing = FastOutSlowInEasing
    ),
)
