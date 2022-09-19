/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.previews

import tm.alashow.base.ui.SnackbarManager
import tm.alashow.datmusic.ui.snackbar.SnackbarMessagesHostViewModel

internal val PreviewSnackbarManager = SnackbarManager()
internal val PreviewSnackbarMessagesHostViewModel = SnackbarMessagesHostViewModel(snackbarManager = PreviewSnackbarManager)
