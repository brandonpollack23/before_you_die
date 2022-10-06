package com.beforeyoudie.common.di

actual fun makeTestPlatformComponent(): BydPlatformComponent = BydPlatformComponent::class.create(DatabaseFileName())