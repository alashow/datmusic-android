/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.base.util.CreateFileContract
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.settings.R
import tm.alashow.datmusic.ui.settings.SettingsLoadingButton

@Composable
fun BackupRestoreButton(viewModel: BackupRestoreViewModel = hiltViewModel()) {
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(BackupRestoreViewState.Empty)

    val backupOutputFilePickerLauncher = rememberLauncherForActivityResult(contract = CreateFileContract(BACKUP_FILE_PARAMS)) {
        if (it != null) viewModel.backupTo(it)
    }
    val restoreInputFilePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        if (it != null) viewModel.restoreFrom(it)
    }

    Column(horizontalAlignment = Alignment.End) {
        SettingsLoadingButton(
            isLoading = viewState.isBackingUp,
            text = stringResource(R.string.settings_database_backup),
            onClick = { backupOutputFilePickerLauncher.launch(arrayOf(BACKUP_FILE_PARAMS.fileMimeType)) }
        )

        SettingsLoadingButton(
            isLoading = viewState.isRestoring,
            text = stringResource(R.string.settings_database_restore),
            onClick = { restoreInputFilePickerLauncher.launch(BACKUP_FILE_PARAMS.fileMimeType) }
        )
    }
}