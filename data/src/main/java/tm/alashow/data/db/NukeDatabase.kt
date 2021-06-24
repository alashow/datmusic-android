/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import io.reactivex.Completable
import javax.inject.Inject
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.datmusic.data.db.AppDatabase

/**
 * Tiny class for clearing all tables of database.
 */
class NukeDatabase @Inject constructor(
    private val database: AppDatabase,
    private val schedulers: AppRxSchedulers
) {
    fun nuke() = Completable.fromAction { database.clearAllTables() }.subscribeOn(schedulers.database)
}
