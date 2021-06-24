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
import tm.alashow.data.db.EntryDao
import tm.alashow.domain.Entry
import tm.alashow.domain.Params
import tm.alashow.domain.errors.EmptyResultException

abstract class PersistedListCall<ET : Entry, out ED : EntryDao<Params, ET>>(
    private val databaseTxRunner: DatabaseTxRunner,
    private val entryDao: ED,
    private val schedulers: AppRxSchedulers
) : ListCall<Params, ET> {

    override fun data(params: Params): Flowable<List<ET>> {
        return entryDao.entries(params)
            .distinctUntilChanged()
            .subscribeOn(schedulers.database)
    }

    override fun isEmpty(params: Params): Single<Boolean> {
        return entryDao.count(params)
            .subscribeOn(schedulers.database)
            .map { it == 0 }
    }

    private fun load(params: Params): Single<List<ET>> {
        // copy it so we won't have updated hash of params when saving it
        val param = params.copy()

        return networkCall(param)
            .subscribeOn(schedulers.network)
            .doOnSuccess {
                if (it.isEmpty()) {
                    throw EmptyResultException()
                }
            }
            .observeOn(schedulers.database)
            .doOnSuccess { save(param, it) }
    }

    private fun save(params: Params, items: List<ET>) {
        databaseTxRunner.runInTransaction {
            entryDao.deleteAll()

            items.forEach { item ->
                item.params = params.toString()
                entryDao.insert(item)
            }
        }
    }

    override fun refresh(params: Params): Completable = load(params).ignoreElement()

    protected abstract fun networkCall(params: Params): Single<List<ET>>
}
