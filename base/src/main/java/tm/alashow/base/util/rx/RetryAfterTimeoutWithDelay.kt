/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.rx

import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit

class RetryAfterTimeoutWithDelay(
    private val maxRetries: Int,
    private val delay: Long,
    private val shouldRetry: (Throwable) -> Boolean,
    private val scheduler: Scheduler
) : Function<Flowable<out Throwable>, Flowable<*>> {
    private var retryCount = 0

    override fun apply(t: Flowable<out Throwable>): Flowable<*> {
        return t.flatMap {
            if (++retryCount < maxRetries && shouldRetry(it)) {
                Flowable.timer(delay * retryCount, TimeUnit.MILLISECONDS, scheduler)
            } else {
                Flowable.error(it)
            }
        }
    }
}
