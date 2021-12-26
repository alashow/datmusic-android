/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.migrators

import javax.inject.Inject
import tm.alashow.base.migrator.AppMigration
import tm.alashow.datmusic.data.interactors.backup.CreateDatmusicBackup

class AudiosFtsAppMigration @Inject constructor(
    private val datmusicBackup: CreateDatmusicBackup
) : AppMigration(id = "audios_fts_initial_index_fix") {

    override suspend fun apply() {
        // creating backup will create Downloads playlist & clear unused entities
        // triggering Audios FTS table to repopulate
        datmusicBackup.execute(Unit)
    }
}
