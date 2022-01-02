/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import android.content.ContentResolver
import android.content.Context
import androidx.documentfile.provider.DocumentFile
import androidx.test.core.app.ApplicationProvider
import java.io.File

fun createTestDownloadsLocation(): Pair<ContentResolver, DocumentFile> {
    val context: Context = ApplicationProvider.getApplicationContext()
    val folder = File(context.filesDir, "Downloads")
    folder.mkdir()
    val documentFile = DocumentFile.fromFile(folder).apply {
        assert(isDirectory)
        assert(exists())
    }
    return context.contentResolver to documentFile
}
