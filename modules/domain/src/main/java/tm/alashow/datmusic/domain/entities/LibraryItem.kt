/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

typealias LibraryItems = List<LibraryItem>

interface LibraryItem {
    val isUpdatable get() = true
    val isDeletable get() = true
    val isDownloadable get() = false

    fun getLabel(): String
    fun getIdentifier(): String
}
