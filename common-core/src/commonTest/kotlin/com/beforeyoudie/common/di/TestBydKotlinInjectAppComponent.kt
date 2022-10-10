package com.beforeyoudie.common.di

import me.tatarka.inject.annotations.Component

/**
 * Test version of CommonBydKotlinInjectAppComponent, use this in tests and override components
 * etc. as necessary.  Shouldn't use this directly but extend it in your test, exposing the
 * dependencies and test versions of classes you need access to.
 */
@ApplicationScope
@Component
abstract class TestBydKotlinInjectAppComponent(
  @Component val platformComponent: BydPlatformComponent = makeTestPlatformComponent()
) :
  ICommonBydKotlinInjectAppComponent

/** Construct test platform component helper, this will provide mocks etc. per platform. */
expect fun makeTestPlatformComponent(): BydPlatformComponent
