/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings.backup

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.datmusic.data.backup.DatmusicBackupToFile
import tm.alashow.datmusic.data.backup.DatmusicRestoreFromFile
import tm.alashow.datmusic.ui.settings.R
import tm.alashow.i18n.UiMessage

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val backupToFile: DatmusicBackupToFile,
    private val restoreFromFile: DatmusicRestoreFromFile,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    fun backup(outputFile: Uri) = viewModelScope.launch {
        backupToFile.execute(outputFile)
        snackbarManager.addMessage(UiMessage.Resource(R.string.settings_database_backup_complete))
    }

    fun restore(restoreFile: Uri) = viewModelScope.launch {
        restoreFromFile.execute(restoreFile)
        snackbarManager.addMessage(UiMessage.Resource(R.string.settings_database_restore_complete))
    }
}
