package com.beforeyoudie

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.TestLogWriter
import com.beforeyoudie.common.state.TaskId
import com.benasher44.uuid.uuid4
import io.kotest.core.Tuple2
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

@OptIn(ExperimentalKermitApi::class)
abstract class CommonTest : FunSpec() {
  protected val testLogWriter = TestLogWriter(Severity.Verbose)

  init {
    beforeTest { Logger.setLogWriters(testLogWriter) }
    afterTest {}
  }

  final override fun afterTest(f: suspend (Tuple2<TestCase, TestResult>) -> Unit) =
    super.afterTest(f)
  final override fun beforeTest(f: suspend (TestCase) -> Unit) = super.beforeTest(f)
}

fun randomTaskId() = TaskId(uuid4())
