package com.beforeyoudie.common.di

import me.tatarka.inject.annotations.Component

@ApplicationScope
@Component
abstract class TestBydKotlinInjectAppComponent(@Component val platformComponent: BydPlatformComponent = makeTestPlatformComponent()) :
  ICommonBydKotlinInjectAppComponent

expect fun makeTestPlatformComponent(): BydPlatformComponent
