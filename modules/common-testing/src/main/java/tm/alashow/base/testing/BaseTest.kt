/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.testing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.robolectric.annotation.Config

@Config(application = HiltTestApplication::class, manifest = Config.NONE)
@RunWith(AndroidJUnit4::class)
abstract class BaseTest {
    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule by lazy { HiltAndroidRule(this) }

    @get:Rule(order = 1)
    val mockitoRule: MockitoRule by lazy { MockitoJUnit.rule() }

    @get:Rule(order = 2)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    open fun setUp() {
        hiltRule.inject()
    }
}

abstract class BaseComposeTest : BaseTest() {
    @get:Rule(order = 3)
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    fun stringRes(resId: Int) = context.getString(resId)
    fun stringRes(resId: Int, vararg formatArgs: Any) = context.getString(resId, *formatArgs)
}
