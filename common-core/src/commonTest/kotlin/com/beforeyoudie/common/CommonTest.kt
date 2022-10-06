package com.beforeyoudie.common

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import com.beforeyoudie.common.di.BydKotlinInjectComponent
import com.beforeyoudie.common.di.BydPlatformInjectComponent
import com.beforeyoudie.common.storage.IBydStorage
import io.kotest.core.Tuple2
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

// TODO now remove koin and move this and sqldelight test to kotlin-inject

abstract class CommonTest : FunSpec() {
  protected lateinit var app: CommonTestComponent

  init {
    beforeTest {
      app = createTestApp()
      Logger.setLogWriters(CommonWriter())
    }
    afterTest {}
  }

  final override fun afterTest(f: suspend (Tuple2<TestCase, TestResult>) -> Unit) =
    super.afterTest(f)
  final override fun beforeTest(f: suspend (TestCase) -> Unit) = super.beforeTest(f)
}

abstract class CommonTestComponent(platformInjectComponent: BydPlatformInjectComponent) :
  BydKotlinInjectComponent(platformInjectComponent) {
  abstract val storage: IBydStorage
}

expect fun createTestApp(): CommonTestComponent
