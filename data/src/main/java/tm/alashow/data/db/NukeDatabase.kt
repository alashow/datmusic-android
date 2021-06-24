/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import javax.inject.Inject
import tm.alashow.datmusic.data.db.AppDatabase

/**
 * Tiny class for clearing all tables of database.
 */
class NukeDatabase @Inject constructor(
    private val database: AppDatabase
) {
    suspend fun nuke() = database.clearAllTables()
}
