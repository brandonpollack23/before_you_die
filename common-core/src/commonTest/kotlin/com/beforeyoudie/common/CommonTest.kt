package com.beforeyoudie.common

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import io.kotest.core.Tuple2
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.koin.core.context.stopKoin

// TODO now remove koin and move this and sqldelight test to kotlin-inject

abstract class CommonTest : FunSpec() {
  init {
    beforeTest {
      Logger.setLogWriters(CommonWriter())
    }
    afterTest {
      stopKoin()
    }
  }

  final override fun afterTest(f: suspend (Tuple2<TestCase, TestResult>) -> Unit) =
    super.afterTest(f)
  final override fun beforeTest(f: suspend (TestCase) -> Unit) = super.beforeTest(f)
}

expect fun createTestApp()