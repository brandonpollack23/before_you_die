package com.beforeyoudie.common

import com.beforeyoudie.common.di.ApplicationScope
import com.beforeyoudie.common.di.BydPlatformInjectComponent
import me.tatarka.inject.annotations.Component

@Component
@ApplicationScope
abstract class TestAppComponent : CommonTestComponent(BydPlatformInjectComponent::class.create(""))

actual fun createTestApp(): CommonTestComponent = TestAppComponent::class.create()