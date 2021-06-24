/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.base.vm

import androidx.lifecycle.LiveData
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import tm.alashow.base.util.arch.LiveEvent
import tm.alashow.base.util.arch.asEvent
import tm.alashow.base.util.extensions.asFlowable
import tm.alashow.base.util.extensions.asLive

abstract class BaseViewModel<MT : Any> : RxAwareViewModel() {

    protected val stateSubject = BehaviorSubject.create<MT>()

    val viewState: LiveData<MT> = stateSubject.asFlowable().asLive()

    val state: MT?
        get() = stateSubject.value

    protected fun state(message: MT) = stateSubject.onNext(message)

    val liveError = LiveEvent<Throwable?>()
    protected fun postError(error: Throwable?) {
        Timber.e(error, "Posted error in Base")
        liveError.postValue(error.asEvent())
    }

    protected fun unknownError() = postError(Throwable("Unknown error"))
}
