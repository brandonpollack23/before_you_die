package com.beforeyoudie

import android.content.Context
import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.DatabaseFileName
import io.mockk.mockkClass

actual fun makeTestPlatformComponent(): BydPlatformComponent =
  BydPlatformComponent::class.create(mockkClass(Context::class), DatabaseFileName())
