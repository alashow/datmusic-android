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
            val message = it.message.asString(context)
            val actionLabel = it.action?.label?.asString(context)
            when (snackbarHostState.showSnackbar(message, actionLabel)) {
                SnackbarResult.ActionPerformed -> viewModel.onSnackbarActionPerformed(it)
                SnackbarResult.Dismissed -> viewModel.onSnackbarDismissed(it)
            }
        }
    }
}
