/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos

const val DATMUSIC_FIRST_PAGE_INDEX = 0

data class CaptchaSolution(
    val captchaId: Long,
    val captchaIndex: Int,
    val captchaKey: String,
) {
    companion object {
        fun CaptchaSolution.toQueryMap() = mapOf(
            "captcha_id" to captchaId,
            "captcha_index" to captchaIndex,
            "captcha_key" to captchaKey
        )
    }
}
