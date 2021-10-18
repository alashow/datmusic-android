/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.i18n

import android.content.res.Resources

interface TextCreator<Params> {
    fun Params.localize(resources: Resources): String
}
