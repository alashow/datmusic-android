/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.material.SwipeToDismiss
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DismissableSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        snackbar = {
            SwipeDismissSnackbar(
                data = it,
                onDismiss = { it.dismiss() }
            )
        },
        modifier = modifier
    )
}

/**
 * Wrapper around [Snackbar] to make it swipe-dismissable, using [SwipeToDismiss].
 */
@Composable
fun SwipeDismissSnackbar(
    data: SnackbarData,
    onDismiss: () -> Unit = {},
    snackbar: @Composable (SnackbarData) -> Unit = { Snackbar(it) }
) {
    Dismissable(onDismiss = onDismiss) {
        snackbar(data)
    }
}
