/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain

enum class CoverImageSize(val type: String, val maxSize: Int) {
    LARGE("large", 1200), MEDIUM("medium", 600), SMALL("small", 300)
}
