/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.utils.extensions

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContract

fun Activity.toggleKeyboard(show: Boolean, view: View) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (show) {
        inputMethodManager.showSoftInput(currentFocus ?: view, InputMethodManager.SHOW_FORCED)
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    } else {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}

class CompleteTask : ActivityResultContract<Intent, Boolean>() {
    override fun createIntent(context: Context, input: Intent): Intent {
        return input
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean = resultCode == RESULT_OK
}
