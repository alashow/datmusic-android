/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import io.reactivex.Completable
import io.reactivex.Single
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.domain.Entry
import tm.alashow.domain.Params

abstract class RoomRepo<E : Entry>(
    private val dao: EntryDao<Params, E>,
    private val schedulers: AppRxSchedulers
) {
    fun entries(params: Params) = dao.entries(params)
        .distinctUntilChanged()
        .subscribeOn(schedulers.database)

    fun isEmpty(params: Params): Single<Boolean> {
        return dao.count(params)
            .subscribeOn(schedulers.database)
            .map { it == 0 }
    }

    fun entry(id: Long) = dao.entry(id).subscribeOn(schedulers.database)
    fun delete(id: Long) = Completable.fromCallable { dao.delete(id) }.subscribeOn(schedulers.database)

    fun insert(item: E) = Single.fromCallable { dao.insert(item) }.subscribeOn(schedulers.database)
    fun insert(items: List<E>) = Single.fromCallable { dao.insert(items) }.subscribeOn(schedulers.database)
    fun update(item: E) = Completable.fromCallable { dao.update(item) }.subscribeOn(schedulers.database)

    fun clear() = Completable.fromCallable { dao.deleteAll() }.subscribeOn(schedulers.database)
}
