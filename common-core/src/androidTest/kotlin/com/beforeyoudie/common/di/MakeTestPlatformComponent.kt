package com.beforeyoudie.common.di

import android.content.Context
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
actual fun makeTestPlatformComponent(): BydPlatformComponent =
  BydPlatformComponent::class.create(
    mockkClass(Context::class),
    DatabaseFileName(),
    StandardTestDispatcher()
  )
