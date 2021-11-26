/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db

import javax.inject.Inject
import tm.alashow.data.db.NukeDatabase

class DatabaseNuke @Inject constructor(
    private val nukeDatabase: NukeDatabase,
    private val appDatabase: AppDatabase,
) {
    suspend fun nuke() = nukeDatabase.nuke(appDatabase)
}
