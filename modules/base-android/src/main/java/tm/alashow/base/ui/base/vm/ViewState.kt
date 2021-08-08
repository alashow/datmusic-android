/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.base.vm

interface ViewState

open class TypedViewState<out V>(val value: V) : ViewState
