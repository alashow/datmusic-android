/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlinx.coroutines.launch
import tm.alashow.ui.theme.AppTheme

// from https://github.com/nanihadesuka/LazyColumnScrollbar

@Composable
fun LazyColumnScrollbar(
    listState: LazyListState,
    rightSide: Boolean = true,
    enabled: Boolean = true,
    thickness: Dp = 4.dp,
    padding: Dp = AppTheme.specs.paddingSmall,
    thumbHeight: Float = 0.1f,
    thumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    thumbSelectedColor: Color = MaterialTheme.colorScheme.secondary,
    thumbShape: Shape = CircleShape,
    content: @Composable () -> Unit
) {
    Box {
        content()
        if (enabled)
            LazyColumnScrollbar(
                listState = listState,
                rightSide = rightSide,
                thickness = thickness,
                padding = padding,
                thumbHeight = thumbHeight,
                thumbColor = thumbColor,
                thumbSelectedColor = thumbSelectedColor,
                thumbShape = thumbShape,
            )
    }
}

/**
 * Scrollbar for LazyColumn
 *
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding   Padding of the scrollbar
 * @param thumbHeight Thumb height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 */
@Composable
fun LazyColumnScrollbar(
    listState: LazyListState,
    rightSide: Boolean = true,
    thickness: Dp = 4.dp,
    padding: Dp = AppTheme.specs.paddingSmall,
    thumbHeight: Float = 0.1f,
    thumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    thumbSelectedColor: Color = MaterialTheme.colorScheme.secondary,
    thumbShape: Shape = CircleShape
) {
    val coroutineScope = rememberCoroutineScope()
    var isSelected by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    fun normalizedOffsetPosition() = listState.layoutInfo.let {
        if (it.totalItemsCount == 0 || it.visibleItemsInfo.isEmpty()) 0f
        else it.visibleItemsInfo.first().run { index.toFloat() - offset.toFloat() / size.toFloat() } / it.totalItemsCount.toFloat()
    }

    fun setScrollOffset(newOffset: Float) {
        dragOffset = newOffset.coerceIn(0f, 1f)

        val exactIndex: Float = listState.layoutInfo.totalItemsCount.toFloat() * dragOffset
        val index: Int = floor(exactIndex).toInt()
        val remainder: Float = exactIndex - floor(exactIndex)

        coroutineScope.launch {
            listState.scrollToItem(index = index, scrollOffset = 0)
            val offset = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.let { it.toFloat() * remainder }?.toInt() ?: 0
            listState.scrollToItem(index = index, scrollOffset = offset)
        }
    }

    val isInAction = listState.isScrollInProgress || isSelected

    val alpha by animateFloatAsState(
        targetValue = if (isInAction) 1f else 0f,
        animationSpec = tween(durationMillis = if (isInAction) 75 else 500, delayMillis = if (isInAction) 0 else 500)
    )

    val displacement by animateFloatAsState(
        targetValue = if (isInAction) 0f else 14f,
        animationSpec = tween(durationMillis = if (isInAction) 75 else 500, delayMillis = if (isInAction) 0 else 500)
    )

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val dragState = rememberDraggableState { delta ->
            setScrollOffset(dragOffset + delta / constraints.maxHeight.toFloat())
        }

        BoxWithConstraints(
            Modifier
                .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
                .alpha(alpha)
                .fillMaxHeight()
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    startDragImmediately = true,
                    onDragStarted = { offset ->
                        val newOffset = offset.y / constraints.maxHeight.toFloat()
                        val currentOffset = normalizedOffsetPosition()

                        if (currentOffset < newOffset && newOffset < currentOffset)
                            dragOffset = currentOffset
                        else
                            setScrollOffset(newOffset)
                        isSelected = true
                    },
                    onDragStopped = {
                        isSelected = false
                    }
                )
                .absoluteOffset(x = if (rightSide) displacement.dp else -displacement.dp)
        ) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer { translationY = constraints.maxHeight.toFloat() * normalizedOffsetPosition() }
                    .padding(horizontal = padding)
                    .width(thickness)
                    .clip(thumbShape)
                    .background(if (isSelected) thumbSelectedColor else thumbColor)
                    .fillMaxHeight(thumbHeight)
            )
        }
    }
}
