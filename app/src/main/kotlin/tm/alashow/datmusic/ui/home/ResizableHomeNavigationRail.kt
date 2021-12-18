/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.insets.systemBarsPadding
import tm.alashow.datmusic.ui.playback.PlaybackMiniControlsDefaults
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.ui.theme.AppTheme

@OptIn(ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun RowScope.ResizableHomeNavigationRail(
    isPlayerActive: Boolean,
    selectedTab: RootScreen,
    navController: NavHostController,
    configuration: Configuration = LocalConfiguration.current,
    navigationRailWeightWithPlayer: Float = 4f,
    navigationRailWeightWithoutPlayer: Float = 2.8f,
    navigationRailWeightMinWeight: Float = 0.75f,
) {
    val haptic = LocalHapticFeedback.current
    val screenWidth = configuration.screenWidthDp
    val dragRange = (-screenWidth / 4f)..(screenWidth / 4f)
    val dragSnapAnchors = listOf(0f, dragRange.endInclusive, dragRange.start)
    var dragSnapCurrentAnchor by remember { mutableStateOf(0) }
    var dividerDragOffset by remember { mutableStateOf(0f) }
    val dividerDragOffsetWeight by derivedStateOf { (dividerDragOffset / screenWidth) * 12 }

    val navigationRailBaseWeight = if (isPlayerActive) navigationRailWeightWithPlayer else navigationRailWeightWithoutPlayer
    val navigationRailWeight by animateFloatAsState(navigationRailBaseWeight + dividerDragOffsetWeight)

    Box(Modifier.weight(navigationRailWeight.coerceAtLeast(navigationRailWeightMinWeight))) {
        HomeNavigationRail(
            selectedTab = selectedTab,
            onNavigationSelected = { selected -> navController.selectRootScreen(selected) },
            modifier = Modifier.fillMaxHeight()
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
        ) {
            Box(
                Modifier
                    .width(28.dp)
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .padding(bottom = PlaybackMiniControlsDefaults.height + AppTheme.specs.padding)
                    .systemBarsPadding()
                    // position draggable area above mini playback controls
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            dividerDragOffset += delta
                            dividerDragOffset = dividerDragOffset.coerceIn(dragRange)
                        },
                        onDragStarted = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        enabled = true,
                        indication = null,
                        onClick = {
                            // cycle through the snap anchors
                            dragSnapCurrentAnchor++
                            if (dragSnapCurrentAnchor > dragSnapAnchors.lastIndex)
                                dragSnapCurrentAnchor = 0
                            dividerDragOffset = dragSnapAnchors[dragSnapCurrentAnchor]
                        },
                    )

            )
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
            )
        }
    }
}
