/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

enum class CoverImageSize(val type: String, val maxSize: Int) {
    LARGE("large", Int.MAX_VALUE), MEDIUM("medium", 600), SMALL("small", 300)
}
