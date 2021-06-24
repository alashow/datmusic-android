/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.base.vm

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 * Simple ViewModel which exposes a CompositeDisposable which is automatically cleared when
 * the ViewModel is cleared.
 */
open class RxAwareViewModel : ViewModel() {

    val disposables = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
