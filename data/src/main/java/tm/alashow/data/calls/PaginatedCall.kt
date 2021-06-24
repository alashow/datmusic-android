/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.calls

import androidx.paging.DataSource
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.data.db.DatabaseTxRunner
import tm.alashow.data.db.PaginatedEntryDao
import tm.alashow.domain.PaginatedEntry
import tm.alashow.domain.Params
import tm.alashow.domain.errors.EmptyResultException

abstract class PaginatedCall<ET : PaginatedEntry, out ED : PaginatedEntryDao<Params, ET>>(
    private val databaseTxRunner: DatabaseTxRunner,
    private val entryDao: ED,
    private val schedulers: AppRxSchedulers,
    override val pageSize: Int = 21
) : PaginationCall<Params, ET> {

    override fun data(params: Params): Flowable<List<ET>> {
        return entryDao.entries(params)
            .distinctUntilChanged()
            .subscribeOn(schedulers.database)
    }

    override fun dataPage(params: Params): Flowable<List<ET>> {
        return entryDao.entriesPage(params, params.page)
            .subscribeOn(schedulers.database)
            .distinctUntilChanged()
    }

    override fun isEmpty(params: Params): Single<Boolean> {
        return entryDao.count(params)
            .subscribeOn(schedulers.database)
            .map { it == 0 }
    }

    override fun dataSourceFactory(params: Params): DataSource.Factory<Int, ET> = entryDao.entriesDataSource(params)

    private fun loadPage(params: Params, resetOnSave: Boolean = false): Single<List<ET>> {
        // copy it so we won't have updated hash of params when saving it
        val param = params.copy()

        return networkCall(param)
            .subscribeOn(schedulers.network)
            .doOnSuccess {
                if (it.isEmpty()) {
                    // save page anyways so old data will be removed
                    savePage(param, it, resetOnSave)
                    throw EmptyResultException()
                }
            }
            .observeOn(schedulers.database)
            .doOnSuccess { savePage(param, it, resetOnSave) }
    }

    private fun savePage(params: Params, items: List<ET>, resetOnSave: Boolean) {
        databaseTxRunner.runInTransaction {
            when {
                resetOnSave -> entryDao.deleteAll()
                else -> entryDao.deletePage(params, params.page)
            }
            items.forEach { item ->
                item.page = params.page
                item.params = params.toString()
                entryDao.insert(item)
            }
        }
    }

    override fun loadPage(params: Params): Completable = loadPage(params, false).ignoreElement()

    override fun loadNextPage(params: Params): Completable = loadPage(params.increment())

    override fun refresh(params: Params): Completable {
        params.reset()
        return loadPage(params, true).ignoreElement()
    }

    protected abstract fun networkCall(params: Params): Single<List<ET>>
}
