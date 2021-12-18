/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.insets.systemBarsPadding
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.playback.PlaybackMiniControlsDefaults
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.ui.theme.AppTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun RowScope.ResizableHomeNavigationRail(
    selectedTab: RootScreen,
    navController: NavHostController,
    configuration: Configuration = LocalConfiguration.current,
    navigationRailBaseWeight: Float = 4.5f,
    navigationRailWeightMinWeight: Float = 0.9f,
    viewModel: ResizableHomeNavigationRailViewModel = hiltViewModel(),
    dividerDragOffset: State<Float> = rememberFlowWithLifecycle(viewModel.dragOffset).collectAsState(initial = 0f),
    setDividerDragOffset: (Float) -> Unit = viewModel::setDragOffset,
) {
    val screenWidth = configuration.screenWidthDp
    val dragRange = (-screenWidth / 3f)..(screenWidth / 3f)
    val dragSnapAnchors = listOf(0f, dragRange.endInclusive, dragRange.start)
    var dragSnapCurrentAnchor by remember { mutableStateOf(0) }
    val dividerDragOffsetWeight by derivedStateOf { (dividerDragOffset.value / screenWidth) * 12 }
    val navigationRailWeight = navigationRailBaseWeight + dividerDragOffsetWeight

    Box(Modifier.weight(navigationRailWeight.coerceAtLeast(navigationRailWeightMinWeight))) {
        HomeNavigationRail(
            selectedTab = selectedTab,
            onNavigationSelected = { selected -> navController.selectRootScreen(selected) },
            extraContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .resizableArea(
                            dragRange = dragRange,
                            dividerDragOffset = dividerDragOffset,
                            setDividerDragOffset = setDividerDragOffset,
                            dragSnapAnchors = dragSnapAnchors,
                            dragSnapCurrentAnchor = dragSnapCurrentAnchor,
                            setDragSnapCurrentAnchor = { dragSnapCurrentAnchor = it },
                        ),
                )
            },
            modifier = Modifier.fillMaxHeight()
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
        ) {
            Box(
                Modifier
                    .width(8.dp)
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    // position draggable area above mini playback controls
                    .padding(bottom = PlaybackMiniControlsDefaults.height + AppTheme.specs.padding)
                    .systemBarsPadding()
                    .resizableArea(
                        dragRange = dragRange,
                        dividerDragOffset = dividerDragOffset,
                        setDividerDragOffset = setDividerDragOffset,
                        dragSnapAnchors = dragSnapAnchors,
                        dragSnapCurrentAnchor = dragSnapCurrentAnchor,
                        setDragSnapCurrentAnchor = { dragSnapCurrentAnchor = it },
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

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.resizableArea(
    dragRange: ClosedFloatingPointRange<Float>,
    dividerDragOffset: State<Float>,
    setDividerDragOffset: (Float) -> Unit,
    dragSnapAnchors: List<Float>,
    dragSnapCurrentAnchor: Int,
    setDragSnapCurrentAnchor: (Int) -> Unit,
    orientation: Orientation = Orientation.Horizontal,
) = composed {
    val haptic = LocalHapticFeedback.current
    draggable(
        orientation = orientation,
        state = rememberDraggableState { delta ->
            setDividerDragOffset((dividerDragOffset.value + delta).coerceIn(dragRange))
        },
        onDragStarted = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    ).combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        enabled = true,
        indication = null,
        onClick = {
            // cycle through the snap anchors
            var newAnchor = dragSnapCurrentAnchor + 1
            if (newAnchor > dragSnapAnchors.lastIndex)
                newAnchor = 0
            setDragSnapCurrentAnchor(newAnchor)
            setDividerDragOffset(dragSnapAnchors[dragSnapCurrentAnchor])
        },
    )
}
