/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

const val DATMUSIC_FIRST_PAGE_INDEX = 0

@Parcelize
data class CaptchaSolution(
    val captchaId: Long,
    val captchaIndex: Int,
    val captchaKey: String,
) : Parcelable {
    companion object {
        fun CaptchaSolution.toQueryMap() = mapOf(
            "captcha_id" to captchaId,
            "captcha_index" to captchaIndex,
            "captcha_key" to captchaKey
        )
    }
}
