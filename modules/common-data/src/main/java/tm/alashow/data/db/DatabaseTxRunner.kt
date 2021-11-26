/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import androidx.room.RoomDatabase

open class DatabaseTxRunner(private val db: RoomDatabase) {
    suspend fun invoke(run: () -> Unit) = db.runInTransaction(run)
}
