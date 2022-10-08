package com.beforeyoudie.common.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
actual fun makeTestPlatformComponent(): BydPlatformComponent = BydPlatformComponent::class.create(
  DatabaseFileName(),
  StandardTestDispatcher()
)
