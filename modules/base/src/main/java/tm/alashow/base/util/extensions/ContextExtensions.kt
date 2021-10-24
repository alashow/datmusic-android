/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.extensions

import android.content.Context
import android.net.Uri

fun Context.writeToFile(data: ByteArray, output: Uri) {
    runCatching {
        val outputStream = contentResolver.openOutputStream(output) ?: error("Failed to open output file stream")
        outputStream.write(data)
    }.onFailure {
        error("Failed to write to file: $output")
    }
}

fun Context.readFromFile(input: Uri): String {
    val inputStream = contentResolver.openInputStream(input) ?: error("Failed to open input file stream")
    return inputStream.bufferedReader().readText()
}
