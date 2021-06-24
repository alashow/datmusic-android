/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data

import android.content.Context
import tm.alashow.domain.Optional
import tm.alashow.domain.some
import java.io.File
import javax.inject.Inject

open class LocalFilesRepo @Inject constructor(private val context: Context) {

    var suffix: String = ""

    private fun getFile(name: String) = File(context.filesDir, "$name.$suffix")

    suspend fun read(name: String): Optional<String>  {
        val file = getFile(name)
        return when (file.exists()) {
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
    }

    suspend fun save(name: String, data: String) {
        with(getFile(name)) {
            delete()
            bufferedWriter().use {
                it.write(data)
            }
        }
    }
}
