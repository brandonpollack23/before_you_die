package com.beforeyoudie.common

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import com.beforeyoudie.common.di.ApplicationScope
import com.beforeyoudie.common.di.BydKotlinInjectAppComponent
import com.beforeyoudie.common.di.BydPlatformInjectComponent
import com.beforeyoudie.common.di.IBydKotlinInjectAppComponent
import com.beforeyoudie.common.di.kotlinInjectCreateApp
import com.beforeyoudie.common.storage.IBydStorage
import io.kotest.core.Tuple2
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import me.tatarka.inject.annotations.Component

// TODO NOW remove koin and move this and sqldelight test to kotlin-inject

abstract class CommonTest : FunSpec() {
  protected lateinit var app: CommonTestComponent

  init {
    beforeTest {
      // TODO NOW FIX ME
      app = TestBydKotlinInjectAppComponent()
      Logger.setLogWriters(CommonWriter())
    }
    afterTest {}
  }

  final override fun afterTest(f: suspend (Tuple2<TestCase, TestResult>) -> Unit) =
    super.afterTest(f)
  final override fun beforeTest(f: suspend (TestCase) -> Unit) = super.beforeTest(f)
}

@ApplicationScope
@Component
abstract class TestBydKotlinInjectAppComponent(@Component platformInjectComponent: BydPlatformInjectComponent) :
  IBydKotlinInjectAppComponent {
  abstract val storage: IBydStorage
}