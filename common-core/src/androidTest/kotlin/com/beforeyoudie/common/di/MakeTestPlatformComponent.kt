package com.beforeyoudie.common.di

import android.content.Context
import io.mockk.mockkClass

actual fun makeTestPlatformComponent(): BydPlatformComponent =
  BydPlatformComponent::class.create(mockkClass(Context::class), DatabaseFileName())
