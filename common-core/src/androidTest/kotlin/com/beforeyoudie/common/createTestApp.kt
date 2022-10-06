package com.beforeyoudie.common

import android.content.Context
import com.beforeyoudie.common.di.ApplicationScope
import com.beforeyoudie.common.di.BydPlatformInjectComponent
import io.mockk.mockkClass
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class TestAppComponent : CommonTestComponent(BydPlatformInjectComponent::class.create("")) {
  @Provides
  @ApplicationScope
  fun provideContext(): Context = mockkClass(Context::class)
}

actual fun createTestApp(): CommonTestComponent = TestAppComponent::class.create()
