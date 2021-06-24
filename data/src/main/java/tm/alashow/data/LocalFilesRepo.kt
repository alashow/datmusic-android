/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data

import android.content.Context
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.domain.models.Optional
import tm.alashow.domain.models.some

open class LocalFilesRepo @Inject constructor(private val context: Context, private val dispatchers: CoroutineDispatchers) {

    var suffix: String = ""

    private fun getFile(name: String) = File(context.filesDir, "$name.$suffix")

    suspend fun read(name: String): Optional<String> = withContext(dispatchers.io) {
        val file = getFile(name)
        val data = when (file.exists()) {
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
        data
    }

    suspend fun save(name: String, data: String) = withContext(dispatchers.io) {
        with(getFile(name)) {
            delete()
            bufferedWriter().use {
                it.write(data)
            }
        }
    }
}
