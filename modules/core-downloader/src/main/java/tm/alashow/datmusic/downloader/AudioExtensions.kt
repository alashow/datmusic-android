/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.Audio

private val MULTIPLE_ARTIST_SPLIT_REGEX = Regex("((,)|(feat\\.)|(ft\\.))")

val filenameIllegalChars = setOf('|', '/', '\\', '?', '*', '<', '>', '"', ':')
private fun String.cleanIllegalChars(chars: Set<Char> = filenameIllegalChars, replacement: Char = '_') =
    map { if (it in chars) replacement else it }.joinToString("")

fun DocumentFile.getOrCreateDir(name: String) = findFile(name.cleanIllegalChars())
    ?: createDirectory(name.cleanIllegalChars())
    ?: error("Couldn't create folder:$name")

fun Audio.artists() = artist.split(MULTIPLE_ARTIST_SPLIT_REGEX, 10).map { it.trim() }
fun Audio.mainArtist() = artist.split(MULTIPLE_ARTIST_SPLIT_REGEX, 10).first().trim()

private fun Audio.createDocumentFile(parent: DocumentFile) = parent.createFile(fileMimeType(), fileDisplayName().cleanIllegalChars())
    ?: error("Couldn't create document file")

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
