package com.beforeyoudie

import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.DatabaseFileName

actual fun makeTestPlatformComponent(): BydPlatformComponent = BydPlatformComponent::class.create(DatabaseFileName())