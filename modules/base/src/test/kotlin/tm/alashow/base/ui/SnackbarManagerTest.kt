/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.i18n.UiMessage

@HiltAndroidTest
class SnackbarManagerTest : BaseTest() {

    @Inject lateinit var snackbarManager: SnackbarManager

    private val testError = Throwable("Test")

    private val testMessage = testMessage()
    private fun testMessage(text: String = "Test") = SnackbarMessage<Unit>(
        message = UiMessage.Plain(text),
        action = null
    )

    @Test
    fun `addError emits error and removes when removeCurrentError is called`() = runTest {
        snackbarManager.errors.test {
            snackbarManager.addError(testError)

            assertThat(awaitItem())
                .isNull() // it always starts with no errors

            assertThat(awaitItem())
                .isEqualTo(testError)

            // emulate user dismissing the error
            snackbarManager.removeCurrentError()

            assertThat(awaitItem())
                .isNull() // test error should be dismissed by now
        }
    }

//    TODO: fix this test, advanceTimeBy doesn't work (even when passing test dispatchers to delay in delayFlow)
//    @Test
//    fun `addError emits error and removes when return error duration is elapsed`() = runTest(dispatchTimeoutMs = 100000) {
//        snackbarManager.errors.test(100000) {
//            val errorDuration = snackbarManager.addError(testError)
//
//            assertThat(awaitItem())
//                .isNull() // it always starts with no errors
//
//            assertThat(awaitItem())
//                .isEqualTo(testError)
//
//            advanceTimeBy(errorDuration)
//
//            assertThat(awaitItem())
//                .isNull() // test error should be dismissed by now
//        }
//    }

    @Test
    fun `addMessage adds a message to messages`() = runTest {
        snackbarManager.addMessage(testMessage)
        snackbarManager.messages.test {
            assertThat(awaitItem())
                .isEqualTo(testMessage)
        }
    }

    @Test
    fun `addMessage doesn't add a message to messages if same message added before it's duration expired`() = runTest {
        val secondMessage = testMessage("Test2")
        snackbarManager.messages.test {
            snackbarManager.addMessage(testMessage)
            snackbarManager.addMessage(testMessage)
            snackbarManager.addMessage(secondMessage)

            assertThat(awaitItem())
                .isEqualTo(testMessage)
            assertThat(awaitItem())
                .isEqualTo(secondMessage)

            snackbarManager.onMessageDismissed(testMessage)
            snackbarManager.onMessageDismissed(secondMessage)

            snackbarManager.addMessage(testMessage)
            snackbarManager.onMessageDismissed(testMessage)
            snackbarManager.addMessage(testMessage)
            snackbarManager.addMessage(secondMessage)

            assertThat(awaitItem())
                .isEqualTo(testMessage)
            assertThat(awaitItem())
                .isEqualTo(testMessage)
            assertThat(awaitItem())
                .isEqualTo(secondMessage)
        }
    }
}
