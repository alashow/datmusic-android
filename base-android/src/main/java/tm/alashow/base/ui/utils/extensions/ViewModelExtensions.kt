/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.utils.extensions

import androidx.lifecycle.SavedStateHandle

inline fun <reified T : Any> SavedStateHandle.require(key: String): T = requireNotNull(get(key))
