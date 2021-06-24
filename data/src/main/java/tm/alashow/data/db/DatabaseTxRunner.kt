/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import tm.alashow.datmusic.data.db.AppDatabase

class DatabaseTxRunner(private val db: AppDatabase) {
    fun runInTransaction(run: () -> Unit) = db.runInTransaction(run)
}
