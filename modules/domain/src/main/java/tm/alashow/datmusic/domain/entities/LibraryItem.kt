/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

typealias LibraryItems = List<LibraryItem>
interface LibraryItem {
    fun getLabel(): String
}
