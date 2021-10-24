/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.base.util.CreateFileContract
import tm.alashow.datmusic.ui.settings.R
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.outlinedButtonColors

val backupFileParams = CreateFileContract.Params(suggestedName = "datmusic-backup", fileExtension = "json", fileMimeType = "application/json")

@Composable
fun BackupRestoreButton(viewModel: BackupRestoreViewModel = hiltViewModel()) {

    val backupOutputFilePickerLauncher = rememberLauncherForActivityResult(contract = CreateFileContract(backupFileParams)) {
        if (it != null) viewModel.backup(it)
    }
    val restoreInputFilePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        if (it != null) viewModel.restore(it)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
        OutlinedButton(
            onClick = { backupOutputFilePickerLauncher.launch(arrayOf(backupFileParams.fileMimeType)) },
            colors = outlinedButtonColors(),
        ) {
            Text(stringResource(R.string.settings_database_backup))
        }
        OutlinedButton(
            onClick = { restoreInputFilePickerLauncher.launch(backupFileParams.fileMimeType) },
            colors = outlinedButtonColors(),
        ) {
            Text(stringResource(R.string.settings_database_restore))
        }
    }
}
