package com.beforeyoudie.common.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
actual fun createTestPlatformComponent(): BydPlatformComponent {
  val applicationCoroutineContext: ApplicationCoroutineContext =
    StandardTestDispatcher(name = "MainCoroutineContext")
  val ioCoroutineContext: IOCoroutineContext = StandardTestDispatcher(name = "IoCoroutineContext")

  return JvmDesktopPlatformComponent::class.create(applicationCoroutineContext, ioCoroutineContext)
}
