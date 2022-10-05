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
import org.jetbrains.annotations.VisibleForTesting
import tm.alashow.base.R
import tm.alashow.i18n.UiMessage

@Singleton
class SnackbarManager @Inject constructor() {

    private val messagesChannel = Channel<SnackbarMessage<*>>(Channel.CONFLATED)
    private val actionDismissedMessageChannel = Channel<SnackbarMessage<*>>(Channel.CONFLATED)
    private val actionPerformedMessageChannel = Channel<SnackbarMessage<*>>(Channel.CONFLATED)

    val messages = messagesChannel.receiveAsFlow()
    private val shownMessages = mutableSetOf<UiMessage<*>>()

    /**
     * Shows given [UiMessage] as a snackbar. If the same [message] is already being shown, it will not be shown again.
     * [message] must be dismissed it can be shown again.
     * @param message UiMessage to show
     */
    fun addMessage(message: UiMessage<*>) = addMessage(SnackbarMessage<Unit>(message))

    /**
     * Shows given [SnackbarMessage] as a snackbar.
     * If the same SnackbarMessage's [snackbarMessage].message is already being shown, it will not be shown again.
     * [snackbarMessage] must be dismissed or it's action performed before it can be shown again.
     * @param snackbarMessage the message to show
     */
    fun addMessage(snackbarMessage: SnackbarMessage<*>) {
        if (snackbarMessage.message !in shownMessages) {
            messagesChannel.trySend(snackbarMessage)
            shownMessages.add(snackbarMessage.message)
        }
    }

    /**
     * Adds the given [error] as a snackbar message with a retry action.
     * @param error the error to show
     * @param retryLabel the label for the retry action, defaults to "Retry"
     * @param onRetry callback to perform when the retry action performed
     */
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

    /**
     * Dismisses the given [message].
     */
    fun onMessageDismissed(message: SnackbarMessage<*>) {
        shownMessages.remove(message.message)
        actionDismissedMessageChannel.trySend(message)
    }

    /**
     * Marks the action on given [message] as performed.
     */
    fun onMessageActionPerformed(message: SnackbarMessage<*>) {
        shownMessages.remove(message.message)
        actionPerformedMessageChannel.trySend(message)
    }

    /**
     * Observe the action to be performed on the [message], until the action is performed or the message is dismissed.
     * @param message the message to observe
     * @return the given message if the action was performed, null if the message was dismissed
     */
    @VisibleForTesting
    internal suspend fun <T : SnackbarMessage<*>> observeMessageAction(message: T): T? {
        val result = merge(
            actionDismissedMessageChannel.receiveAsFlow().filter { it == message }.map { null }, // map to null because it's dismissed
            actionPerformedMessageChannel.receiveAsFlow().filter { it == message },
        ).firstOrNull()
        return if (result == message) message else null
    }

    /**
     * Observe the action to be performed on the [message], until the action is performed or the message is dismissed.
     * @param message the message to observe
     * @param onAction callback to perform when the message action is performed
     */
    suspend fun <T : SnackbarMessage<*>> observeMessageAction(message: T, onAction: () -> Unit) {
        if (observeMessageAction(message) != null) onAction()
    }
}
