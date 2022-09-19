/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import tm.alashow.base.R
import tm.alashow.i18n.UiMessage

data class SnackbarAction<T>(val label: UiMessage<*>, val argument: T)
open class SnackbarMessage<T>(val message: UiMessage<*>, val action: SnackbarAction<T>? = null)

@Singleton
class SnackbarManager @Inject constructor() {

    private val messagesChannel = Channel<SnackbarMessage<*>>(Channel.CONFLATED)
    private val actionDismissedMessageChannel = Channel<SnackbarMessage<*>>(Channel.CONFLATED)
    private val actionPerformedMessageChannel = Channel<SnackbarMessage<*>>(Channel.CONFLATED)

    val messages = messagesChannel.receiveAsFlow()
    private val shownMessages = mutableSetOf<UiMessage<*>>()

    suspend fun addError(
        error: Throwable,
        retryLabel: UiMessage<*> = UiMessage.Resource(R.string.error_retry),
        onRetry: () -> Unit
    ) {
        val action = SnackbarAction(retryLabel, onRetry)
        val message = SnackbarMessage(UiMessage.Error(error), action)
        addMessage(SnackbarMessage(UiMessage.Error(error), action))

        observeMessageAction(message, onRetry)
    }

    fun addMessage(message: UiMessage<*>) = addMessage(SnackbarMessage<Unit>(message))

    fun addMessage(message: SnackbarMessage<*>) {
        if (message.message !in shownMessages) {
            messagesChannel.trySend(message)
            shownMessages.add(message.message)
        }
    }

    fun onMessageDismissed(message: SnackbarMessage<*>) {
        shownMessages.remove(message.message)
        actionDismissedMessageChannel.trySend(message)
    }

    fun onMessageActionPerformed(message: SnackbarMessage<*>) {
        shownMessages.remove(message.message)
        actionPerformedMessageChannel.trySend(message)
    }

    /**
     * Listen for [actionPerformedMessageChannel] for given [message] for limited time.
     * Returns given action if it's performed on time, null otherwise.
     */
    suspend fun <T : SnackbarMessage<*>> observeMessageAction(message: T): T? {
        val result = merge(
            actionDismissedMessageChannel.receiveAsFlow().filter { it == message }.map { null }, // map to null because it's dismissed
            actionPerformedMessageChannel.receiveAsFlow().filter { it == message },
        ).firstOrNull()
        return if (result == message) message else null
    }

    suspend fun <T : SnackbarMessage<*>> observeMessageAction(message: T, onAction: () -> Unit) {
        if (observeMessageAction(message) != null) {
            onAction()
        }
    }
}
