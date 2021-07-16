/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tonyodev.fetch2.Status
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.ui.audios.AudioRowItem
import tm.alashow.ui.components.ProgressIndicator
import tm.alashow.ui.theme.AppTheme

@Composable
internal fun AudioDownload(audioDownloadItem: AudioDownloadItem) {
    var menuVisible by remember { mutableStateOf(false) }
    val actionHandler = AudioDownloadItemActionHandler()

    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        modifier = Modifier
            .clickable { menuVisible = true }
            .fillMaxWidth()
            .padding(AppTheme.specs.inputPaddings)
    ) {
        val downloadInfo = audioDownloadItem.downloadInfo
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AudioRowItem(audio = audioDownloadItem.audio, maxLines = 1, modifier = Modifier.weight(16f))
            val isPaused = downloadInfo.status == Status.PAUSED
            val isQueued = downloadInfo.status == Status.QUEUED
            val isRetriable = downloadInfo.status == Status.FAILED || downloadInfo.status == Status.CANCELLED
            when (downloadInfo.status) {
                Status.DOWNLOADING, Status.PAUSED, Status.FAILED, Status.CANCELLED, Status.QUEUED -> {
                    DownloadRequestProgress(
                        paused = isPaused,
                        queued = isQueued,
                        retriable = isRetriable,
                        onClick = {
                            actionHandler(
                                when {
                                    isRetriable -> AudioDownloadItemAction.Retry(audioDownloadItem)
                                    isPaused -> AudioDownloadItemAction.Resume(audioDownloadItem)
                                    else -> AudioDownloadItemAction.Pause(audioDownloadItem)
                                }
                            )
                        },
                        progress = downloadInfo.progress / 100f,
                        modifier = Modifier.weight(4f)
                    )
                }
                else -> Unit
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                val fileSize = downloadInfo.fileSizeStatus()
                val status = downloadInfo.statusLabel().let {
                    when (fileSize.isNotBlank()) {
                        true -> it.lowercase()
                        else -> it
                    }
                }

                val footer = listOf(fileSize, status).filter { it.isNotBlank() }.joinToString(" · ")
                Text(text = footer, modifier = Modifier.weight(19f))
            }

            AudioDownloadDropdownMenu(
                audioDownload = audioDownloadItem.copy(downloadInfo = downloadInfo),
                expanded = menuVisible,
                onExpandedChange = { menuVisible = it },
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
            ) {
                actionHandler(AudioDownloadItemAction.from(it, audioDownloadItem))
            }
        }
    }
}

@Composable
private fun DownloadRequestProgress(
    paused: Boolean,
    queued: Boolean,
    retriable: Boolean,
    onClick: () -> Unit,
    progress: Float = 0f,
    size: Dp = 36.dp,
    strokeWidth: Dp = 2.dp,
    modifier: Modifier = Modifier
) {
    val progressAnimated by animateFloatAsState(progress.coerceIn(0f, 1f), animationSpec = tween(2000, easing = LinearEasing))

    Box(
        modifier = modifier
            .width(size)
            .clip(CircleShape),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (queued) {
            ProgressIndicator(Modifier.size(size).padding(AppTheme.specs.paddingTiny))
        } else {
            CircularProgressIndicator(
                progress = progressAnimated,
                color = MaterialTheme.colors.secondary,
                strokeWidth = strokeWidth,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
            )
            Box(
                Modifier
                    .clip(CircleShape)
                    .clickable(
                        onClick = onClick,
                        indication = rememberRipple(color = MaterialTheme.colors.secondary),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .background(MaterialTheme.colors.secondary.copy(alpha = 0.1f))
            ) {
                val icon = when {
                    retriable -> Icons.Filled.Refresh
                    paused -> Icons.Filled.PlayArrow
                    else -> Icons.Filled.Pause
                }
                Crossfade(icon) {
                    Icon(
                        painter = rememberVectorPainter(it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(size)
                            .padding(AppTheme.specs.paddingSmall)
                    )
                }
            }
        }
    }
}
