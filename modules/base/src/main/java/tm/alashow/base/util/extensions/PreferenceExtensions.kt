/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.extensions

import android.content.SharedPreferences

inline fun SharedPreferences.edit(preferApply: Boolean = false, f: SharedPreferences.Editor.() -> Unit) {
    val editor = edit()
    editor.f()
    if (preferApply) editor.apply() else editor.commit()
}
