/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import tm.alashow.base.util.CreateFileContract
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.settings.R
import tm.alashow.datmusic.ui.settings.SettingsLoadingButton
import tm.alashow.ui.theme.AppTheme

@Composable
internal fun BackupRestoreButton(
    modifier: Modifier = Modifier,
    isPreviewMode: Boolean = LocalIsPreviewMode.current,
) {
    when (isPreviewMode) {
        true -> BackupRestoreButtonPreview()
        false -> BackupRestoreButton(modifier, hiltViewModel())
    }
}

@Composable
private fun BackupRestoreButton(
    modifier: Modifier = Modifier,
    viewModel: BackupRestoreViewModel,
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
    BackupRestoreButton(
        modifier = modifier,
        viewState = viewState,
        onBackupTo = viewModel::backupTo,
        onRestoreFrom = viewModel::restoreFrom,
    )
}

@Composable
private fun BackupRestoreButton(
    modifier: Modifier = Modifier,
    viewState: BackupRestoreViewState,
    onBackupTo: (Uri) -> Unit,
    onRestoreFrom: (Uri) -> Unit,
) {
    val backupOutputFilePickerLauncher = rememberLauncherForActivityResult(contract = CreateFileContract(BACKUP_FILE_PARAMS)) {
        if (it != null) onBackupTo(it)
    }
    val restoreInputFilePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        if (it != null) onRestoreFrom(it)
    }

    FlowRow(
        mainAxisAlignment = FlowMainAxisAlignment.End,
        mainAxisSpacing = AppTheme.specs.paddingSmall,
        mainAxisSize = SizeMode.Expand,
        modifier = modifier,
    ) {
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

@CombinedPreview
@Composable
private fun BackupRestoreButtonPreview(modifier: Modifier = Modifier) {
    BackupRestoreButton(
        modifier = modifier,
        viewState = BackupRestoreViewState.Empty,
        onBackupTo = {},
        onRestoreFrom = {}
    )
}
