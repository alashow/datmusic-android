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
import tm.alashow.data.db.PaginatedItemEntryDao
import tm.alashow.domain.Optional
import tm.alashow.domain.PaginatedEntry
import tm.alashow.domain.Params
import tm.alashow.domain.errors.EmptyResultException

abstract class PaginatedItemCall<ET : PaginatedEntry, out ED : PaginatedItemEntryDao<Params, ET>>(
    private val databaseTxRunner: DatabaseTxRunner,
    private val entryDao: ED,
    private val schedulers: AppRxSchedulers
) : PaginatedCall<ET, ED>(databaseTxRunner, entryDao, schedulers), PaginatingItemCall<Params, ET> {

    override fun dataItem(params: Params): Flowable<ET> {
        return entryDao.entry(params.id())
            .distinctUntilChanged()
            .subscribeOn(schedulers.database)
    }

    override fun has(params: Params): Single<Boolean> {
        return entryDao.has(params.id())
            .subscribeOn(schedulers.database)
            .map { it == 0 }
    }

    private fun load(params: Params): Single<ET> {
        // copy it so we won't have updated hash of params when saving it
        val param = params.copy()

        return networkItemCall(param)
            .map { entry ->
                when (entry) {
                    is Optional.Some -> entry.value
                    is Optional.None -> throw EmptyResultException()
                }
            }
            .subscribeOn(schedulers.network)
            .observeOn(schedulers.database)
            .doOnSuccess { saveItem(param, it) }
    }

    private fun saveItem(params: Params, item: ET) {
        databaseTxRunner.runInTransaction {
            entryDao.delete(params.id())

            item.page = params.page
            item.params = params.toString()
            entryDao.insert(item)
        }
    }

    override fun refreshItem(params: Params): Completable = load(params).ignoreElement()

    protected abstract fun networkItemCall(params: Params): Single<Optional<ET>>
}
