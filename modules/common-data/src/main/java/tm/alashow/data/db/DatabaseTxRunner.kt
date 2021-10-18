/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import androidx.room.RoomDatabase

class DatabaseTxRunner(private val db: RoomDatabase) {
    fun runInTransaction(run: () -> Unit) = db.runInTransaction(run)
}
