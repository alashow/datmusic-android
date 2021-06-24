/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data

import android.content.Context
import io.reactivex.Single
import java.io.File
import javax.inject.Inject
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.domain.Optional
import tm.alashow.domain.some

open class LocalFilesRepo @Inject constructor(private val context: Context, private val schedulers: AppRxSchedulers) {

    var suffix: String = ""

    private fun getFile(name: String) = File(context.filesDir, "$name.$suffix")

    fun read(name: String): Single<Optional<String>> = Single.fromCallable {
        val file = getFile(name)
        when (file.exists()) {
            true -> {
                val data = file.bufferedReader(Charsets.UTF_8).readText()
                when (data.isBlank()) {
                    true -> Optional.None
                    else -> {
                        some(data)
                    }
                }
            }
            else -> Optional.None
        }
    }.subscribeOn(schedulers.io)

    fun save(name: String, data: String): Single<Boolean> = Single.fromCallable {
        with(getFile(name)) {
            delete()
            bufferedWriter().use {
                it.write(data)
            }
        }
        true
    }.subscribeOn(schedulers.io)
}
