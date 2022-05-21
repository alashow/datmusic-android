/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coVerify
import io.mockk.spyk
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.i18n.UiMessage

@HiltAndroidTest
class SnackbarManagerTest : BaseTest() {

    @Inject lateinit var snackbarManager: SnackbarManager

    private val testMessage = testMessage()
    private fun testMessage(
        text: String = "Test",
        action: SnackbarAction<Unit> = SnackbarAction(UiMessage.Plain("Test"), Unit)
    ) = SnackbarMessage<Unit>(
        message = UiMessage.Plain(text),
        action = action
    )

    @Test
    fun `addMessage adds a message to messages`() = runTest {
        snackbarManager.addMessage(testMessage)
        snackbarManager.messages.test {
            assertThat(awaitItem())
                .isEqualTo(testMessage)
        }
    }

    @Test
    fun `addMessage doesn't add a message to messages if same message added before existing one is dismissed`() = runTest {
        val secondMessage = testMessage("Test2")
        snackbarManager.messages.test {
            snackbarManager.addMessage(testMessage)
            snackbarManager.addMessage(testMessage) // this is ignored because of the same message and not dismissed yet
            snackbarManager.addMessage(secondMessage)

            assertThat(awaitItem())
                .isEqualTo(testMessage)
            assertThat(awaitItem())
                .isEqualTo(secondMessage)

            snackbarManager.onMessageDismissed(testMessage)
            snackbarManager.onMessageDismissed(secondMessage)

            snackbarManager.addMessage(testMessage)
            snackbarManager.onMessageDismissed(testMessage)
            snackbarManager.addMessage(testMessage) // this won't be ignored because same message was dismissed ^
            snackbarManager.addMessage(secondMessage)

            assertThat(awaitItem())
                .isEqualTo(testMessage)
            assertThat(awaitItem())
                .isEqualTo(testMessage)
            assertThat(awaitItem())
                .isEqualTo(secondMessage)
        }
    }

    @Test
    fun `observeMessageAction returns null when message is dismissed`() = runTest {
        snackbarManager.addMessage(testMessage)
        snackbarManager.onMessageDismissed(testMessage)

        val result = snackbarManager.observeMessageAction(testMessage)
        assertThat(result)
            .isNull()
    }

    @Test
    fun `observeMessageAction returns message when message is performed`() = runTest {
        snackbarManager.addMessage(testMessage)
        snackbarManager.onMessageActionPerformed(testMessage)

        val result = snackbarManager.observeMessageAction(testMessage)
        assertThat(result)
            .isEqualTo(testMessage)
    }

    @Test
    fun `observeMessageAction does not call onRetry message when message is dismissed`() = runTest {
        snackbarManager.addMessage(testMessage)
        snackbarManager.onMessageDismissed(testMessage)

        val onRetry: () -> Unit = spyk()
        snackbarManager.observeMessageAction(testMessage, onRetry)
        coVerify(exactly = 0) { onRetry() }
    }

    @Test
    fun `observeMessageAction calls onRetry message when message is performed`() = runTest {
        snackbarManager.addMessage(testMessage)
        snackbarManager.onMessageActionPerformed(testMessage)

        val onRetry: () -> Unit = spyk()
        snackbarManager.observeMessageAction(testMessage, onRetry)
        coVerify { onRetry() }
    }
}
