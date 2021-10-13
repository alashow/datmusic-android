/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.snackbar

import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.asString
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.collectEvent

@Composable
internal fun SnackbarMessagesListener(
    snackbarHostState: SnackbarHostState = LocalScaffoldState.current.snackbarHostState,
    viewModel: SnackbarListenerViewModel = hiltViewModel()
) {
    val coroutine = rememberCoroutineScope()
    val context = LocalContext.current
    collectEvent(viewModel.messages) {
        coroutine.launch {
            val snackbarResult = snackbarHostState.showSnackbar(it.message.asString(context), it.action?.label?.asString(context))
            when (snackbarResult) {
                SnackbarResult.ActionPerformed -> viewModel.onSnackbarActionPerformed(it)
                SnackbarResult.Dismissed -> Timber.d("Snackbar dismissed")
            }
        }
    }
}
