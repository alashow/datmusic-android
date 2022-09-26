/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads.audio

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Status
import tm.alashow.base.util.extensions.interpunctize
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.isComplete
import tm.alashow.datmusic.downloader.isPausable
import tm.alashow.datmusic.downloader.isResumable
import tm.alashow.datmusic.downloader.isRetriable
import tm.alashow.datmusic.downloader.progressVisible
import tm.alashow.datmusic.ui.audios.AudioRowItem
import tm.alashow.datmusic.ui.downloader.AudioDownloadItemActionHandler
import tm.alashow.datmusic.ui.downloader.LocalAudioDownloadItemActionHandler
import tm.alashow.datmusic.ui.downloads.AudioDownloadItemAction
import tm.alashow.datmusic.ui.downloads.R
import tm.alashow.datmusic.ui.downloads.fileSizeStatus
import tm.alashow.datmusic.ui.downloads.statusLabel
import tm.alashow.datmusic.ui.library.playlist.addTo.AddToPlaylistMenu
import tm.alashow.ui.TimedVisibility
import tm.alashow.ui.colorFilterDynamicProperty
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.components.ProgressIndicator
import tm.alashow.ui.material.ContentAlpha
import tm.alashow.ui.material.ProvideContentAlpha
import tm.alashow.ui.theme.AppTheme

@Composable
internal fun AudioDownload(
    audioDownloadItem: AudioDownloadItem,
    modifier: Modifier = Modifier,
    onAudioPlay: (AudioDownloadItem) -> Unit,
    actionHandler: AudioDownloadItemActionHandler = LocalAudioDownloadItemActionHandler.current
) {
    val audio = audioDownloadItem.audio
    val downloadInfo = audioDownloadItem.downloadInfo
    var menuVisible by remember { mutableStateOf(false) }
    var addToPlaylistVisible by remember { mutableStateOf(false) }

    AudioDownloadBoxWithSwipeActions(
        audioDownloadItem = audioDownloadItem,
        onAddToPlaylist = { addToPlaylistVisible = true }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
            modifier = modifier
                .clickable {
                    if (downloadInfo.isComplete()) onAudioPlay(audioDownloadItem)
                    else menuVisible = true
                }
                .fillMaxWidth()
                .padding(AppTheme.specs.inputPaddings)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AudioRowItem(
                    audio = audioDownloadItem.audio,
                    modifier = Modifier.weight(16f),
                    onCoverClick = { onAudioPlay(audioDownloadItem) }
                )
                DownloadRequestProgress(
                    downloadInfo = downloadInfo,
                    onClick = {
                        when {
                            downloadInfo.isRetriable() -> AudioDownloadItemAction.Retry(audioDownloadItem)
                            downloadInfo.isResumable() -> AudioDownloadItemAction.Resume(audioDownloadItem)
                            downloadInfo.isPausable() -> AudioDownloadItemAction.Pause(audioDownloadItem)
                            else -> null
                        }?.run(actionHandler)
                    },
                    progress = downloadInfo.progress / 100f,
                    modifier = Modifier.weight(4f)
                )
            }

            AudioDownloadFooter(
                audio = audio,
                downloadInfo = downloadInfo,
                audioDownloadItem = audioDownloadItem,
                menuVisible = menuVisible,
                onMenuVisibleChange = { menuVisible = it },
                addToPlaylistVisible = addToPlaylistVisible,
                setAddToPlaylistVisible = { addToPlaylistVisible = it },
                onAudioPlay = onAudioPlay,
                actionHandler = actionHandler
            )
        }
    }
}

@Composable
private fun AudioDownloadFooter(
    audio: Audio,
    downloadInfo: Download,
    menuVisible: Boolean,
    onMenuVisibleChange: (Boolean) -> Unit,
    addToPlaylistVisible: Boolean,
    setAddToPlaylistVisible: (Boolean) -> Unit,
    audioDownloadItem: AudioDownloadItem,
    onAudioPlay: (AudioDownloadItem) -> Unit,
    actionHandler: AudioDownloadItemActionHandler
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        ProvideContentAlpha(ContentAlpha.medium) {
            val fileSize = downloadInfo.fileSizeStatus()
            val status = downloadInfo.statusLabel().let {
                when (fileSize.isNotBlank()) {
                    true -> it.lowercase()
                    else -> it
                }
            }

            val footer = listOf(fileSize, status).filter { it.isNotBlank() }.interpunctize()
            Text(text = footer, modifier = Modifier.weight(19f))
        }

        AddToPlaylistMenu(audio, addToPlaylistVisible, setAddToPlaylistVisible)
        AudioDownloadDropdownMenu(
            audioDownload = audioDownloadItem.copy(downloadInfo = downloadInfo),
            expanded = menuVisible,
            onExpandedChange = onMenuVisibleChange,
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
        ) { actionLabel ->
            when (val action = AudioDownloadItemAction.from(actionLabel, audioDownloadItem)) {
                is AudioDownloadItemAction.Play -> onAudioPlay(action.audio)
                is AudioDownloadItemAction.AddToPlaylist -> setAddToPlaylistVisible(true)
                else -> actionHandler(action)
            }
        }
    }
}

@Composable
private fun DownloadRequestProgress(
    downloadInfo: Download,
    onClick: () -> Unit,
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = AppTheme.specs.iconSize,
    strokeWidth: Dp = 1.dp,
) {
    val paused = downloadInfo.isResumable()
    val queued = downloadInfo.status == Status.QUEUED
    val retriable = downloadInfo.isRetriable()
    val previousStatus by remember { mutableStateOf(downloadInfo.status) }

    val justCompleted by remember(downloadInfo) {
        derivedStateOf {
            (previousStatus == Status.DOWNLOADING || previousStatus == Status.QUEUED) && downloadInfo.status == Status.COMPLETED
        }
    }

    val progressAnimated by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        animationSpec = tween((Downloader.DOWNLOADS_STATUS_REFRESH_INTERVAL * 1.3).toInt(), easing = LinearEasing)
    )

    if (downloadInfo.progressVisible() || justCompleted)
        Box(
            modifier = modifier
                .width(size)
                .clip(CircleShape),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (queued) {
                ProgressIndicator(
                    size = size,
                    strokeWidth = strokeWidth,
                    modifier = Modifier
                        .size(size)
                        .padding(AppTheme.specs.paddingTiny)
                )
            } else if (downloadInfo.progressVisible()) {
                CircularProgressIndicator(
                    progress = progressAnimated,
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = strokeWidth,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                )
                IconButton(
                    onClick = onClick,
                    rippleColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(size)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
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
                                .padding(AppTheme.specs.paddingSmall)
                        )
                    }
                }
            }
            if (justCompleted) {
                TimedVisibility {
                    val completeComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.complete))
                    LottieAnimation(
                        completeComposition,
                        modifier = Modifier.size(size + AppTheme.specs.paddingTiny),
                        dynamicProperties = colorFilterDynamicProperty()
                    )
                }
            }
        }
}
