package com.beforeyoudie

import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.DatabaseFileName

// TODO NOW reenable this just to get platform
actual fun makeTestPlatformComponent(): BydPlatformComponent = BydPlatformComponent::class.create(DatabaseFileName())