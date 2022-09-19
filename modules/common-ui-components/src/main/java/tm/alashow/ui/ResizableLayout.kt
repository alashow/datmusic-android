/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.LocalIsPreviewMode

val WIDE_LAYOUT_MIN_WIDTH = 600.dp

// TODO: Enable back wide layout in preview mode when hiltViewModel works in previews
//       because wide layout uses ResizablePlaybackSheetLayoutViewModel
@Composable
fun BoxWithConstraintsScope.isWideLayout(isPreviewMode: Boolean = LocalIsPreviewMode.current) =
    maxWidth >= WIDE_LAYOUT_MIN_WIDTH && !isPreviewMode

@Composable
fun RowScope.ResizableLayout(
    availableWidth: Dp,
    initialWeight: Float,
    minWeight: Float,
    maxWeight: Float,
    dragOffset: State<Float>,
    setDragOffset: (Float) -> Unit,
    analyticsPrefix: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(Modifier) -> Unit,
) {
    var weight by remember { mutableStateOf(initialWeight) }
    Box(modifier.weight(weight.coerceIn(minWeight, maxWeight))) {
        val availableWidthValue = availableWidth.value
        val dragRange = (-availableWidthValue / 3f)..(availableWidthValue / 3f)
        val dragSnapAnchors = listOf(0f, dragRange.endInclusive, dragRange.start)
        val dragOffsetWeight by remember { derivedStateOf { (dragOffset.value / availableWidthValue) * maxWeight } }
        var dragSnapCurrentAnchor by remember { mutableStateOf(0) }
        weight = initialWeight + dragOffsetWeight

        val resizableModifier = Modifier.resizableArea(
            dragRange = dragRange,
            dividerDragOffset = dragOffset,
            setDividerDragOffset = setDragOffset,
            dragSnapAnchors = dragSnapAnchors,
            dragSnapCurrentAnchor = dragSnapCurrentAnchor,
            setDragSnapCurrentAnchor = { dragSnapCurrentAnchor = it },
            analyticsPrefix = analyticsPrefix
        )

        content(resizableModifier)
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.resizableArea(
    dragRange: ClosedFloatingPointRange<Float>,
    dividerDragOffset: State<Float>,
    setDividerDragOffset: (Float) -> Unit,
    dragSnapAnchors: List<Float>,
    dragSnapCurrentAnchor: Int,
    setDragSnapCurrentAnchor: (Int) -> Unit,
    analyticsPrefix: String,
    orientation: Orientation = Orientation.Horizontal,
) = composed {
    val haptic = LocalHapticFeedback.current
    val analytics = LocalAnalytics.current
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
            analytics.event("$analyticsPrefix.snapAnchor", mapOf("anchor" to newAnchor))
        },
    )
}
