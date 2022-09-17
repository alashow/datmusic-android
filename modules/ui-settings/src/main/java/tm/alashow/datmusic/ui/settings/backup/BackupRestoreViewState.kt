/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings.backup

import javax.annotation.concurrent.Immutable

@Immutable
internal data class BackupRestoreViewState(
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false
) {
    companion object {
        val Empty = BackupRestoreViewState()
    }
}
