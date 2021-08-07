/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Delays visibility of given [content] for [delayMillis].
 */
@Composable
fun Delayed(delayMillis: Long = 200, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TimedVisibility(delayMillis = delayMillis, visibility = false, modifier = modifier, content = content)
}

/**
 * Changes visibility of given [content] after [delayMillis] to opposite of initial [visibility].
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TimedVisibility(delayMillis: Long = 4000, visibility: Boolean = true, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(visibility) }
    val coroutine = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val job = coroutine.launch {
            delay(delayMillis)
            visible = !visible
        }

        onDispose {
            job.cancel()
        }
    }
    AnimatedVisibility(visible = visible, modifier = modifier, enter = fadeIn(), exit = fadeOut()) {
        content()
    }
}
