package com.beforeyoudie.common.di

import android.content.Context
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
actual fun createTestPlatformComponent(): BydPlatformComponent {
  val applicationCoroutineContext: ApplicationCoroutineContext =
    StandardTestDispatcher(name = "MainCoroutineContext")
  val ioCoroutineContext: IOCoroutineContext = StandardTestDispatcher(name = "IoCoroutineContext")
  val mockContext = mockkClass(Context::class)

  return AndroidBydPlatformComponent::class.create(
    mockContext,
    applicationCoroutineContext,
    ioCoroutineContext
  )
}
