/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import org.threeten.bp.Duration
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.base.util.extensions.delayFlow
import tm.alashow.i18n.UiMessage

data class SnackbarAction<T>(val label: UiMessage<*>, val argument: T)
open class SnackbarMessage<T>(val message: UiMessage<*>, val action: SnackbarAction<T>? = null)

@Singleton
class SnackbarManager @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
) {
    private var maxDuration = Duration.ofSeconds(6).toMillis()
    private val maxQueue = 3

    private val pendingErrors = Channel<Throwable>(maxQueue, BufferOverflow.DROP_OLDEST)
    private val removeErrorSignal = Channel<Unit>(Channel.RENDEZVOUS)

    /**
     * A flow of [Throwable]s to display in the UI, usually as snackbars. The flow will immediately
     * emit `null`, and will then emit errors sent via [addError]. Once [maxDuration] has elapsed,
     * or [removeCurrentError] is called (if before that) `null` will be emitted to remove
     * the current error.
     */
    val errors: Flow<Throwable?> = channelFlow {
        send(null)

        pendingErrors.receiveAsFlow().collectLatest {
            send(it)

            // Wait for either a maxDuration timeout, or a remove signal (whichever comes first)
            merge(
                delayFlow(maxDuration, Unit),
                removeErrorSignal.receiveAsFlow(),
            ).firstOrNull()

            // Remove the error
            send(null)
        }
    }

    /**
     * Add [error] to the queue of errors to display.
     */
    suspend fun addError(error: Throwable) {
        pendingErrors.send(error)
    }

    /**
     * Remove the current error from being displayed.
     */
    suspend fun removeCurrentError() {
        removeErrorSignal.send(Unit)
    }

    private val messagesChannel = Channel<SnackbarMessage<*>>(Channel.CONFLATED)
    private val performedActionsMessageChannel = Channel<SnackbarMessage<*>>(Channel.CONFLATED)

    val messages = messagesChannel.receiveAsFlow()

    fun addMessage(message: UiMessage<*>) = addMessage(SnackbarMessage<Unit>(message))

    fun addMessage(message: SnackbarMessage<*>) {
        messagesChannel.trySend(message)
    }

    fun onMessageActionPerformed(message: SnackbarMessage<*>) {
        performedActionsMessageChannel.trySend(message)
    }

    /**
     * Listen for [performedActionsMessageChannel] for given [action] for limited time.
     * Returns given action if it's performed on time, null otherwise.
     */
    suspend fun <T : SnackbarMessage<*>> observeMessageAction(action: T): T? {
        val result = merge(performedActionsMessageChannel.receiveAsFlow().filter { it == action }, delayFlow(4000L, Unit))
            .firstOrNull()
        return if (result == action) action else null
    }
}
