/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import androidx.room.RoomDatabase
import javax.inject.Inject
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers

/**
 * Tiny class for clearing all tables of database.
 */
class NukeDatabase @Inject constructor(private val dispatchers: CoroutineDispatchers) {
    suspend fun nuke(database: RoomDatabase) = withContext(dispatchers.io) {
        database.clearAllTables()
    }
}
