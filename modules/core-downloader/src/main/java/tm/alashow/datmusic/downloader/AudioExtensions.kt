/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaExtractor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException
import timber.log.Timber
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.AudioHeader

val filenameIllegalChars = setOf('|', '/', '\\', '?', '*', '<', '>', '"', ':')
private fun String.cleanIllegalChars(chars: Set<Char> = filenameIllegalChars, replacement: Char = '_') =
    map { if (it in chars) replacement else it }.joinToString("")

fun Uri.toDocumentFile(context: Context) = when (scheme) {
    "file" -> DocumentFile.fromFile(toFile())
    else -> DocumentFile.fromTreeUri(context, this)
} ?: error("Couldn't resolve uri to document file")

fun DocumentFile.getOrCreateDir(name: String) = findFile(name.cleanIllegalChars())
    ?: createDirectory(name.cleanIllegalChars())
    ?: error("Couldn't create folder:$name")

fun Audio.createDocumentFile(parent: DocumentFile): DocumentFile {
    var newFile = parent.createFile(fileMimeType(), fileDisplayName().cleanIllegalChars())
    // normal saf would return new file name if file already existed,
    // so we need to have similar behavior for raw files (document files opened via DocumentFile.fromFile)
    if (newFile == null && parent.uri.scheme == "file") {
        newFile = parent.listFiles().find { it.name?.startsWith(fileDisplayName()) == true }
    }
    return newFile ?: error("Couldn't create document file")
}

fun Audio.documentFile(parent: DocumentFile, songsGrouping: DownloadsSongsGrouping): DocumentFile {
    if (!parent.exists())
        throw FileNotFoundException("Parent folder doesn't exist")
    return when (songsGrouping) {
        DownloadsSongsGrouping.Flat -> createDocumentFile(parent)
        DownloadsSongsGrouping.ByArtist -> {
            val mainArtist = mainArtist()
            val artistFolder = parent.getOrCreateDir(mainArtist)
            createDocumentFile(artistFolder)
        }
        DownloadsSongsGrouping.ByAlbum -> {
            val mainArtist = mainArtist()
            val artistFolder = parent.getOrCreateDir(mainArtist)
            val albumName = album ?: ""

            when (albumName.isBlank()) {
                true -> createDocumentFile(artistFolder)
                else -> {
                    val albumFolder = artistFolder.getOrCreateDir(albumName)
                    createDocumentFile(albumFolder)
                }
            }
        }
    }
}

/**
 * Tries to get bitmap from downloaded audio file.
 * Depends on [Audio.audioDownloadItem] already being there
 */
fun Audio.artworkFromFile(context: Context): Bitmap? {
    try {
        val downloadInfo = audioDownloadItem?.downloadInfo ?: return null

        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(context, downloadInfo.fileUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                return metadataRetriever.primaryImage
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        val data = metadataRetriever.embeddedPicture
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.size)
        }
        return null
    } catch (e: Exception) {
        Timber.e(e)
    }
    return null
}

fun AudioDownloadItem.audioHeader(context: Context): AudioHeader {
    try {
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(context, downloadInfo.fileUri, null)
        return AudioHeader.from(this, mediaExtractor.getTrackFormat(0))
    } catch (e: Exception) {
        Timber.e(e)
    }
    return AudioHeader()
}
