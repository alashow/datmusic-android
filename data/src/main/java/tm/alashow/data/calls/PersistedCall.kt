/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.calls

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.data.db.DatabaseTxRunner
import tm.alashow.data.db.SingleEntryDao
import tm.alashow.domain.Entry
import tm.alashow.domain.Params

abstract class PersistedCall<ET : Entry, out ED : SingleEntryDao<Params, ET>>(
    private val databaseTxRunner: DatabaseTxRunner,
    private val entryDao: ED,
    private val schedulers: AppRxSchedulers
) : Call<Params, ET> {

    override fun data(params: Params): Flowable<ET> {
        return entryDao.entry(params)
            .distinctUntilChanged()
            .subscribeOn(schedulers.database)
    }

    override fun isEmpty(params: Params): Single<Boolean> {
        return entryDao.count(params)
            .subscribeOn(schedulers.database)
            .map { it == 0 }
    }

    private fun load(params: Params): Single<ET> {
        // copy it so we won't have updated hash of params when saving it
        val param = params.copy()

        return networkCall(param)
            .subscribeOn(schedulers.network)
            .observeOn(schedulers.database)
            .doOnSuccess { saveItem(param, it) }
    }

    private fun saveItem(params: Params, item: ET) {
        databaseTxRunner.runInTransaction {
            entryDao.reset()

            item.params = params.toString()
            entryDao.insert(item)
        }
    }

    override fun refresh(params: Params): Completable = load(params).ignoreElement()

    protected abstract fun networkCall(params: Params): Single<ET>
}
